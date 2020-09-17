(ns inflacao-pedestal-service.data-access
  (:require [cheshire.core :refer :all]
            [clj-http.client :as client]
            [inflacao-pedestal-service.database :as database]))

(def links-map {:PRECOS12_IPCA12 "http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='PRECOS12_IPCA12')",
                :IGP12_IGPM12 "http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IGPM12')",
                :IGP12_IGPDI12 "http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IGPDI12')",
                :IGP12_IPC12 "http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IPC12')",
                :PRECOS12_INPC12 "http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='PRECOS12_INPC12')"
                })

(defn get-data [link]
  (client/get link {:accept :json} true))

(defn parse-data [json]
  (:value (parse-string (:body json) true)))

(defn save-data [clean-data]
  (let [vector-size (count clean-data)]
    (loop [i 0]
      (if (< i vector-size)
        (do
          (database/insert-data-inflacao (nth clean-data i))
          (recur (inc i)))))))

(defn access-data []
  (println "new into channel")
   (doseq [kv links-map]
     (if (= true (database/need-to-update? (key kv)))
       (save-data (parse-data (get-data (val kv)))))))


