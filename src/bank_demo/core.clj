(ns bank-demo.core
  (:gen-class)
  (:require
    [clojure.java.io :as io]
    [integrant.core :as ig]))

(defn load-config!
  ([]
   (-> (io/resource "config.edn")
       slurp
       (ig/read-string))))

(defn load-namespaces [cfg]
  (ig/load-namespaces cfg)
  cfg)

(defn -main
  [& _args]
  (-> (load-config!)
      load-namespaces
      ig/init))
