(ns bank-demo.handler.account
  (:require
    [bank-demo.cache :as cache]
    [bank-demo.db.account :as account]
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
    ([request respond _raise]
     (respond (create-account request)))))
