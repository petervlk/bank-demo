(ns bank-demo.handler-test
  (:require
    [bank-demo.core :refer [load-config! load-namespaces]]
    [bank-demo.handler :as sut]
    [clojure.test :refer [deftest are testing]]
    [integrant.core :as ig]
    [reitit.core]
    [reitit.ring]))

(defn method-on-path?
  [router method path]
  (some-> router
          (reitit.core/match-by-path path)
          (get-in [:result method])))

(deftest routes-test
  (let [router (-> (load-config!)
                   (load-namespaces)
                   (ig/init [::sut/router])
                   ::sut/router)]
    (testing "test request handling"
      (are [http-method path] (some? (method-on-path? router http-method path))
        :post "/account"
        :get  "/account/1"
        :get  "/account/1/audit"
        :post "/account/1/deposit"
        :post "/account/1/send"
        :post "/account/1/withdraw"))))
