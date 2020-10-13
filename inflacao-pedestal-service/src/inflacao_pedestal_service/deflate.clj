(ns inflacao-pedestal-service.deflate
  (:require [inflacao-pedestal-service.database :as database]
            [java-time :as jt]))

(defn- check-if-is-whitin [date min max]
  (let [day (jt/days 1)
        min-adj (jt/minus min day)
        max-adj (jt/plus max day)]
    (if (and (jt/after? date min-adj) (jt/after? max-adj date))
      true
      false)))

(defn- front-end-date-parser [unparsed-date]
  (str unparsed-date "-01"))

(defn- retract-one-month-string [date]
  (str (jt/minus (jt/local-date (front-end-date-parser date)) (jt/months 1))))

(defn- cruzeiro-para-cruzado [valor]
  (/ valor 1000))

(defn- cruzado-para-cruzadonovo [valor]
  (/ valor 1000))

(defn- cruzadonovo-para-cruzeiro [valor]
  valor)

(defn- cruzeiro-para-cruzeiroreal [valor]
  (/ valor 1000))

(defn- cruzeiroreal-para-real [valor]
  (/ valor 2750))

(defn deflate [index-1 index-2 value]
  (if (> index-1 0)
    (* (with-precision 10 (/ index-2 index-1)) value)
    0))

(defn moeda-info [data]
  (let [local-date (jt/local-date data)]
    (if (check-if-is-whitin local-date (jt/local-date 1970 5 15) (jt/local-date 1986 2 27))
      {:moeda "Cr$" :inside 0 :limit-date (jt/local-date 1986 2 1)}
      (if (check-if-is-whitin local-date (jt/local-date 1986 2 28) (jt/local-date 1989 1 15))
        {:moeda "Cz$" :inside 1 :limit-date (jt/local-date 1989 1 01)}
        (if (check-if-is-whitin local-date (jt/local-date 1989 1 16) (jt/local-date 1990 3 15))
          {:moeda "NCz$" :inside 2 :limit-date (jt/local-date 1990 2 1)}
          (if (check-if-is-whitin local-date (jt/local-date 1990 3 16) (jt/local-date 1993 7 30))
            {:moeda "Cr$" :inside 3 :limit-date (jt/local-date 1993 7 01)}
            (if (check-if-is-whitin local-date (jt/local-date 1993 8 1) (jt/local-date 1994 6 30))
              {:moeda "CR$" :inside 4 :limit-date (jt/local-date 1994 6 1)}
              (if (check-if-is-whitin local-date (jt/local-date 1994 7 1) (jt/local-date 2099 1 1))
                {:moeda "R$" :inside 5 :limit-date (jt/local-date 2099 1 1)}))))))))

(defn deflate-one-full-date [valor valores-inicio data-inicio data-fim]
  (let [data-key (keyword data-fim)
        precos12_inpc12 (keyword "precos12_inpc12")
        igp12_ipc12 (keyword "igp12_ipc12")
        igp12_igpdi12 (keyword "igp12_igpdi12")
        igp12_igpm12 (keyword "igp12_igpm12")
        precos12_ipca12 (keyword "precos12_ipca12")
        moeda (keyword "moeda")
        moeda-inicio (moeda-info (front-end-date-parser data-inicio))
        moeda-fim (moeda-info (front-end-date-parser data-fim))]
    (if (= (:inside moeda-inicio) (:inside moeda-fim))
      (let [valores-fim (database/get-value-date-all-table (front-end-date-parser data-fim))]
        {data-key {
                   moeda           (:moeda moeda-fim)
                   precos12_inpc12 (deflate (precos12_inpc12 valores-inicio) (precos12_inpc12 valores-fim) valor)
                   igp12_ipc12     (deflate (igp12_ipc12 valores-inicio) (igp12_ipc12 valores-fim) valor)
                   igp12_igpdi12   (deflate (igp12_igpdi12 valores-inicio) (igp12_igpdi12 valores-fim) valor)
                   igp12_igpm12    (deflate (igp12_igpm12 valores-inicio) (igp12_igpm12 valores-fim) valor)
                   precos12_ipca12 (deflate (precos12_ipca12 valores-inicio) (precos12_ipca12 valores-fim) valor)
                   }}
        )
      (let [valores-fim (database/get-value-date-all-table (front-end-date-parser data-fim))]
        {data-key {
                   moeda           (:moeda moeda-fim)
                   precos12_inpc12 (deflate (precos12_inpc12 valores-inicio) (precos12_inpc12 valores-fim) valor)
                   igp12_ipc12     (deflate (igp12_ipc12 valores-inicio) (igp12_ipc12 valores-fim) valor)
                   igp12_igpdi12   (deflate (igp12_igpdi12 valores-inicio) (igp12_igpdi12 valores-fim) valor)
                   igp12_igpm12    (deflate (igp12_igpm12 valores-inicio) (igp12_igpm12 valores-fim) valor)
                   precos12_ipca12 (deflate (precos12_ipca12 valores-inicio) (precos12_ipca12 valores-fim) valor)
                   }}
        ))))

(defn deflate-all-values [valor valores-inicio inicio fins]
  (let [final-count (dec (count fins))]
    (loop [i 0
           deflated {}]
      (if (< i final-count)
        (recur (inc i) (conj deflated (deflate-one-full-date valor ((keyword inicio) valores-inicio) inicio (nth fins i))))
        deflated))))

(defn inflacao-all-values-got [deflated datas]
  (loop [i 0
         cleaned deflated]
    (if (< i (dec (count datas)))
      (let [date (nth datas i)
            date-keyword (keyword date)
            new-values {:moeda           (get-in deflated [date-keyword :moeda])
                        :precos12_ipca12 (- (get-in deflated [date-keyword :precos12_ipca12]) 100)
                        :precos12_inpc12 (- (get-in deflated [date-keyword :precos12_inpc12]) 100)
                        :igp12_ipc12     (- (get-in deflated [date-keyword :igp12_ipc12]) 100)
                        :igp12_igpm12    (- (get-in deflated [date-keyword :igp12_igpm12]) 100)
                        :igp12_igpdi12   (- (get-in deflated [date-keyword :igp12_igpdi12]) 100)}]
        (recur (inc i) (assoc cleaned date-keyword new-values)))
      cleaned)))

(defn generate-graph [valor inicio fins]
  (let [inicio-minus-one (retract-one-month-string inicio)
        get-valores-inicio {(keyword inicio) (database/get-value-date-all-table inicio-minus-one)}]
    {:chart (deflate-all-values valor get-valores-inicio inicio fins)
     :table (inflacao-all-values-got (deflate-all-values 100 get-valores-inicio inicio fins) fins)}))
