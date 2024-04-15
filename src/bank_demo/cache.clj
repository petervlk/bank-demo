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
  (log/debug "CACHE: Add account" account)
  (swap! cache assoc account-number account))

(defmethod ig/init-key ::simple-cache [_ {:keys [datasource]}]
  (atom (init-cache datasource)))
