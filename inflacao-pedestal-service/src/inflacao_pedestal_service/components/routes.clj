(ns inflacao-pedestal-service.components.routes
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http.body-params :as body-params]
            [inflacao-pedestal-service.deflate :as deflate]
            [ring.util.response :as ring]))

(def database-pool (atom nil))

(defn home-page
  [request]
  (ring/content-type (ring/response "Hello World my friend, you took the wrong route, sorry.") "text/plain"))

(defn graph-generator
  [request]
  (let [json-params (:json-params request)
        valor (:valor json-params)
        inicio (:inicio json-params)
        fins (:fins json-params)
        graph-table (deflate/generate-graph valor inicio fins database-pool)]
    (ring/content-type (ring/response graph-table) "application/json")))

(defn xls-generator
  [request]
  (let [all-used-data (database/get-all-use-data database-pool)]
    (ring/content-type (ring/response all-used-data) "application/json")))

(def common-interceptors [(body-params/body-params) http/html-body])

(def routes #{["/" :get (conj common-interceptors `home-page) :route-name :index]
              ["/graphgen" :post (conj common-interceptors `graph-generator) :route-name :graph]
              ["/xlsgen" :get (conj common-interceptors `xls-generator) :route-name :xls]})

(defrecord Routes []
  component/Lifecycle
  (start [this]
    (reset! database-pool (:database this))
    (prn @database-pool)
    (assoc this :routes routes))
  (stop [this] (assoc this :routes nil)))

(defn new-routes [] (map->Routes {}))

