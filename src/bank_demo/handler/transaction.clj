(ns bank-demo.handler.transaction
  (:require
    [bank-demo.cache :as cache]
    [bank-demo.db.transaction :as db]
    [integrant.core :as ig]))

(defn- trx-timestamped
  [trx]
  (assoc trx :timestamp (.toEpochMilli (java.time.Instant/now))))

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
       (throw (ex-info "Failed to withdraw funds" {:account-number id :amount amount}))))
    ([request respond raise]
     (try
       (respond (withdraw-funds request))
       (catch Exception e
         (raise e))))))

(defmethod ig/init-key ::transfer [_ {:keys [datasource cache]}]
  (fn transfer-funds
    ([{:as _request
       {{account-source :id}     :path
        {amount :amount account-destination :account-number} :body} :parameters}]
     (if-let [trx (->> {:amount amount
                        :account-source account-source
                        :account-destination account-destination}
                       (trx-timestamped)
                       ;; TODO - these validations and modifications of cache need to be atomic
                       (cache/transaction-accounts-exsist? cache)
                       (cache/sufficient-funds? cache)
                       (db/store-transaction! datasource))]
       {:status 200
        ;; TODO - refactor transaction fetching
        :body   (-> (cache/add-transaction cache trx)
                    (get account-source)
                    (dissoc :transactions))}
       (throw (ex-info
                "Failed to transfer funds"
                {:account-source account-source
                 :account-destination account-destination
                 :amount amount}))))
    ([request respond raise]
     (try
       (respond (transfer-funds request))
       (catch Exception e
         (raise e))))))
