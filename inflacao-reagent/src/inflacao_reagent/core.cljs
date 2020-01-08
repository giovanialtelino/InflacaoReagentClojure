(ns inflacao-reagent.core
  (:require
    [inflacao-reagent.components :as components]
    [inflacao-reagent.util :as utils]
    [reitit.frontend :as rf]
    [reitit.frontend.easy :as rfe]
    [reitit.coercion.spec :as rss]
    [reagent.core :as r]
    [cljsjs.chartjs]
    [cljsjs.react-table]
    [cljs.core.async :refer [<!]]))

(def ReactTable (r/adapt-react-class (aget js/ReactTable "default")))

(defn my-component []
  [ReactTable {:data [1] :columns ["ok"]}])

(defn about-page []
  [:div "Hello about page"])

(defn xls-page [])

(defn chart-component-mount
  []
  (r/create-class
    {:component-did-mount #(utils/chart-component nil)
     :display-name        "chart-component"
     :reagent-render      (fn []
                            [:canvas {:id "rev-chartjs"
                                      }])}))

(defn inflacao-deflacao-page []
  [:div {:class "container is-fluid"}
   [:h1 {:class "title"} "Calculadora de Inflação e Deflação"]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-3"} [components/valor-input]]
    [:div {:class "column is-3"} [components/data-inicial-input]]]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-3"}
     [:label {:class "label"}
      "Datas para serem calculadas"]]]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-3"} [components/date-field 0]]
    [:div {:class "column is-3"} [components/date-field 1]]
    [:div {:class "column is-3"} [components/date-field 2]]
    [:div {:class "column is-3"} [components/date-field 3]]]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-2"} [components/send-button]]]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-12 chart-container" :style {:position "absolute"
                                                         :height "200px"
                                                         :width  "80%"
                                                         }  } [chart-component-mount]]]
   [:div {:class "columns is-centered"}
    [:div {:class "column is-12"} [my-component]]]
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
    {:use-fragment true})
  (r/render [current-page] (.getElementById js/document "app")))
