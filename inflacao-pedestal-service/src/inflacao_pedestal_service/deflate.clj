(ns inflacao-pedestal-service.deflate
  (:require [inflacao-pedestal-service.database :as database]
            [inflacao-pedestal-service.data-acess :as data-access]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn deflate [index-1 index-2 value]
  (* (/ index-2 index-1) value))

(defn date-cleaner [date]
  (let [year (Integer/parseInt (subs date 0 4))
        month (Integer/parseInt (subs date 5 7))]
    (c/to-sql-date (t/date-time year month))))

(defn check-if-database-is-updated []
  (data-access/access-data))

(defn get-value-all-fins [fins]
  (println fins)
  (println (keyword (nth fins 0)))
  (println (database/get-value-date-all-table (date-cleaner (nth fins 0))))
  (let [fins-count (count fins)]
    (loop [i 0
           all-fins {}]
      (if (< i fins-count)
          (recur (inc i) (conj all-fins { (keyword (nth fins i)) (database/get-value-date-all-table (date-cleaner (nth fins i)))}))
          all-fins))))

(defn generate-graph [valor inicio fins]
     (let [get-valores-inicio (conj {} {(keyword inicio)(database/get-value-date-all-table (date-cleaner inicio))})
        get-value-all-fins (get-value-all-fins fins)]
       (println get-valores-inicio)
    [get-valores-inicio get-value-all-fins]))