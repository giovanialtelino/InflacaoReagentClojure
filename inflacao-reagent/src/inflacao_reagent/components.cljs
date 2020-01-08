(ns inflacao-reagent.components
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [cljsjs.chartjs]
    [reagent.core :as r]
    [inflacao-reagent.util :as utils]
    [inflacao-reagent.chart :as chart]
    [cljs.core.async :refer [<!]]))

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

(defn send-button-func []
 (prn (go (<! (utils/send-button-handler)))))

(defn send-button []
  [:div.control
   [:button.button.is-primary {:on-click  send-button-func}  "Gerar Gráfico e Tabela"]]
  )

