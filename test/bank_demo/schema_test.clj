(ns bank-demo.schema-test
  (:require
    [bank-demo.schema :as sut]
    [clojure.test :refer [deftest is testing]]
    [malli.core :as m]
    [malli.error :as me]))

(deftest pos-int-schema-test
  (is (true? (m/validate sut/PosInt 1)))
  (is (false? (m/validate sut/PosInt 0))))

(deftest nat-int-schema-test
  (is (true? (m/validate sut/NatInt 1)))
  (is (true? (m/validate sut/NatInt 0)))
  (is (false? (m/validate sut/NatInt -1))))

(deftest non-empty-string-schema-test
  (testing "validation"
    (is (true? (m/validate sut/AccountHolderName "a")))
    (is (false? (m/validate sut/AccountHolderName "")))
    (is (false? (m/validate sut/AccountHolderName "\t")))
    (is (false? (m/validate sut/AccountHolderName " "))))
  (testing "error messages"
    (is (= ["String must not be blank!"]
           (me/humanize (m/explain sut/NonEmptyString " "))))))

(deftest transaction-audit-schema-test
  (testing "valid data"
    (is (true? (m/validate sut/AccountTransactionReport {:sequence    0
                                                         :debit       100
                                                         :description "deposit"})))
    (is (true? (m/validate sut/AccountTransactionReport {:sequence    1
                                                         :credit      2000
                                                         :description "withdrawal"}))))
  (testing "invalid data"
    (is (false? (m/validate sut/AccountTransactionReport {:sequence    2
                                                          :description "none"})))
    (is (false? (m/validate sut/AccountTransactionReport {:sequence    3
                                                          :credit      24
                                                          :debit       42
                                                          :description "both"})))))

(comment
  (require '[malli.generator :as mg])

  (mg/generate sut/Account)
  ;; => {:account-number 869, :name "0975SMzn449C0", :balance 1745}

  (mg/generate sut/AccountTransactionReport)
  ;; => {:sequence 177, :description "23d0r5X", :credit 3}
  ;; => {:sequence 29, :description "mpp46bjln90rwYkfd5Pa7d8az", :debit 3}

  (mg/generate sut/AccountAudit)
  ;; => [{:sequence 54268, :description "j92O49Ugb1m2", :debit 191}
  ;;     {:sequence 50490, :description "7C8bP0", :credit 262}
  ;;     {:sequence 446061, :description "Jid64", :debit 4197733}]

  (mg/generate sut/Transaction)
  ;; => {:account-source nil,
  ;;     :account-destination 118425804,
  ;;     :amount 1420,
  ;;     :timestamp 883697515}

  *e)
