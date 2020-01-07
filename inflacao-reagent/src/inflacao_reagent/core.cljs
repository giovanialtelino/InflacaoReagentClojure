(ns inflacao-reagent.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [inflacao-reagent.util :as utils]
    [reitit.frontend :as rf]
    [reitit.coercion :as rc]
    [reitit.frontend.easy :as rfe]
    [reitit.coercion.spec :as rss]
    [reagent.core :as r]
    [cljs-http.client :as http]
    [fipp.edn :as fedn]
    [cljsjs.chartjs]
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

(defn clean-dates [dates]
  (loop [i 0
         clean-dates []]
    (if (< i (count dates))
     (if (= 7 (count (nth dates i)))
      (recur (inc i) (conj clean-dates (nth dates i)))
      (recur (inc i) clean-dates))
       clean-dates)))

(defn process-values-dates [values val-inicio dates index]
  (prn values)
  (prn val-inicio)
  (prn dates)
  (prn index)
  (let [date-counter (count dates)]
    (loop [i 0
           values-cleaned [val-inicio]]
      (if (< i date-counter)
        (recur (inc i) (conj values-cleaned (get-in values [(keyword (nth dates i)) index])))
        values-cleaned))))

(defn dataset-generator [dates val-inicio values]
  (let [
        precos12_inpc12 (process-values-dates values val-inicio dates :precos12_inpc12)
        igp12_ipc12 (process-values-dates values val-inicio dates :igp12_ipc12)
        igp12_igpdi12  (process-values-dates values val-inicio dates :igp12_igpdi12)
        igp12_igpm12  (process-values-dates values val-inicio dates :igp12_igpm12)
        precos12_ipca12  (process-values-dates values val-inicio dates :precos12_ipca12)]
    (prn precos12_inpc12)
  [{:data precos12_inpc12
   :label "INPC"
   :borderColor "#E1F5FE" :backgroundColor "#E1F5FE" :fill "false"
    :order 0}
   {:data precos12_ipca12
    :label "IPCA"
    :borderColor "#039BE5" :backgroundColor "#039BE5" :fill "false"
    :order 1}
  {:data igp12_ipc12
   :label "IPC"
   :borderColor "#81D4FA" :backgroundColor "#81D4FA" :fill "false"
   :order 2}
  {:data igp12_igpdi12
   :label "IGPDI"
   :borderColor "#29B6F6" :backgroundColor "#29B6F6" :fill "false"
   :order 3}
  {:data igp12_igpm12
   :label "IGPM"
   :borderColor "#0277BD" :backgroundColor "#0277BD" :fill "false"
   :order 4}
  ]))

(defn show-chart
  [data]
   (let [
          dates (clean-dates (nth data 0))
          dates-inicio (into [] (concat [(nth data 1)] dates))
          dataset (dataset-generator dates (nth data 2) (nth data 3))
          context (.getContext (.getElementById js/document "rev-chartjs") "2d")
          chart-data {:type "line"
                      :responsive "true"
                       :data {:labels dates-inicio :datasets dataset }}]
    (js/Chart. context (clj->js chart-data))))

(defn send-button-handler []
  (let [year-month (utils/year-month-collector)
        valor-inicial (int (utils/get-valor-inicial))
        mes-ano-inicial (utils/get-year-month-inicial)]
    (if (false? (number? valor-inicial))
      (js/alert "Insira um valor para cálculo")
      (if (false? (= 7 (count mes-ano-inicial)))
        (js/alert "Selecione um mês e ano inicial")
        (utils/send-to-api year-month valor-inicial mes-ano-inicial)))))

(defn send-button []
  [:div.control
   [:button.button.is-primary {:on-click send-button-handler} "Gerar Gráfico e Tabela"]]
  )

(defn rev-chartjs-component
  []
  (r/create-class
    {:component-did-mount #(show-chart nil)
     :display-name        "chartjs-component"
     :reagent-render      (fn []
                            [:canvas {:id "rev-chartjs" :width "700" :height "250"
                                      }])}))

(defn about-page []
  [:div "Hello about page"])

(defn xls-page [])

(defn inflacao-deflacao-page []
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
    [:div {:class "column is-12"} [rev-chartjs-component]]]
   ]
  )

(defn navbar []
  [:nav.navbar.is-fixed-top {:role "navigation" :aria-label "main navigation"}
   [:div.navbar-brand
    [:a.navbar-item
     ]
    [:a.navbar-burger.burger {:role "button" :aria-label "menu" :aria-expanded "false" :data-target "navbarBasicExample"}
     [:span {:aria-hidden "true"}]
     [:span {:aria-hidden "true"}]
     [:span {:aria-hidden "true"}]]]
   [:div#navbarBasicExample.navbar-menu
    [:div.navbar-start
     [:a.navbar-item  {:href (rfe/href ::calculadora)} "Calculadora"]
     [:a.navbar-item {:href (rfe/href ::xls)} "Planilhas e CSVs"]
    ]
    [:div.navbar-end
     [:a.navbar-item {:href (rfe/href ::sobre)} "Sobre"]
    ]]])

(defonce match (r/atom nil))

(defn current-page []
  [:div [navbar]
   (if @match
     (let [view (:view (:data @match))]
       [view @match]))
   ])

(def routes
  [["/"
    {:name ::calculadora
     :view inflacao-deflacao-page}]
   ["/sobre"
    {:name ::sobre
     :view about-page}]
   ["/xls"
    {:name ::xls
     :view xls-page}]])

(defn init! []
  (rfe/start!
    (rf/router routes {:data {:coercion rss/coercion}})
    (fn [m] (reset! match m))
    ;; set to false to enable HistoryAPI
    {:use-fragment true})
  (r/render [current-page] (.getElementById js/document "app")))
