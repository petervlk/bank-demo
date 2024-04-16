(ns bank-demo.cache
  (:require
    [bank-demo.db.account :as account]
    [integrant.core :as ig]
    [taoensso.timbre :as log]))

;; TODO add schema for cache
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
  [cache-data account-number]
  (get cache-data account-number))

(defn account-report
  [cache account-number]
  (log/debugf "CACHE: Fetching account report #%s" account-number)
  (some-> (fetch-account @cache account-number)
          (dissoc :transactions)))

(defn account-transactions-report
  [cache account-number]
  (log/debugf "CACHE: Fetching account transactions report #%s" account-number)
  (some-> (fetch-account @cache account-number)
          (get :transactions [])))

(defn transaction-accounts-exsist?
  [cache {:keys [account-source account-destination] :as transaction}]
  (if (->> [account-source account-destination]
           (filter identity)
           (every? #(contains? @cache %)))
    transaction
    (throw (ex-info "CACHE: Invalid transaction data." {:error "Account refs invalid"}))))

(defn add-transaction
  [cache transaction]
  (letfn [(update-balances
            [cache-data {:keys [account-source account-destination amount]}]
            (cond-> cache-data
              account-source (update-in [account-source :balance] - amount)
              account-destination (update-in [account-destination :balance] + amount)))
          (update-transactions
            [cache-data {:keys [account-source account-destination] :as trx}]
            (cond-> cache-data
              account-source (update-in [account-source :transactions] conj trx)
              account-destination (update-in [account-destination :transactions] conj trx)))
          (trx-to-accounts
            [cache-data trx]
            (-> cache-data
                (update-transactions trx)
                (update-balances trx)))]
    (swap! cache trx-to-accounts transaction)))

(defmethod ig/init-key ::simple-cache [_ {:keys [datasource]}]
  (atom (init-cache datasource)))
