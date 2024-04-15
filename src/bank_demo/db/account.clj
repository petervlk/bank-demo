(ns bank-demo.db.account
  (:require
    [next.jdbc :as jdbc]
    [next.jdbc.result-set :as rs]
    [next.jdbc.sql :as sql]))

(defn create-account-table!
  [ds]
  (jdbc/execute! ds ["
CREATE TABLE IF NOT EXISTS account (
  account_number int auto_increment primary key,
  name varchar(50) not null,
  balance int default 0
)"]))

(defn create-account!
  [ds account-holder-name]
  (when-let [account (sql/insert! ds :account {:name account-holder-name}
                                  {:return-keys true
                                   :builder-fn rs/as-unqualified-kebab-maps})]
    (assoc account
           :name account-holder-name
           :balance 0)))
