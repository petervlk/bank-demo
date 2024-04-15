(ns bank-demo.db
  (:require
    [bank-demo.db.account :as account]
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
  {:account (account/create-account-table! datasource)})
