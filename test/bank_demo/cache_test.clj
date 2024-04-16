(ns bank-demo.cache-test
  (:require
    [bank-demo.cache :as sut]
    [clojure.test :refer [deftest is testing]]))

(deftest add-transaction-test
  (let [cache {7 {:balance 100
                  :transactions []}
               1 {:balance 1000
                  :transactions [{:account-source      42
                                  :account-destination 1
                                  :amount              5}]}}
        trx {:account-source      1
             :account-destination 7
             :amount              5}
        credit-trx (dissoc trx :account-destination)
        debit-trx (dissoc trx :account-source)]
    (testing "credit"
      (is (= {7 {:balance 100
                 :transactions []}
              1 {:balance 995
                 :transactions [{:account-source      42
                                 :account-destination 1
                                 :amount              5}
                                credit-trx]}}
             (sut/add-transaction (atom cache) credit-trx))))
    (testing "debit"
      (is (= {7 {:balance 105
                 :transactions [debit-trx]}
              1 {:balance 1000
                 :transactions [{:account-source      42
                                 :account-destination 1
                                 :amount              5}]}}
             (sut/add-transaction (atom cache) debit-trx))))
    (testing "transfer"
      (is (= {7 {:balance 105
                 :transactions [trx]}
              1 {:balance 995
                 :transactions [{:account-source      42
                                 :account-destination 1
                                 :amount              5}
                                trx]}}
             (sut/add-transaction (atom cache) trx))))))
