(ns bank-demo.handler
  (:require
    [bank-demo.handler.transaction :as trx]
    [bank-demo.schema :as schema]
    [integrant.core :as ig]
    [muuntaja.core]
    [reitit.coercion.malli]
    [reitit.ring :as ring]
    [reitit.ring.coercion :as rrc]
    [reitit.ring.middleware.exception :as mw-exception]
    [reitit.ring.middleware.muuntaja :as mw-muuntaja]))

(defmethod ig/init-key ::router [_ {:keys [create-account
                                           show-account
                                           audit-account]}]
  (ring/router
    [["/account"
      ["" {:post {:parameters {:body schema/CreateAccountRequest}
                  :responses {201 {:body schema/Account}}
                  :handler create-account}}]
      ["/:id"
       [""
        {:get {:parameters {:path schema/PathParamsAccountId}
               :responses  {200 {:body schema/Account}}
               :handler    show-account}}]
       ["/deposit"
        {:post {:parameters {:path schema/PathParamsAccountId
                             :body schema/DepositRequest}
                :responses  {200 {:body schema/Account}}
                :handler    #'trx/deposit-funds}}]
       ["/withdraw"
        {:post {:parameters {:path schema/PathParamsAccountId
                             :body schema/WithdrawalRequest}
                :responses  {200 {:body schema/Account}}
                :handler    #'trx/withdraw-funds}}]
       ["/send"
        {:post {:parameters {:path schema/PathParamsAccountId
                             :body schema/TransferRequest}
                :responses  {200 {:body schema/Account}}
                :handler    #'trx/transfer-funds}}]
       ["/audit"
        {:get {:parameters {:path schema/PathParamsAccountId}
               :responses  {200 {:body schema/AccountAudit}}
               :handler    audit-account}}]]]]
    {:data {:muuntaja   muuntaja.core/instance
            :coercion   reitit.coercion.malli/coercion
            :middleware [mw-muuntaja/format-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware
                         mw-exception/exception-middleware]}}))

(defmethod ig/init-key ::default-handler [_ _]
  (ring/routes
    (ring/redirect-trailing-slash-handler {:method :strip})
    (ring/create-default-handler)))

(defmethod ig/init-key ::app [_ {:keys [router default-handler]}]
  (ring/ring-handler
    router
    default-handler))
