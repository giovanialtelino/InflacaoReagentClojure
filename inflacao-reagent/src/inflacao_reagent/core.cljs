(ns inflacao-reagent.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.core :as r]
    [cljsjs.d3 :as d3]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [cljs-time.core :as dt]))

(def date-counter 4)

(defn month-year-collector [i]
       (str   (-> js/document
              (.getElementById (str "ano-" i))
              (.-value))
            "-"
          (-> js/document
              (.getElementById (str "mes-" i))
              (.-value))))

(defn send-stuff []
    (loop [i 0
           body []]
      (if (< i date-counter )
        (do (println (month-year-collector i))
            (recur (inc i) (conj body (month-year-collector i))))
        body)))

(defn format-organize-body [body]
  (loop [i (count body)
         format-body []]
    (if (= 7 (count (nth i body)))
      (recur (inc i)))))

(defn temp []
  (go (let [response (<! (http/get "http://localhost:8080/"
                                   {:with-credetials? false
                                    :body             ""}))]
        (prn (:status response))
        (prn (:body response)))))

;; -------------------------
;; Views
(defn dropdown-selector-mes [id]
  [:select
   {:id (str "mes-" id)}
   [:option {:value "01"} "Janeiro"]
   [:option {:value "02"} "Fevereiro"]
   [:option {:value "03"} "Março"]
   [:option {:value "04"} "Abril"]
   [:option {:value "05"} "Maio"]
   [:option {:value "06"} "Junho"]
   [:option {:value "07"} "Julho"]
   [:option {:value "08"} "Agosto"]
   [:option {:value "09"} "Setembro"]
   [:option {:value "10"} "Outubro"]
   [:option {:value "11"} "Novembro"]
   [:option {:value "12"} "Dezembro"]
   ])

(defn input-selector-ano [id]
  [:input {:type      "number"
           :maxLength "4"
           :min       "1979"
           :class     "input"
           :id        (str "ano-" id)
           }])

(defn date-field [id]
  [:div.field.has-addons.has-addons-right
   [:p.control {:class "select"}
    [dropdown-selector-mes id]]
   [:p.control
    [input-selector-ano id]]
   ])

(defn send-button []
  [:div.control
   [:button.button.is-link {:on-click format-organize-body send-stuff} "Gerar Gráfico"]]
  )

(defn graph-field [])

(defn home-page []
  [:div {:class "container"}
   [:h1 {:class "title"} "Calculadora de Inflação"]
   [:div {:class "columns"}
    [:div {:class "column"} [date-field 0]]
    [:div {:class "column"} [date-field 1]]
    [:div {:class "column"} [date-field 2]]
    [:div {:class "column"} [date-field 3]]]
   [:div {:class "columns"}
    [:div {:clas "column"} [send-button]]]
   [:div {:class "columns"}
    [:div {:class "column"} [graph-field]]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
