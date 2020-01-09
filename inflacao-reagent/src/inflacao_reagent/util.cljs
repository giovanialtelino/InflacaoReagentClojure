(ns inflacao-reagent.util
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [cljsjs.chartjs]
    [cljs-http.client :as http]
    [cljsjs.react-table]
    [reagent.core :as r]
    [inflacao-reagent.xls-page :as xls]
    [cljs.core.async :refer [<!]]))

(def date-counter 4)

(defn get-valor-inicial []
    (-> js/document
             (.getElementById "valor-inicial")
             (.-value)))

(defn get-year-month-inicial []
  (str   (-> js/document
             (.getElementById "ano-inicial" )
             (.-value))
         "-"
         (-> js/document
             (.getElementById "mes-inicial" )
             (.-value))))

(defn year-month-searcher [i]
  (str   (-> js/document
             (.getElementById (str "ano-" i))
             (.-value))
         "-"
         (-> js/document
             (.getElementById (str "mes-" i))
             (.-value))))

(defn clean-dates [dates]
  (loop [i 0
         clean-dates []]
    (if (< i (count dates))
      (if (= 7 (count (nth dates i)))
        (recur (inc i) (conj clean-dates (nth dates i)))
        (recur (inc i) clean-dates))
      clean-dates)))

(defn process-values-dates [values val-inicio dates index]
  (let [date-counter (count dates)]
    (loop [i 0
           values-cleaned [val-inicio]]
      (if (< i date-counter)
        (recur (inc i) (conj values-cleaned (get-in values [(keyword (nth dates i)) index])))
        values-cleaned))))

(defn year-month-collector []
  (loop [i 0
         body []]
    (if (< i date-counter )
        (recur (inc i) (conj body (year-month-searcher i)))
        body)))

(defn dataset-generator [dates val-inicio values]
  (let [
        precos12_inpc12 (process-values-dates values val-inicio dates :precos12_inpc12)
        igp12_ipc12 (process-values-dates values val-inicio dates :igp12_ipc12)
        igp12_igpdi12  (process-values-dates values val-inicio dates :igp12_igpdi12)
        igp12_igpm12  (process-values-dates values val-inicio dates :igp12_igpm12)
        precos12_ipca12  (process-values-dates values val-inicio dates :precos12_ipca12)]
    [{:data precos12_inpc12
      :label "INPC"
      :borderColor "#332288" :backgroundColor "#332288" :fill "false"
      :order 0}
     {:data precos12_ipca12
      :label "IPCA"
      :borderColor "#DDCC77" :backgroundColor "#DDCC77" :fill "false"
      :order 1}
     {:data igp12_ipc12
      :label "IPC"
      :borderColor "#44AA99" :backgroundColor "#44AA99" :fill "false"
      :order 2}
     {:data igp12_igpdi12
      :label "IGPDI"
      :borderColor "#117733" :backgroundColor "#117733" :fill "false"
      :order 3}
     {:data igp12_igpm12
      :label "IGPM"
      :borderColor "#999933" :backgroundColor "#999933" :fill "false"
      :order 4}
     ]))

(defn chart-component
  [data]
  (let [
        dates (clean-dates (nth data 0))
        dates-inicio (into [] (concat [(nth data 1)] dates))
        dataset (dataset-generator dates (nth data 2) (nth data 3))
        context (.getContext (.getElementById js/document "rev-chartjs") "2d")
        chart-data {:type "line"
                    :options {:responsive true
                              :maintainAspectRatio false
                              :title {:display true
                                      :text "Inflação - Deflação"}
                              :tooltips {:mode "index"
                                         :intersect false}
                              :hover {:mode "nearest"
                                      :intersect true
                                      }
                              }


                    :data {:labels dates-inicio :datasets dataset }}]
    (js/Chart. context (clj->js chart-data))))

(def ReactTable (r/adapt-react-class (aget js/ReactTable "default")))

(defn my-table-component [data]
  (prn data)
  [:table.table [ReactTable {:data data
                             :columns [
                                       {:Header "Hello" :columns[ {:Header "first name"  :accessor "0" }
                                                                 {:Header "second name" :accessor "1" }]}
                                       ]
                             :showPagination false
                             :defaultPageSize 4
                             }]])

(defn send-to-api [mes-ano-diversos valor-inicial mes-ano-inicial]
  (let [body {:valor valor-inicial
              :inicio mes-ano-inicial
              :fins mes-ano-diversos}]
    (go (let [response (<! (http/post "http://localhost:8080/graphgen"
                                      {:with-credetials? false
                                       :json-params      body}))]
          (prn response)
          ;          (my-table [mes-ano-diversos mes-ano-inicial valor-inicial (:body response)])
          (chart-component [mes-ano-diversos mes-ano-inicial valor-inicial (:body response)])))))

(defn send-button-handler []
  (let [year-month (year-month-collector)
        valor-inicial (int (get-valor-inicial))
        mes-ano-inicial (get-year-month-inicial)]
    (if (false? (number? valor-inicial))
      (js/alert "Insira um valor para cálculo")
      (if (false? (= 7 (count mes-ano-inicial)))
        (js/alert "Selecione um mês e ano inicial")
         (send-to-api year-month valor-inicial mes-ano-inicial)))))



