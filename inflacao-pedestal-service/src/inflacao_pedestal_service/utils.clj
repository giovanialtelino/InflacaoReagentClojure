(ns inflacao-pedestal-service.utils
  (:require [java-time :as jt]))

(defn left-zero-cleaner [value]
  (if (= 0 (Integer/parseInt (str (nth value 0))))
    (subs value 1 2)
    value))

(defn format-date-to-javatime [unformated-date]
  (let [year (Integer/parseInt (subs unformated-date 0 4))
        month (Integer/parseInt (left-zero-cleaner (subs unformated-date 5 7)))
        day (Integer/parseInt (left-zero-cleaner (subs unformated-date 8 10)))
        formated (jt/to-sql-date (jt/local-date-time year month day))]
    formated))
