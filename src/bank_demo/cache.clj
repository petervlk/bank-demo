(ns bank-demo.cache
  (:require
    [bank-demo.db.account :as account]
    [integrant.core :as ig]
    [taoensso.timbre :as log]))

(defn init-cache
  [datasource]
  (into {}
        (map (juxt :account-number identity))
        (account/get-all-accounts datasource)))

(defn add-account
  [cache {:keys [account-number] :as account}]
  (log/debug "CACHE: Adding account" account)
  (swap! cache assoc account-number account))

(defn fetch-account
  [cache account-number]
  (log/debugf "CACHE: Fetching account #%s" account-number)
  (get @cache account-number))

(defn fetch-account-info
  [cache account-number]
  (some-> (fetch-account cache account-number)
          (dissoc :transactions)))

(defn fetch-account-transactions
  [cache account-number]
  (some-> (fetch-account cache account-number)
          (get :transactions [])))

(defmethod ig/init-key ::simple-cache [_ {:keys [datasource]}]
  (atom (init-cache datasource)))
