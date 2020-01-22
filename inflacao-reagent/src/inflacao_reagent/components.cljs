(ns inflacao-reagent.components
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [cljsjs.chartjs]
    [reagent.core :as r]
    [inflacao-reagent.util :as utils]
    [cljs.core.async :refer [<!]]
    [cljs-time.core :as tt]))

(def moeda (r/atom "Valor inteiro inicial para cálculo "))

(defn dropdown-selector-mes [id]
  [:div.select-style
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
    ]])

(defn update-moeda [ano]
  (if (= 4 (count ano))
    (let [ano-n (int ano)
          mes-n (int (-> js/document
                         (.getElementById "mes-inicial")
                         (.-value)))
          data (tt/local-date ano-n mes-n 1)]
      (if (tt/within? (tt/interval (tt/local-date 1970 5 15) (tt/local-date 1986 2 27))
                      data) (reset! moeda "Valor inteiro inicial para cálculo (Cr$)"))
      (if (tt/within? (tt/interval (tt/local-date 1986 2 28) (tt/local-date 1989 1 15))
                      data) (reset! moeda "Valor inteiro inicial para cálculo (Cz$)"))
      (if (tt/within? (tt/interval (tt/local-date 1989 1 16) (tt/local-date 1990 3 15))
                      data) (reset! moeda "Valor inteiro inicial para cálculo (NCz$)"))
      (if (tt/within? (tt/interval (tt/local-date 1990 3 16) (tt/local-date 1993 7 30))
                      data) (reset! moeda "Valor inteiro inicial para cálculo (Cr$)"))
      (if (tt/within? (tt/interval (tt/local-date 1993 8 1) (tt/local-date 1994 6 30))
                      data) (reset! moeda "Valor inteiro inicial para cálculo (CR$)"))
      (if (tt/within? (tt/interval (tt/local-date 1994 7 1) (tt/local-date 2099 1 1))
                      data) (reset! moeda "Valor inteiro inicial para cálculo (R$)"))
      )))

(defn check-change-ano-selector-valid [ano new-ano id]
  (if (= 0 (count new-ano))
    (do
      (reset! moeda "Valor inteiro inicial para cálculo ")
      (reset! ano nil))
    (do
      (if (false? (< 4 (count new-ano)))
        (do
          (reset! ano new-ano)
          (if (= id "ano-inicial")
            (update-moeda new-ano)))))))

(defn check-change-valor-inicial-valid [val new-val]
  (if (= 0 (count new-val))
    (reset! val nil)
    (reset! val new-val)))

(defn input-selector-ano [id date-value]
  [:input {:type        "number"
                                 :maxLength   "4"
                                 :min         "1979"
                                 :class       "input"
                                 :id          (str "ano-" id)
                                 :placeholder "Ano"
                                 :value       @date-value
                                 :on-change   #(check-change-ano-selector-valid date-value (-> % .-target .-value) (-> % .-target .-id))
                                 }])

(defn atom-input [val]
  [:input {:type        "number"
           :value       @val
           :min         "1"
           :class       "input-number"
           :id          "valor-inicial"
           :on-change   #(check-change-valor-inicial-valid val (-> % .-target .-value))
           :placeholder "Valor Inteiro"}])

(defn valor-input []
  (let
    [val (r/atom 1, 00)]
    (fn []
      [:div.al-ct
       [:label @moeda]
       [:div [atom-input val]]
       ])))

(defn date-field [id]
  (let [date (r/atom nil)]

     [:div.date-field
      [dropdown-selector-mes id]
      [input-selector-ano id date]
      ]))

(defn data-inicial-input []
  [:div.al-ct
   [:label "Data Inicial"]
   [date-field "inicial"]])

(defn send-button-func []
  (utils/send-button-handler))

(defn send-button []
  [:div
   [:button.button
    {:on-click send-button-func} "Gerar Gráfico E Tabela"]]
  )

