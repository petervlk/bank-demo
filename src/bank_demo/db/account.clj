(ns bank-demo.db.account
  (:require
    [next.jdbc :as jdbc]
    [next.jdbc.sql :as sql]
    [taoensso.timbre :as log]))

(defn create-account-table!
  [ds]
  (jdbc/execute! ds ["
CREATE TABLE IF NOT EXISTS account (
  account_number int auto_increment primary key,
  name varchar(50) not null,
  balance int default 0
)"] jdbc/unqualified-snake-kebab-opts)
  [:account-table :ready])

(defn create-account!
  [ds account-holder-name]
  (log/debugf "DB: Creating account: %s" account-holder-name)
  (when-let [account (sql/insert! ds :account {:name account-holder-name}
                                  jdbc/unqualified-snake-kebab-opts)]
    (assoc account
           :name account-holder-name
           :balance 0)))

(defn get-all-accounts
  [ds]
  (sql/query ds ["select * from account"] jdbc/unqualified-snake-kebab-opts))
