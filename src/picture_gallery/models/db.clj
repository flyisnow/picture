(ns picture-gallery.models.db
  (:require [korma.db :refer :all]
            [korma.core :refer :all]))

(def db
  {:subprotocol "postgresql"
   :subname "//127.0.0.1:5432/gallery"
   :user "admin"
   :password "admin"})

(defdb korma-db db)
(defentity users)
(defentity images)


;; (defmacro with-db [f & body]
;;   `(sql/with-connection ~db (~f ~@body)))

(defn create-user [user]
  (insert users (values user)))
;;   (with-db sql/insert-record :users user))


(defn get-user [id]
  (first (select users
                 (where {:id id}))))

;;   (with-db sql/with-query-results
;;     res ["select * from users where id =?" id] (first res)))

(defn add-image [userid name]
  (transaction
   (if (empty? (select images
                       (where {:userid userid :name name})
                       (limit 1)))
     (insert images (values {:userid userid :name name}))
     (throw
      (Exception. "You have already uploaded an image with the same name")))))

;;   (with-db
;;     sql/transaction
;;     (if (sql/with-query-results
;;           res
;;           ["select userid from images where userid=? and name=? " userid name]
;;           (empty? res))
;;       (sql/insert-record :images {:userid userid :name name})
;;       (throw
;;        (Exception. "you have already uploaded an image with the same name")))))

;;   (sql/with-connection db
;;     (sql/with-query-results res ["select * from users "] (println res)))

(defn images-by-user [userid]
  (select images (where {:userid userid})))

;;   (with-db
;;     sql/with-query-results
;;     res ["select * from images where userid = ? " userid] (doall res)))

;;(images-by-user "hzm")
(defn delete-image [userid name]
  (delete images (where {:userid userid :name name})))

(defn get-gallery-previews []
  (exec-raw
   ["select * from
    (select * ,row_number() over (partition by userid) as row_number from images )
    as rows where row_number =1 " []]
   :results))
;;   (with-db
;;     sql/with-query-results
;;     res ["select * from
;;          (select *,row_number() over (partition by userid) as row_number from images)
;;          as rows where row_number=1"]
;;     (doall res)))


;; (defn delete-image [userid name]
;;   (with-db
;;     sql/delete-rows :images ["userid=? and name=?" userid name]))

(defn delete-user [userid]
  (delete users (where {:userid userid})))
;;   (with-db
;;     sql/delete-rows :users ["id=?" userid]))
