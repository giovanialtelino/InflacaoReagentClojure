(ns inflacao-pedestal-service.service
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http.body-params :as body-params]
            [inflacao-pedestal-service.deflate :as deflate]
            [inflacao-pedestal-service.database :as database]
            [io.pedestal.http :as bootstrap]
            [cheshire.core :as cs]
            [ring.util.response :as ring]))

(def database-pool (atom nil))

(defn home-page
  [request]
  (ring/content-type (ring/response "Hello World my friend, you took the wrong route, sorry.") "text/plain"))

(defn graph-generator
  [{{:keys [json-params]} :json-params
    {:keys [storage]}     :components}]
  (let [valor (:valor json-params)
        inicio (:inicio json-params)
        fins (:fins json-params)
        graph-table (deflate/generate-graph valor inicio fins storage)]
    (ring/content-type (ring/response graph-table) "application/json")))

(defn xls-generator
  [{{:keys [storage]} :components}]
  (prn "--------------------------------")
  (prn storage)
  (prn (:database storage))
  (prn (:datasource (:database storage)))
  (prn "--------------------------------")
  (let [all-used-data (database/get-all-use-data (:database storage))]
    (ring/content-type (ring/response (cs/generate-string all-used-data)) "application/json")))

(def common-interceptors [(body-params/body-params) bootstrap/html-body])

(def routes #{["/" :get (conj common-interceptors `home-page) :route-name :index]
              ["/graphgen" :post (conj common-interceptors `graph-generator) :route-name :graph]
              ["/xlsgen" :get (conj common-interceptors `xls-generator) :route-name :xls]})