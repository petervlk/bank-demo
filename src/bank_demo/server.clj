(ns bank-demo.server
  (:require
    [integrant.core :as ig]
    [ring.adapter.jetty :refer [run-jetty]]
    [taoensso.timbre :as log]))

(defmethod ig/init-key :bank-demo/server [_ {:keys [port handler]}]
  (log/info "Starting server")
  (run-jetty handler {:port port
                      :async? true
                      :join? false}))

(defmethod ig/halt-key! :bank-demo/server [_ jetty]
  (log/info "Stopping server")
  (when jetty
    (.stop jetty)))
