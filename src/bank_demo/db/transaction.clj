(ns bank-demo.db.transaction
  (:require
    [next.jdbc :as jdbc]
    [next.jdbc.sql :as sql]
    [taoensso.timbre :as log]))

(defn create-transaction-table!
  [ds]
  (jdbc/execute! ds ["
CREATE TABLE IF NOT EXISTS transaction (
  id int auto_increment primary key,
  timestamp bigint not null,
  amount int not null,
  account_source int default null,
  account_destination int default null,
  FOREIGN KEY (account_source) REFERENCES account(account_number), 
  FOREIGN KEY (account_destination) REFERENCES account(account_number),
  CHECK(amount > 0),
  CHECK(account_source is not null OR account_destination is not null),
  CHECK(account_destination != account_source)
)"]))

(defn create-transaction!
  [ds transaction]
  (log/debug "DB: Creating transaction" transaction)
  (let [epoch-milli (.toEpochMilli (java.time.Instant/now))
        trx (assoc transaction :timestamp epoch-milli)]
    (sql/insert! ds :transaction trx jdbc/unqualified-snake-kebab-opts)
    trx))

(defn get-all-transactions
  [ds]
  (sql/query ds ["select * from transaction"] jdbc/unqualified-snake-kebab-opts))

(defn transaction-report
  [{from   :account-source
    to     :account-destination
    amount :amount}]
  ;; TODO
  (throw (ex-info "Not yet implemented" {:msg "missing implementation"})))

(comment

  (require 'integrant.repl.state)

  (defn ds []
    (:bank-demo.db/datasource integrant.repl.state/system))

  (create-transaction-table! (ds))

  (sql/insert! (ds) :transaction
               {
                :amount 51
                :account-source 1
                :account-destination 2
                :timestamp 10}
               jdbc/unqualified-snake-kebab-opts)

  (get-all-transactions (ds))
  
  (create-transaction!
    (ds)
    {:amount              51
     :account-source      1
     :account-destination 2})

  (.toEpochMilli (java.time.Instant/now))

  (:bank-demo.db/populator integrant.repl.state/system)

  *e)
