(ns picture-gallery.routes.auth
  (:require [hiccup.form :refer :all]
            [compojure.core :refer :all]
            [picture-gallery.routes.home :refer :all]
            [picture-gallery.views.layout :as layout]
            [picture-gallery.models.db :as db]
            [noir.util.crypt :as crypt]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as vali]
            [picture-gallery.util :refer [gallery-path]]
            [picture-gallery.routes.upload :refer [delete-image]]
            [ring.util.anti-forgery :refer :all]
            )
  (:import [java.io File]))

(defn create-gallery-path []
  (let [user-path (File. (gallery-path))]
    (if-not (.exsits user-path) (.mkdir user-path))
    (str (.getAbsolutePath user-path) File/separator)))


(defn valid? [id pass pass1]
  (vali/rule (vali/has-value? id)
             [:id "user id is required"])
  (vali/rule (vali/min-length? pass 5)
             [:pass "password must be at least 5 characters"])
  (vali/rule (= pass pass1)
             [:pass "entered passwords do not match"])
  (not (vali/errors? :id :pass :pass1)))

(defn error-item [[error]]
  [:div.error error])

(defn control [id label field]
  (list
   (vali/on-error id error-item)
   label field
   [:br]))

(defn registration-page [& [id]]
  (layout/base
   (form-to [:post "/register"]
            (anti-forgery-field)
            (control :id
              (label "user-id" "user id")
              (text-field {:tabindex 1} "id" id))
            (control :pass
              (label "pass" "password")
              (password-field {:tabindex 2} "pass"))
            (control :pass1
              (label "pass1" "retype password")
              (password-field {:tabindex 3} "pass1"))
            (submit-button {:tabindex 4} "create account"))))


(defn format-error [id ex]
  (cond
   (and (instance? org.postgresql.util.PSQLException ex)
        (= 0 (.getErrorCode ex)))
   (str "The user with id " id " already exists!")

   :else
   (str "An error has occured while processiong the request" (.getMessage ex))))


(defn handle-registration [id pass pass1]
  (if (valid? id pass pass1)
    (try
      (db/create-user {:id id :pass (crypt/encrypt pass)})
      (session/put! :user id)
      (create-gallery-path)
      (resp/redirect "/")
      (catch Exception ex
        (vali/rule false [:id (format-error id ex)])
        (registration-page)))
    (registration-page id)))

;; (handle-registration "hzm" "123456" "123456")

;; (valid? "hzm" "123456" "123456")

(defn handle-login [id pass]
  (let [user (db/get-user id)]
    (if (and user    (crypt/compare pass (:pass user)))
      (session/put! :user id)))
  (resp/redirect "/"))

(defn handle-logout []
  (session/clear!)
  (resp/redirect "/"))

(defn delete-account-page []
  (if-let [user (session/get :user)]
    (layout/common
     (form-to [:post "/confirm-delete"]
              (anti-forgery-field)
              (submit-button "delete account"))
     (form-to [:get "/"]
              (submit-button "cancel")))
    (resp/redirect "/")))

(defn handle-confirm-delete []
  (if-let [user (session/get :user)]
    (do
    (doseq [{:keys [name]} (db/images-by-user user)]
      (println name)
      (delete-image user name))
    (clojure.java.io/delete-file (gallery-path))
    (db/delete-user user)))
  (session/clear!)
  (resp/redirect "/"))

;;   (clojure.java.io/delete-file (str "galleries" File/separator "hzm" ))
;;   (clojure.java.io/delete-file (str "galleries" File/separator"test"))
;;   (clojure.java.io/delete-file  "galleries\\test")
(defroutes auth-routes
  (GET "/register" []
       (registration-page))
  (POST "/register" [id pass pass1]
;;         ("特三通"))
         (handle-registration id pass pass1))
  (POST "/login" [id pass ]
        (handle-login id pass))
  (GET "/logout" []
       (handle-logout))
  (GET "/delete-account" [] (delete-account-page))
  (POST "/confirm-delete" []
        (handle-confirm-delete))
  )

