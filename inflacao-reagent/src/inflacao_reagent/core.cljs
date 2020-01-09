(ns inflacao-reagent.core
  (:require
    [inflacao-reagent.xls-page :as xls-page]
    [inflacao-reagent.components :as components]
    [inflacao-reagent.util :as utils]
    [reitit.frontend :as rf]
    [reitit.frontend.easy :as rfe]
    [reitit.coercion.spec :as rss]
    [reagent.core :as r]
    [cljsjs.chartjs]
    [cljsjs.react-table]
    [cljs.core.async :refer [<!]]))

(defn about-page []
  [:div [:h1 "Fonte dos dados para o cálculo"]
   [:p "Os dados utilizados neste site foram retirados de APIs do IPEADATA, todo dia primeiro de cada mês os dados o sistema consulta a API do governo e atualiza os dados num banco de dados interno."]
   [:p "Por essa razão é possível que se um índice foi adicionado no meio do mês, ele só vai ser atualizado e estará disponível para uso na geração do gráfico no próximo mês"]
   [:p "Caso ocorra alguma modificação ou revisão de um índice que já foi adicionado ele não será atualizado automáticamente, só ocorrerá de modo manual, essa é uma das razões que este sistema não deve ser utilizado como 'fonte de verdade' para cálculos de grande importância"]
   [:p "Entretanto não deixa de ser uma maneira rápida e prática de comparar a variação dos valores no decorrer dos anos"]
   [:p "O principal motivo para criação dessa página foi técnico, para experimentar de forma prática alguns métodos de programação, e para os interessados atualizarei essa página, futuramente, com um link para um blog, demonstrando um pouco o desenvolvimento da aplicação"]
   [:p "Já a ideia para essa página surgiu de uma conversa em um grupo no WhatsApp, em que foi citado esse (https://fmeireles.com/blog/rstats/deflacionar-series-no-r-deflatebr/) projeto, que permite deflacionar valores, entretanto para que fosse utilizado a pessoa deveria ter um conhecimento da linguagem de programação R."]])



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
                                                         :height "500px"
                                                         :width  "80%"
                                                         }  } [chart-component-mount]]]
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
     [:a.navbar-item {:href (rfe/href ::xls)} "Dados Utilizados"]
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
     :view xls-page/page}]])

(defn init! []
  (rfe/start!
    (rf/router routes {:data {:coercion rss/coercion}})
    (fn [m] (reset! match m))
    {:use-fragment true})
  (r/render [current-page] (.getElementById js/document "app")))
