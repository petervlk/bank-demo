(ns bank-demo.handler.account
  (:require
    [bank-demo.db.account :as account]
    [integrant.core :as ig]))

(defmethod ig/init-key ::create [_ {:keys [datasource]}]
  (fn create-account
    ([{:as _request
       {{:keys [name]} :body} :parameters}]
     {:status 201
      :body (account/create-account! datasource name)})
    ([request respond _raise]
     (respond (create-account request)))))
