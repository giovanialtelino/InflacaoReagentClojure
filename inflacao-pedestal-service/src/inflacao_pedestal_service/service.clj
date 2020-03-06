(ns inflacao-pedestal-service.service
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http.body-params :as body-params]
            [inflacao-pedestal-service.deflate :as deflate]
            [inflacao-pedestal-service.database :as database]
            [io.pedestal.http :as bootstrap]
            [cheshire.core :as cs]
            [ring.util.response :as ring]))

(defn home-page
  [request]
  (ring/content-type (ring/response "Hello World my friend, you took the wrong route, sorry.") "text/plain"))

(defn graph-generator
  [{{:keys [valor inicio fins]} :json-params
    {:keys [storage]}           :components}]
  (ring/content-type
    (ring/response
      (cs/generate-string
        (deflate/generate-graph valor inicio fins (:database storage))))
    "application/json"))

(defn xls-generator
  [{{:keys [storage]} :components}]
  (ring/content-type
    (ring/response
      (cs/generate-string
        (database/get-all-use-data (:database storage))))
    "application/json"))

(def common-interceptors [(body-params/body-params) bootstrap/html-body])

(def routes #{["/" :get (conj common-interceptors `home-page) :route-name :index]
              ["/graphgen" :post (conj common-interceptors `graph-generator) :route-name :graph]
              ["/xlsgen" :get (conj common-interceptors `xls-generator) :route-name :xls]})