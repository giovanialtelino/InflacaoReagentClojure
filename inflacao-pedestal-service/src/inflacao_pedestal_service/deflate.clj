(ns inflacao-pedestal-service.deflate
  (:require [inflacao-pedestal-service.database :as database]
            [inflacao-pedestal-service.data-acess :as data-access]
            [ti]))

(defn deflate [index-1 index-2 value]
  (* (/ index-2 index-1) value))

(defn check-if-database-is-updated []
  (data-access/access-data))

(defn generate-graph [json-data]
  (check-if-database-is-updated)
  (let [base-item (nth json-data 0)]

    ))