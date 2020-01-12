(ns inflacao-pedestal-service.deflate
  (:require [inflacao-pedestal-service.database :as database]
            [inflacao-pedestal-service.data-acess :as data-access]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [java-time :as jt]))

(defn deflate [index-1 index-2 value]
  (if (> index-1 0)
    (* (/ index-2 index-1) value)
    0))

(defn front-end-date-parser [unparsed-date]
  (str unparsed-date "-01"))

(defn get-value-all-fins [fins]
  (let [fins-count (count fins)]
    (loop [i 0
           all-fins {}]
      (if (< i fins-count)
        (if (= 7 (count (nth fins i)))
          (recur (inc i) (conj all-fins {(keyword (nth fins i))
                                         (database/get-value-date-all-table (front-end-date-parser (nth fins i)))}))
          (recur (inc i) all-fins))
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
              igp12_ipc12     (deflate (igp12_ipc12 valores-inicio) (igp12_ipc12 valores-fim) valor)
              igp12_igpdi12   (deflate (igp12_igpdi12 valores-inicio) (igp12_igpdi12 valores-fim) valor)
              igp12_igpm12    (deflate (igp12_igpm12 valores-inicio) (igp12_igpm12 valores-fim) valor)
              precos12_ipca12 (deflate (precos12_ipca12 valores-inicio) (precos12_ipca12 valores-fim) valor)
              }}))

(defn deflate-all-values-got [valor valores-inicio valores-finais inicio fins]
  (let [final-count (count fins)]
    (loop [i 0
           deflated {}]
      (if (< i final-count)
        (if (= 7 (count (nth fins i)))
          (recur (inc i) (conj deflated (deflate-one-full-date valor ((keyword inicio) (nth valores-inicio 0)) ((keyword (nth fins i)) valores-finais) inicio (nth fins i))))
          (recur (inc i) deflated))
        deflated))))

(defn inflacao-all-values-got [deflated datas]
  (loop [i 0
         cleaned deflated]
    (if (< i (count datas))
      (if (= 7 (count (nth datas i)))
        (let [date (nth datas i)
            date-keyword (keyword date)
            new-values  {:precos12_ipca12 (- (get-in deflated [date-keyword :precos12_ipca12]) 100)
                         :precos12_inpc12 (- (get-in deflated [date-keyword :precos12_inpc12]) 100)
                         :igp12_ipc12 (- (get-in deflated [date-keyword :igp12_ipc12]) 100)
                         :igp12_igpm12 (- (get-in deflated [date-keyword :igp12_igpm12]) 100)
                         :igp12_igpdi12 (- (get-in deflated [date-keyword :igp12_igpdi12]) 100)}]
          (recur (inc i) (assoc cleaned date-keyword new-values)))
          (recur (inc i) cleaned))
          cleaned)))

(defn generate-graph [valor inicio fins]
  (let [data-inicio-menos-1 (str (jt/minus (jt/local-date (front-end-date-parser inicio)) (jt/months 1)))
    get-valores-inicio (conj [] {(keyword inicio) (database/get-value-date-all-table data-inicio-menos-1)})
    get-value-all-fins (get-value-all-fins fins)
    deflated (deflate-all-values-got valor get-valores-inicio get-value-all-fins inicio fins)
    table (inflacao-all-values-got (deflate-all-values-got 100 get-valores-inicio get-value-all-fins inicio fins) fins)]
  {:chart deflated
  :table table}))
