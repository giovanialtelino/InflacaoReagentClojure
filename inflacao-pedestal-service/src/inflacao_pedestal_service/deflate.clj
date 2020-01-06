(ns inflacao-pedestal-service.deflate
  (:require [inflacao-pedestal-service.database :as database]
            [inflacao-pedestal-service.data-acess :as data-access]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn deflate [index-1 index-2 value]
  (if (> index-1 0 )
    (* (with-precision 2 (/ index-2 index-1)) value)
    0))

(defn date-cleaner [date]
  (let [year (Integer/parseInt (subs date 0 4))
        month (Integer/parseInt (subs date 5 7))]
    (c/to-sql-date (t/date-time year month))))

(defn check-if-database-is-updated []
  (data-access/access-data))

(defn get-value-all-fins [fins]
  (let [fins-count (count fins)]
    (loop [i 0
           all-fins {}]
      (if (< i fins-count)
          (recur (inc i) (conj all-fins { (keyword (nth fins i)) (database/get-value-date-all-table (date-cleaner (nth fins i)))}))
          all-fins))))

(defn deflate-one-full-date [valor valores-inicio valores-fim data-inicio data-fim]
  (let [fim-key (keyword data-fim)
        precos12_inpc12 (keyword "precos12_inpc12")
        igp12_ipc12 (keyword "igp12_ipc12")
        igp12_igpdi12 (keyword "igp12_igpdi12")
        igp12_igpm12 (keyword "igp12_igpm12")
        precos12_ipca12 (keyword "precos12_ipca12")]
        {fim-key {
                  precos12_inpc12 (deflate (precos12_inpc12 valores-inicio) (precos12_inpc12 valores-fim) valor)
                  igp12_ipc12  (deflate (igp12_ipc12 valores-inicio) (igp12_ipc12 valores-fim) valor)
                  igp12_igpdi12 (deflate (igp12_igpdi12 valores-inicio) (igp12_igpdi12 valores-fim) valor)
                  igp12_igpm12 (deflate (igp12_igpm12 valores-inicio) (igp12_igpm12 valores-fim) valor)
                  precos12_ipca12 (deflate (precos12_ipca12 valores-inicio) (precos12_ipca12 valores-fim) valor)
                  }}))

(defn deflate-all-values-got [valor valores-inicio valores-finais inicio fins]
  (let [final-count (count fins)]
    (loop [i 0
           deflated {}]
      (if (< i final-count)
        (recur (inc i) (conj deflated (deflate-one-full-date valor ((keyword inicio) (nth valores-inicio 0)) ((keyword (nth fins i)) valores-finais ) inicio (nth fins i))))
        deflated ))))

(defn generate-graph [valor inicio fins]
     (let [get-valores-inicio (conj [] {(keyword inicio)(database/get-value-date-all-table (date-cleaner inicio))})
           get-value-all-fins (get-value-all-fins fins)
           deflated (deflate-all-values-got valor get-valores-inicio get-value-all-fins inicio fins)]
    deflated))