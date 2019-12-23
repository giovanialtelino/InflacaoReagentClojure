(ns inflacao-pedestal-service.data-acess
  (:require [cheshire.core :refer :all]
            [clj-http.client :as client]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.java.jdbc :as jdbc]))

(def pg-db {:dbtype "postgresql"
            :dbname "mypgdatabase"
            :host "mydb.server.com"
            :user "myuser"
            :password "secret"
            :ssl true
            :sslfactory "org.postgresql.ssl.NonValidatingFactory"})

(def links-vector ["http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='PRECOS12_IPCA12')"])

"http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IGPM12')",
"http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IGPDI12')",
"http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IPC12')",
"http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='PRECOS12_INPC12')"

(defn get-data [link]
  (client/get link {:accept :json} true))

(defn parse-data [json]
  (:value (parse-string (:body json) true)))

;;not the best.......
(defn update-date [data]
  (let [data (:VALDATA data)]
    (c/to-sql-date(subs data 0 10))))

(defn update-data [data]
  (let [data-size (count data)]
    (loop [i 0
           updated-data data]
      (if (< i data-size)
      (recur (inc i) (assoc-in updated-data [i :VALDATA] (update-date (nth updated-data i))))
      updated-data))))

(defn check-last-data []

  )



(defn save-data [data]
  (let [last-data check-last-data]
    (if (t/after? {:VALDATA data} last-data)
      )))

(defn access-data []
  (let [vector-size (count links-vector)]
    (loop [i 0]
      (if (< i vector-size)
        ( do (println (update-data(parse-data(get-data (get links-vector i)))))
             (recur (inc i)))))))
