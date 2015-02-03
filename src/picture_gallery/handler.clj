(ns picture-gallery.handler
  (:require [compojure.core :refer [defroutes routes]]
            [noir.session :as session]
;;             [ring.middleware.resource :refer [wrap-resource]]
;;             [ring.middleware.file-info :refer [wrap-file-info]]
             [hiccup.middleware :refer [wrap-base-url]]
             [compojure.handler :as handler]
            [compojure.route :as route]
            [picture-gallery.routes.home :refer [home-routes]]
            [noir.util.middleware :as noir-middleware]
            [ring.middleware.session.memory :refer [memory-store]]
            [noir.validation :refer [wrap-noir-validation]]
            [taoensso.timbre :as timbre]
            [picture-gallery.routes.auth :refer [auth-routes]]
            [picture-gallery.routes.upload :refer [upload-routes]]
            [picture-gallery.routes.gallery :refer [gallery-routes]]))

(defn init []
  (timbre/set-config! [:timestamp -pattern] "yyyy-MM-dd HH:mm:ss")
  (println "picture-gallery is starting"))

(defn user-page [_]
  (println "user=" (session/get :user))
  (session/get :user)
  )

(defn destroy []
  (println "picture-gallery is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

;; (def app
;;   (-> (routes home-routes app-routes)
;;       (handler/site)
;;       (wrap-base-url)))

(defn print-request [handler]
  (fn [request]
     (println (str request))
    (handler request)))

(def app2 (noir-middleware/app-handler
          [home-routes
           auth-routes
           upload-routes
           gallery-routes
           app-routes]
;;           :access-rules  [{:uri "/upload" :rule user-page}]
           ))

(def app-old
  (-> app2
      ))


(def app
  (->  (routes
          auth-routes
          home-routes
          upload-routes
          gallery-routes
          app-routes)
      (handler/site )
      (wrap-base-url)
      (session/wrap-noir-session
       {:store (memory-store)})
      (wrap-noir-validation)))

