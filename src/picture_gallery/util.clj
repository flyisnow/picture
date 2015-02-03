(ns picture-gallery.util
  (:require [noir.session :as session]
            [hiccup.util :refer [url-encode]])
  (:import  [java.io File ])
  )

(def galleries "galleries")

(defn gallery-path[]
  (str galleries File/separator (session/get :user)))


(def thumb-prefix "thumb")

(defn image-uri [userid file-name]
  (str "/img/" userid "/" (url-encode file-name)))

(defn thumb-uri [userid file-name]
  (image-uri userid (str thumb-prefix file-name)))
