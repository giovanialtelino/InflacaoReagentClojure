(ns inflacao-pedestal-service.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [cheshire.core :as cs]
            [inflacao-pedestal-service.deflate :as deflate]
            [inflacao-pedestal-service.database :as database]))

(defn home-page
  [request]
  {:stauts 200
   :body "Hello World, I'm working"})

(defn graph-generator
  [request]
  (let [json-params (:json-params request)
        valor (:valor json-params)
        inicio (:inicio json-params)
        fins (:fins json-params)
        d3-graph (deflate/generate-graph valor inicio fins)]
  {:status 200
   :body d3-graph}))

(defn xls-generator
  [request]
  (let [all-used-data (database/get-all-use-data)]
  {:status 200
   :body all-used-data}))

(def common-interceptors [(body-params/body-params) http/html-body])

(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/graphgen" :post (conj common-interceptors `graph-generator)]
              ["/xlsgen" :get (conj common-interceptors `xls-generator)]})

(def service {:env :prod
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false
                                        }})