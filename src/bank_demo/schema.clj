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

(def TransactionAudit
  (let [base [:map {:closed true}
              [:sequence #'NatInt]
              [:description #'NonEmptyString]]]
    [:or
     (mu/assoc base :credit #'PosInt)
     (mu/assoc base :debit #'PosInt)]))

(def AccountAudit
  [:vector TransactionAudit])

(def TransactionRequest
  [:map
   [:amount #'PosInt]])

(def WithdrawalRequest #'TransactionRequest)
(def DepositRequest #'TransactionRequest)

(def TransferRequest
  (mu/assoc TransactionRequest :account-number #'AccountId))
