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

(defn store-transaction!
  [ds transaction]
  (log/debug "DB: Store transaction" transaction)
  (sql/insert! ds :transaction transaction jdbc/unqualified-snake-kebab-opts)
  transaction)

(defn get-all-transactions
  [ds]
  (sql/query ds ["select * from transaction"] jdbc/unqualified-snake-kebab-opts))

(defn transaction-report
  [audited-account {:keys [account-source account-destination amount] :as trx}]
  (letfn [(audited? [account-number]
            (= audited-account account-number))
          (transfer? [{:keys [account-source account-destination]}]
            (and account-source account-destination))]
    (cond
      (and (transfer? trx) (audited? account-source))
      {:debit amount
       :description (str "send to #" account-destination)}
      (and (transfer? trx) (audited? account-destination))
      {:credit amount
       :description (str "receive from #" account-source)}
      (audited? account-source)
      {:debit amount
       :description "withdraw"}
      (audited? account-destination)
      {:credit amount
       :description "deposit"})))
