(ns bank-demo.handler.transaction
  (:require
    [bank-demo.schema :as schema]
    [malli.generator :as mg]
    [muuntaja.core]
    [reitit.coercion.malli]))

(defn- dummy-account
  ([]
   (mg/generate schema/Account))
  ([data]
   (merge (mg/generate schema/Account) data)))

(defn deposit-funds
  ([{:as _request
     {{:keys [id]}     :path
      {:keys [amount]} :body} :parameters}]
   {:status 200
    :body   (dummy-account {:account-number id :balance amount})})
  ([request respond _raise]
   (respond (deposit-funds request))))

(defn withdraw-funds
  ([{:as _request
     {{:keys [id]}     :path
      {:keys [amount]} :body} :parameters}]
   {:status 200
    :body (dummy-account {:account-number id :balance amount})})
  ([request respond _raise]
   (respond (withdraw-funds request))))

(defn transfer-funds
  ([{:as _request
     {{:keys [id]}     :path
      {:keys [amount _account-number]} :body} :parameters}]
   {:status 200
    :body (dummy-account {:account-number id :balance amount})})
  ([request respond _raise]
   (respond (deposit-funds request))))
