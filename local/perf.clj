(ns perf
  (:require
    [hato.client :as hc]
    [trombi.core :as trombi]))

(defn- url
  [path]
  (str "http://localhost:9010" path))

(defn view-account-info [_]
  (let [{:keys [status]}
        @(hc/get
           (url "/account/1")
           {:async? true}
           identity
           ex-data)]
    (= status 200)))

(defn audit-account [_]
  (let [{:keys [status]}
        @(hc/get
           (url "/account/1/audit")
           {:async? true}
           identity
           ex-data)]
    (= status 200)))

(defn deposit-funds [_]
  (let [{:keys [status]}
        @(hc/post
           (url "/account/1/deposit")
           {:async?       true
            :body         "{\"amount\": 15}"
            :content-type :json}
           identity
           ex-data)]
    (= status 200)))

(defn withdraw-funds [_]
  (let [{:keys [status]}
        @(hc/post
           (url "/account/1/withdraw")
           {:async?       true
            :body         "{\"amount\": 7}"
            :content-type :json}
           identity
           ex-data)]
    (= status 200)))

(defn transfer-funds [_]
  (let [{:keys [status]}
        @(hc/post
           (url "/account/1/send")
           {:body         "{:amount 2, :account-number 2}",
            :async?       true,
            :content-type :edn})]
    (= status 200)))

(comment

  (trombi/run
   {:name      "Simulation"
    :scenarios [{:name  "Scenario account 1"
                 :steps [{:name    "View account"
                          :request view-account-info}
                         {:name    "Deposit"
                          :request deposit-funds}
                         {:name    "View account"
                          :request view-account-info}
                         {:name    "Transfer"
                          :request transfer-funds}
                         {:name    "Withdraw"
                          :request withdraw-funds}
                         {:name    "Audit"
                          :request audit-account}
                         ]}]}
   {:concurrency 300}))
