(ns inflacao-pedestal-service.data-acess
  (:require [cheshire.core :refer :all]
            [clj-http.client :as client]
            [java-time :as jt]))

(def links-vector ["http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='PRECOS12_IPCA12')"])

"http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IGPM12')",
"http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IGPDI12')",
"http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IPC12')",
"http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='PRECOS12_INPC12')"

(defn get-data [link]
  (client/get link {:accept :json} true))

(defn parse-data [json]
  (parse-string (:body json) true))

;;not the best.......
(defn update-date [data]
  (subs data 0 7))

(defn update-data [data]
  (println data)
  (into []
        (map
          (update-date ))
  )

(defn access-data []
  (let [vector-size (count links-vector)]
    (loop [i 0]
      (if (< i vector-size)
        ( do (update-data(parse-data(get-data (get links-vector i))))
             (recur (inc i)))))))
