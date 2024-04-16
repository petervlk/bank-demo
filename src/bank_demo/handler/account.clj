(ns bank-demo.handler.account
  (:require
    [bank-demo.cache :as cache]
    [bank-demo.db.account :as account]
    [bank-demo.db.transaction :as transaction]
    [integrant.core :as ig]))

(defmethod ig/init-key ::create [_ {:keys [datasource cache]}]
  (fn create-account
    ([{:as _request
       {{:keys [name] :as req-body} :body} :parameters}]
     (if-let [new-account (account/create-account! datasource name)]
       (do
         (cache/add-account cache new-account)
         {:status 201
          :body new-account})
       (throw (ex-info "Failed to create a new account" req-body))))
    ([request respond raise]
     (try
       (respond (create-account request))
       (catch Exception e
         (raise e))))))

(defmethod ig/init-key ::show [_ {:keys [cache]}]
  (fn show-account
    ([{:as _request
       {{:keys [id]} :path} :parameters}]
     (if-let [account (cache/account-report cache id)]
       {:status 200
        :body account}
       {:status 404}))
    ([request respond _raise]
     (respond (show-account request)))))

(defmethod ig/init-key ::audit [_ {:keys [cache]}]
  (fn audit-account
    ([{:as _request
       {{:keys [id]} :path} :parameters}]
     (if-let [trxs (->>
                     (cache/account-transactions-report cache id)
                     (sort-by :timestamp)
                     (map #(transaction/transaction-report id %))
                     (map-indexed (fn [idx trx] (assoc trx :sequence idx)))
                     (reverse)
                     (into []))]
       {:status 200
        :body trxs}
       {:status 404}))
    ([request respond _raise]
     (respond (audit-account request)))))
