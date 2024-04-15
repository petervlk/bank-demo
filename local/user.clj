(ns user
  (:require
    [bank-demo.core :refer [load-config! load-namespaces]]
    [hato.client :as hc]
    [integrant.core :as ig]
    [integrant.repl :as ig-repl]
    [taoensso.timbre :as log]))

(ig-repl/set-prep!
  (fn []
    (-> (load-config!)
        load-namespaces
        ig/prep)))

(defn- async-response
  [resp]
  (-> resp
      deref
      (select-keys [:status :body])))

(defn- url
  [path]
  (str "http://localhost:9010" path))

(defn- req-opts
  ([] (req-opts {}))
  ([opts]
   (cond-> (assoc opts :async? true)
     (:body opts) (->
                    (update :body str)
                    (assoc :content-type :edn)))))

(comment
  (ig-repl/go)

  (ig-repl/halt)

  (ig-repl/reset)


  (log/set-level! :info)
  (log/set-level! :debug)

  ;; API
  (async-response
    (hc/post
      (url "/account")
      (req-opts {:body {:name "Mr. Dude Lebowski"}})))

  (async-response
    (hc/get
      (url "/account/2")
      (req-opts)))

  (async-response
    (hc/post
      (url "/account/6/deposit")
      (req-opts {:body {:amount 15}})))

  (async-response
    (hc/post
      (url "/account/7/withdraw")
      (req-opts {:body {:amount 23}})))

  (async-response
    (hc/post
      (url "/account/7/send")
      (req-opts {:body {:amount 23 :account-number 42}})))

  (async-response
    (hc/get
      (url "/account/7/audit")
      (req-opts)))

  *e)
