(ns bank-demo.handler
  (:require
    [integrant.core :as ig]))

(defn ping
  [_req]
  {:status 200
   :body "pong!"})

(defmethod ig/init-key :bank-demo/handler [_ _]
  #'ping)
