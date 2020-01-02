(ns inflacao-pedestal-service.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [inflacao-pedestal-service.deflate :as deflate]))

(defn home-page
  [request]
  {:stauts 200
   :body "Hello World, I'm working"})

(defn graph-generator
  [request]
  (let [json-params (:json-params request)
        response-body (deflate/generate-graph json-params)]
  {:status 200
   :body response-body}))

(defn xls-generator
  [request]
  {:status 200
   :body "okay"})

(def common-interceptors [(body-params/body-params) http/html-body])

(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/graphgen" :post (conj common-interceptors `graph-generator)]
              ["/xlsgen" :post (conj common-interceptors `xls-generator)]})

(def service {:env :prod
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false
                                        }})