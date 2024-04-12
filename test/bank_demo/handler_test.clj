(ns bank-demo.handler-test
  (:require
    [bank-demo.core :refer [load-config! load-namespaces]]
    [clojure.test :refer [deftest is testing]]
    [integrant.core :as ig]
    [ring.mock.request :as mock]))

(def handler
  (-> (load-config!)
      (load-namespaces)
      (ig/init [:bank-demo/handler])
      :bank-demo/handler))

(deftest app-handler-test
  (testing "test request handling"
    (is (= (handler (mock/request :get "/"))
           {:status 200
            :body   "pong!"}))))
