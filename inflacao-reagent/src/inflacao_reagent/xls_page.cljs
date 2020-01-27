(ns inflacao-reagent.xls-page
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.core :as r]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]))

(def precos12_inpc12 (r/atom [{:valor 0 :data 0}]))
(def igp12_ipc12 (r/atom [{:valor 0 :data 0}]))
(def igp12_igpdi12 (r/atom [{:valor 0 :data 0}]))
(def igp12_igpm12 (r/atom [{:valor 0 :data 0}]))
(def precos12_ipca12 (r/atom [{:valor 0 :data 0}]))
(def last-update (r/atom []))

(defn last-update-valores []
  [:h2 (str "Ultima atualização dos índices foi efetuada em " @last-update)])

(defn lister [items]
  [:tbody
   (for [item items]
     ^{:key (:data item)} [:tr [:td (:data item)] [:td (:valor item)]])])

(defn lister-user [table-name table]
  [:div {:key table-name :id table-name}
   [:table.table-xls
    [:thead [:tr [:th {:colSpan 2 :style {:text-align "center"}} table-name]]]
    [lister @table]]])

(defn update-atoms []
  (go (let [response (<! (http/get "https://api-calculadora-inflacao.giovanialtelino.com/xlsgen"
                                   {:with-credetials? false
                                    }))
            body (:body response)]
        (reset! precos12_inpc12 (:precos12_inpc12 body))
        (reset! precos12_ipca12 (:precos12_ipca12 body))
        (reset! igp12_ipc12 (:igp12_ipc12 body))
        (reset! igp12_igpdi12 (:igp12_igpdi12 body))
        (reset! igp12_igpm12 (:igp12_igpm12 body))
        (reset! last-update (:last-update body))
        )))

(defn page []
  (update-atoms)
  [:div.container
   [:div.al-ct [:h1 "Dados utilizados para cálculo dos gráficos"]]
   [:div.al-ct [:p "Para entender da onde os dados foram retirados e mais sobre o sistema acesse a aba Sobre"]]
   [:div.al-ct [last-update-valores]]
   [:div.table-wrapper
    [lister-user "INPC" precos12_inpc12]
    [lister-user "IPCA" precos12_ipca12]
    [lister-user "IPC" igp12_ipc12]
    [lister-user "IGPDI" igp12_igpdi12]
    [lister-user "IGPM" igp12_igpm12]]]
  )
