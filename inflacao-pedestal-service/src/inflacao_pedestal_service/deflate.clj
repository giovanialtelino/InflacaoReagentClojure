(ns inflacao-pedestal-service.deflate
  (:require [inflacao-pedestal-service.database :as database]
            [inflacao-pedestal-service.data-acess :as data-access]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn deflate [index-1 index-2 value]
  (* (/ index-2 index-1) value))

(defn check-if-database-is-updated []
  (data-access/access-data))

(defn generate-graph [valor inicio fins]
  ;(check-if-database-is-updated)
  fins)