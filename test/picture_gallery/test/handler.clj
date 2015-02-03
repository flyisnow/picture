(ns picture-gallery.test.handler
  (:require [clojure.test :refer :all]
            [noir.util.crypt :refer [encrypt]]

            [picture-gallery.handler :refer :all])
  (:use clojure.test
        ring.mock.request
        picture-gallery.handler))

(defn mock-get-user [id]
  (if ( = id "foo")
    {:id "foo" :pass (encrypt "123456")}))

(deftest test-login
  (testing  "login success"
    (with-redefs [picture-gallery.models.db/get-user mock-get-user]
      (is
       (-> (request :post "/login" {:id "foo" :pass "123456"})
         app :headers (get "Set-Cookie") empty?))))

  (testing "password mismatch"
    (with-redefs [picture-gallery.models.db/get-user mock-get-user]
      (is
       (-> (request :post "/login" {:id "foo" :pass "12345"})
           app :headers (get "Set-Cookie") empty?))))
  (testing "user not found"
    (with-redefs [picture-gallery.models.db/get-user mock-get-user]
      (is
       (-> (request :post "/login" {:id "bar" :pass "123456"})
           app :headers (get "Set-Cookie") empty? )))))



(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
