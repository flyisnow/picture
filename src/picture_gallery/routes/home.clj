(ns picture-gallery.routes.home
  (:require [compojure.core :refer :all]
            [picture-gallery.views.layout :as layout]
            [picture-gallery.routes.gallery :refer [show-galleries]]
            [noir.session :as session]))


(defn home []
(layout/common
   (show-galleries)))
;;   (println "test")
;;     [:h1 "Hello " (session/get :user)]
;;                    [:h1 (session/get :all)]))

(defroutes home-routes
  (GET "/" [] (home)))
