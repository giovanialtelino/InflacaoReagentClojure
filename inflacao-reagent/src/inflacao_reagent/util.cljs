(ns inflacao-reagent.util
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]))

(def date-counter 4)

(defn get-valor-inicial []
    (-> js/document
             (.getElementById "valor-inicial")
             (.-value)))

(defn get-year-month-inicial []
  (str   (-> js/document
             (.getElementById "ano-inicial" )
             (.-value))
         "-"
         (-> js/document
             (.getElementById "mes-inicial" )
             (.-value))))

(defn year-month-searcher [i]
  (str   (-> js/document
             (.getElementById (str "ano-" i))
             (.-value))
         "-"
         (-> js/document
             (.getElementById (str "mes-" i))
             (.-value))))

(defn year-month-collector []
  (loop [i 0
         body []]
    (if (< i date-counter )
      (do
        (recur (inc i) (conj body (year-month-searcher i))))
      body)))

(defn send-to-api [mes-ano-diversos valor-inicial mes-ano-inicial]
  (let [body {:valor valor-inicial
                   :inicio mes-ano-inicial
                   :fins mes-ano-diversos}
        json-body (.stringify js/JSON (clj->js body))]
    (println json-body)
    (go (let [response (<! (http/post "http://localhost:8080/graphgen"
                                     {:with-credetials? false
                                      :json-params      body}))]
          (prn (:status response))
          (prn (:body response))))))

