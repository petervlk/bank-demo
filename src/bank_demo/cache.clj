(ns bank-demo.cache
  (:require
    [bank-demo.db.account :as account]
    [bank-demo.db.transaction :as transaction]
    [integrant.core :as ig]
    [taoensso.timbre :as log]))

(defn- update-balances
  [cache-data {:keys [account-source account-destination amount]}]
  (cond-> cache-data
    account-source (update-in [account-source :balance] - amount)
    account-destination (update-in [account-destination :balance] + amount)))

(defn- update-transactions
  [cache-data {:keys [account-source account-destination] :as trx}]
  (cond-> cache-data
    account-source (update-in [account-source :transactions] conj trx)
    account-destination (update-in [account-destination :transactions] conj trx)))

(defn- trx-to-accounts
  [cache-data trx]
  (-> cache-data
      (update-transactions trx)
      (update-balances trx)))

(defn add-transaction
  [cache transaction]
  (swap! cache trx-to-accounts transaction))

;; TODO add schema for cache
(defn init-cache
  [datasource]
  (let [accounts (account/get-all-accounts datasource)
        transactions (transaction/get-all-transactions datasource)
        cached-accounts (into {} (map (juxt :account-number identity)) accounts)]
    (reduce trx-to-accounts cached-accounts transactions)))

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
    (throw (ex-info "CACHE: Illegal transaction data." {:error "Account refs invalid"}))))

(defn sufficient-funds?
  [cache {:keys [account-source amount] :as transaction}]
  (let [data @cache]
    (if (> amount (get-in data [account-source :balance] 0))
      (throw (ex-info "CACHE: Illegal transaction data." {:error "Insufficient funds in source account"}))
      transaction)))

(defmethod ig/init-key ::simple-cache [_ {:keys [datasource]}]
  (atom (init-cache datasource)))
