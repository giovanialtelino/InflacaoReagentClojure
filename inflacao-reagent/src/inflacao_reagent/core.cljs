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
  [:div [:div
         [:h1 "Fonte dos dados para o cálculo"]
         [:p "Os dados utilizados neste site foram retirados de APIs do IPEADATA, todo dia primeiro de cada mês os dados o sistema consulta a API do governo e atualiza os dados num banco de dados interno."]
         [:p "Por essa razão é possível que se um índice foi adicionado no meio do mês, ele só vai ser atualizado e estará disponível para uso na geração do gráfico no próximo mês."]
         [:p "Caso ocorra alguma modificação ou revisão de um índice que já foi adicionado ele não será atualizado automáticamente, só ocorrerá de modo manual, essa é uma das razões que este sistema não deve ser utilizado como 'fonte de verdade' para cálculos de grande importância, comparando com uma calculadora do governo também percebi algumas variações em caso de grandes números, acredito que causado por arredondamento das casas decimais."]
         [:p "Entretanto não deixa de ser uma maneira rápida e prática de comparar a variação dos valores no decorrer dos anos."]
         [:p "Só é possível utilizar números inteiros nesta calculadora, assim como os pontos, se forem utilizados, serão desconsiderados na  hora do cálculo."]
         [:p "A calculadora idealmente deve ser utilizada para comparar periodos que utilizem as mesmas moedas, por exemplo cruzeiro com cruzeiro e real com real, no momento ainda não adicionei a função para transformar de cruzeiro para real, por exemplo."]
         [:p "O principal motivo para criação dessa página foi técnico, para experimentar de forma prática alguns métodos de programação, especificamente Clojure, e para os interessados atualizarei essa página, futuramente, com um link para um blog, demonstrando um pouco o desenvolvimento da aplicação."]
         [:p "Já a ideia para essa página surgiu de uma conversa em um grupo no WhatsApp, em que foi citado " [:a {:href "https://fmeireles.com/blog/rstats/deflacionar-series-no-r-deflatebr"} "esse"] " projeto, que permite deflacionar valores, até de maneira mais completa com algumas funções extras, entretanto para que fosse utilizado a pessoa deveria ter um conhecimento da linguagem de programação R, para pessoas que não são da área de TI pode ser uma tarefa um pouco complexa, então resolvi criar essa calculadora, mas fica como recomendação o projeto do Fernando Meireles no link."]]
   [:br]
   [:div
    [:p "Neste momento o site tem os seguintes problemas: "]
    [:ul
     [:li "Desalinhamento em determinadas resoluções de tela"]
     [:li "Não mostrar a moeda do periodo que foi selecionado"]
     [:li "Não apresenta erro quando o índice com os valores não está presente no banco de dados"]
     [:li "Ao gerar dois gráficos sem recarregar a página ele pode apresentar o gráfico anterior ao passar o mouse por cima para ver os dados"]]]])

(defn inflacao-deflacao-page []
  [:div.container
   [:div.title [:h1 "Calculadora de Inflação"]
    [components/valor-input]
    [components/data-inicial-input]]
   [:div.dates
    [:label.label
     "Datas para serem calculadas"]
    [:div.date-selector
     [components/date-field 0]
     [components/date-field 1]
     [components/date-field 2]
     [components/date-field 3]]]
   [:div.calc-button
    [components/send-button]]
   [:div.table [utils/lister-table]]
   [:div#chart-container.chart
    [:canvas {:id "rev-chartjs"
              }]
    ]]
  )

(defn navbar []
  [:nav.navbar.header-fixed {:role "navigation" :aria-label "main navigation"}
   [:div.navbar-brand
    [:a.navbar-item
     ]
    [:a.navbar-burger.burger {:role "button" :aria-label "menu" :aria-expanded "false" :data-target "navbarBasicExample"}
     [:span {:aria-hidden "true"}]
     [:span {:aria-hidden "true"}]
     [:span {:aria-hidden "true"}]]]
   [:div#navbarBasicExample.navbar-menu
    [:div.navbar-start
     [:a.navbar-item {:href (rfe/href ::calculadora)} "Calculadora"]
     [:a.navbar-item {:href (rfe/href ::xls)} "Dados Utilizados"]
     ]
    [:div.navbar-end
     [:a.navbar-item {:href (rfe/href ::sobre)} "Sobre"]
     ]]])

(defn footer []
  [:footer.footer
   [:div.content.has-text-centered
    [:p "Giovani"]]]
  )

(defonce match (r/atom nil))

(defn current-page []
  [:div.wrapper [navbar]
   (if @match
     (let [view (:view (:data @match))]
       [view @match]))
   [footer]])

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
