(ns inflacao-reagent.util
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]))

(def date-counter 4)

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

(defn send-to-api [body]
  (go (let [response (<! (http/get "https://webhook.site/bea212c0-497c-450a-a6be-361d7258434a"
                                   {:with-credetials? false
                                    :body      body}))]
        (prn (:status response))
        (prn (:body response)))))

