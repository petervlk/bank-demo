(ns user
  (:require
    [bank-demo.core :refer [load-config! load-namespaces]]
    [integrant.core :as ig]
    [integrant.repl :as ig-repl]))

(ig-repl/set-prep!
  (fn []
    (-> (load-config!)
        load-namespaces
        ig/prep)))

(comment

  (ig-repl/go)

  (ig-repl/halt)

  (ig-repl/reset)

  *e)
