(ns bank-demo.handler.transaction
  (:require
    [bank-demo.cache :as cache]
    [bank-demo.db.transaction :as db]
    [bank-demo.schema :as schema]
    [integrant.core :as ig]
    [malli.generator :as mg]))

(defn- trx-timestamped
  [trx]
  (assoc trx :timestamp (.toEpochMilli (java.time.Instant/now))))

(defn- dummy-account
  ([]
   (mg/generate schema/Account))
  ([data]
   (merge (mg/generate schema/Account) data)))

(defmethod ig/init-key ::deposit [_ {:keys [datasource cache]}]
  (fn deposit-funds
    ([{:as _request
       {{:keys [id]}     :path
        {:keys [amount]} :body} :parameters}]
     (if-let [trx (->> {:amount amount :account-destination id}
                       (cache/transaction-accounts-exsist? cache)
                       (trx-timestamped)
                       (db/store-transaction! datasource))]
       {:status 200
        ;; TODO - refactor transaction fetching
        :body   (-> (cache/add-transaction cache trx)
                    (get id)
                    (dissoc :transactions))}
       (throw (ex-info "Failed to deposit funds" {:account-number id :amount amount}))))
    ([request respond raise]
     (try
       (respond (deposit-funds request))
       (catch Exception e
         (raise e))))))

(defmethod ig/init-key ::withdraw [_ {:keys [datasource cache]}]
  (fn withdraw-funds
    ([{:as _request
       {{:keys [id]}     :path
        {:keys [amount]} :body} :parameters}]
     (if-let [trx (->> {:amount amount :account-source id}
                       (trx-timestamped)
                       ;; TODO - these validations and modifications of cache need to be atomic
                       (cache/transaction-accounts-exsist? cache)
                       (cache/sufficient-funds? cache)
                       (db/store-transaction! datasource))]
       {:status 200
        ;; TODO - refactor transaction fetching
        :body   (-> (cache/add-transaction cache trx)
                    (get id)
                    (dissoc :transactions))}
       (throw (ex-info "Failed to deposit funds" {:account-number id :amount amount}))))
    ([request respond raise]
     (try
       (respond (withdraw-funds request))
       (catch Exception e
         (raise e))))))

(defn transfer-funds
  ([{:as _request
     {{:keys [id]}     :path
      {:keys [amount _account-number]} :body} :parameters}]
   {:status 200
    :body (dummy-account {:account-number id :balance amount})})
  ([request respond _raise]
   (respond (transfer-funds request))))
