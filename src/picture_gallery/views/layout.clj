(ns picture-gallery.views.layout
  (:require [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.element :refer [link-to]]
            [noir.session :as session]
            [ring.util.anti-forgery :refer :all]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]
            [hiccup.form :refer :all]))

(defn utf-8-response [html]
  (content-type (response html) "text/html; charset=utf-8")
  )

(deftype RenderablePage [content]
  Renderable
  (render [this request]
          (println "in render")
          (utf-8-response
           (html5
            [:head
             [:title "Welcome to picture-gallery"]
             (include-css "/css/screen.css")
             [:script {:type "text/javascript"}
              (str "var context=\"" (:context request) "\";")]
             (include-js "//code.jquery.com/jquery-2.0.2.min.js"
                         "js/color.js"
                         "/js/site.js")]
             [:body content]))))


(defn make-menu [& items]
  [:div#usermenu (for [item items ] [:div.menuitem item])])


(defn base [& content]
  (RenderablePage. content))

;;(base-old "hfe")
(defn base-old [& content]
 ;;(utf-8-response
   (html5
   [:head
    [:title "Welcome to picture-gallery"]
    (include-css "/css/screen.css")
   (include-js "//code.jquery.com/jquery-2.0.2.min.js")]
   [:body content]))
;;)

;; (defn common [& content]
;;   (base
;;    (if-let [user (session/get :user)]
;;     (list
;;      [:div (link-to "/logout" (str "logout " user))]
;;      [:div (link-to "/upload" "upload file")])
;;      [:div (link-to "/register" "register")
;;       (form-to [:post "/login"]
;;                (text-field {:placeholder "screen name"} "id")
;;                (password-field {:placeholder "password"} "pass")
;;                (submit-button "login"))])
;;    content))

(defn guest-menu []
  (make-menu
   (link-to "/" "home")
   (link-to "/register" "register")
   (form-to [:post "/login"]
            (anti-forgery-field)
            (text-field {:placeholder "screen name" } "id")
            (password-field {:placeholder "password" } "pass")
            (submit-button "login"))))

(defn guest-menu-old []
  [:div (link-to "/register" "register")
   (form-to [:post "/login"]
            (anti-forgery-field)
            (text-field {:placeholder "screen name" } "id")
            (password-field {:placeholder "password" } "pass")
            (submit-button "login"))])

(defn user-menu [user]
  (make-menu
   (link-to "/" "home")
   (link-to "/upload" "upload images")
   (link-to (str "/gallery/" user) "your galleries")
   (link-to "/logout" (str "logout " user))
   (link-to "/delete-account" "delete account")))

;;   (list
;;    [:div (link-to "/logout" (str "logout " user))]
;;    [:div (link-to "/upload" "upload file")]))

(defn common [& content]
  (base
   (if-let [user (session/get :user)]
     (user-menu user)
     (guest-menu))
   [:div.content content]))
