(ns bank-demo.db.transaction-test
  (:require
    [bank-demo.db.transaction :as sut]
    [clojure.test :refer [deftest is testing]]))

(deftest transaction-report-test
  (let [amount 42
        trx {:amount              amount
             :account-source      1
             :account-destination 2}]
    (testing "transfer between accounts"
      (is (= {:debit amount :description "send to #2"}
             (sut/transaction-report 1 trx)))
      (is (= {:credit amount :description "receive from #1"}
             (sut/transaction-report 2 trx))))
    (testing "withdraw"
      (is (= {:debit amount :description "withdraw"}
             (sut/transaction-report 1 (dissoc trx :account-destination)))))
    (testing "deposit"
      (is (= {:credit amount :description "deposit"}
             (sut/transaction-report 2 (dissoc trx :account-source)))))))
