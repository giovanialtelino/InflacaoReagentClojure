(ns inflacao-reagent.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.core :as r]
    [cljsjs.d3 :as d3]
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

;http post the data to the api
;return json and print to graph

(defn send-to-api [body]
   (go (let [response (<! (http/get "https://webhook.site/bea212c0-497c-450a-a6be-361d7258434a"
                                   {:with-credetials? false
                                    :body      body}))]
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
           :placeholder "Ano"
           }])

(defn valor-input []
  [:div {:class "field"}
   [:label {:class "label"} "Valor inicial para cálculo"]
   [:input {:type "number"
           :min "1"
           :step "any"
           :class "input"
           :id "valor-inicial"
           :placeholder "Valor para cálculo"}]])

(defn date-field [id]
  [:div.field.has-addons.has-addons-center
   [:p.control {:class "select"}
    [dropdown-selector-mes id]]
   [:p.control
    [input-selector-ano id]]
   ])

(defn data-inicial-input []
  [:div {:class "field"}
   [:label {:class "label"} "Data Inicial"]
   [date-field "inicial"]])

(defn send-button-handler []
  (let [year-month (year-month-collector)]
    (send-to-api year-month)))

(defn send-button []
  [:div.control
   [:button.button.is-primary {:on-click send-button-handler} "Gerar Gráfico e Tabela"]]
  )

(defn graph-field [])

(defn home-page []
  [:div {:class "container is-fluid"}
   [:h1 {:class "title"} "Calculadora de Inflação e Deflação"]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-3"} [valor-input]]
    [:div {:class "column is-3"} [data-inicial-input]]]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-3"}
     [:label {:class "label"}
      "Datas para serem calculadas"]]]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-3"} [date-field 0]]
    [:div {:class "column is-3"} [date-field 1]]
    [:div {:class "column is-3"} [date-field 2]]
    [:div {:class "column is-3"} [date-field 3]]]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-2"} [send-button]]]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-12"} [graph-field]]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
