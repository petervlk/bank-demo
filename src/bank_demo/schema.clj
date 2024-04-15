(ns bank-demo.schema
  (:require
    [clojure.string :as string]
    [malli.util :as mu]))

(def PosInt [:and :int [:> 0]])
(def NatInt [:and :int [:>= 0]])

(def NonEmptyString
  [:and
   [:string {:min 1}]
   [:fn {:error/message "String must not be blank!"} (complement string/blank?)]])

(def AccountId #'PosInt)

(def AccountBalance #'NatInt)

(def AccountHolderName #'NonEmptyString)

(def PathParamsAccountId
  [:map
   [:id #'AccountId]])

(def CreateAccountRequest
  [:map
   [:name #'AccountHolderName]])

(def Account
  [:map
   [:account-number #'AccountId]
   [:name #'AccountHolderName]
   [:balance #'AccountBalance]])

(def TransactionAmount #'PosInt)

(def AccountTransactionReport
  (let [base [:map {:closed true}
              [:sequence #'NatInt]
              [:description #'NonEmptyString]]]
    [:or
     (mu/assoc base :credit #'TransactionAmount)
     (mu/assoc base :debit  #'TransactionAmount)]))

(def AccountAudit
  [:vector AccountTransactionReport])

(def TransactionRequest
  [:map
   [:amount #'PosInt]])

(def WithdrawalRequest #'TransactionRequest)
(def DepositRequest #'TransactionRequest)

(def TransferRequest
  (mu/assoc TransactionRequest :account-number #'AccountId))

(def TransactionTimestamp #'NatInt) ; (.toEpochMilli (java.time.Instant/now))

(def Transaction
  [:and
   [:map
    [:account-source [:maybe #'AccountId]]
    [:account-destination [:maybe #'AccountId]]
    [:amount #'TransactionAmount]
    [:timestamp #'TransactionTimestamp]]
   [:fn {:error/message "Source and/or destination accounts must be specified!"}
    (fn [{:keys [account-source account-destination]}]
      (or account-source account-destination))]])
