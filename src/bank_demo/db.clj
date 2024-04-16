(ns bank-demo.db
  (:require
    [bank-demo.db.account :refer [create-account-table!]]
    [bank-demo.db.transaction :refer [create-transaction-table!]]
    [hikari-cp.core :as hikari]
    [integrant.core :as ig]
    [taoensso.timbre :as log]))

(defmethod ig/init-key ::datasource [_ config]
  (log/info "Initializing datasource connection pool")
  (hikari/make-datasource config))

(defmethod ig/halt-key! ::datasource [_ datasource]
  (log/info "Closing datasource connection pool")
  (hikari/close-datasource datasource))

(defmethod ig/init-key ::populator [_ {:keys [datasource]}]
  (mapv #(% datasource) [create-account-table! create-transaction-table!]))
