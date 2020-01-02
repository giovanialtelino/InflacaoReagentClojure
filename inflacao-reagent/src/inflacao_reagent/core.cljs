(ns inflacao-reagent.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
      [reagent.core :as r]
      [cljsjs.d3 :as d3]
      [cljs-http.client :as http]
      [cljs.core.async :refer [<!]]))

(def inflacao-selecionada (r/atom ""))

(defn send-stuff []
  (println "Button was clicked")
  (go (let [response (<! (http/get ""
                                   {:with-credetials? false
                                    :body ""}))]
        (prn (:status response))
        (prn (:body response)))))

;; -------------------------
;; Views
(defn dropdown-selector-mes []
  [:select
   [:option {:value "janeiro"} "Janeiro"]
   [:option {:value "fevereiro"} "Fevereiro"]
   [:option {:value "marco"} "Março"]
   [:option {:value "abril"} "Abril"]
   [:option {:value "maio"} "Maio"]
   [:option {:value "junho"} "Junho"]
   [:option {:value "julho"} "Julho"]
   [:option {:value "agosto"} "Agosto"]
   [:option {:value "setembro"} "Setembro"]
   [:option {:value "outubro"} "Outubro"]
   [:option {:value "novembro"} "Novembro"]
   [:option {:value "dezembro"} "Dezembro"]])

(defn input-selector-ano []
  [:input {:type "number"
           :maxLength "4"
           :min "1979"
           :class "input"
           }])

(defn date-field []
[:div.field.has-addons.has-addons-right
 [:p.control {:class "select"}
  [dropdown-selector-mes]]
 [:p.control
  [input-selector-ano]]
 ])

(defn graph-field []
  [:div.control
   [:button.button.is-link {:on-click send-stuff} "Gerar Gráfico"]]
)

(defn send-button [])

(defn home-page []
    [:div {:class "container"}
     [:h1 {:class "title"} "Calculadora de Inflação"]
     [:div {:class "columns"}
      [:div {:class "column"} [date-field] ]
      [:div {:class "column"} [date-field]]
      [:div {:class "column"} [date-field]]
      [:div {:class "column"} [date-field]]]
     [:div {:class "columns"}
      [:div {:clas "column"} [send-button] ]]
     [:div {:class "columns"}
      [:div {:class "column"} [graph-field]]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
