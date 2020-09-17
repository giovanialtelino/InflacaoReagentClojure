(ns inflacao-pedestal-service.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [inflacao-pedestal-service.deflate :as deflate]
            [inflacao-pedestal-service.database :as database]
            [taoensso.tufte :as tufte]))

(tufte/add-basic-println-handler! {})

(defn home-page
  [request]
  {:stauts 200
   :type "html"
   :body   "Hello World my friend, you took the wrong route, sorry."})

(defn graph-generator
  [request]
    (let [ json-params  (:json-params request)
         valor  (:valor json-params)
         inicio  (:inicio json-params)
         fins  (:fins json-params)
         graph-table (deflate/generate-graph valor inicio fins)]
    {:status 200
     :body   graph-table}))

(defn xls-generator
  [request]
  (let [all-used-data (database/get-all-use-data)]
    {:status 200
     :body   all-used-data}))

(def common-interceptors [(body-params/body-params) http/html-body])

(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/graphgen" :post (conj common-interceptors `graph-generator)]
              ["/xlsgen" :get (conj common-interceptors `xls-generator)]})

(def service {:env                     :prod
              ::http/routes            routes
              ::http/resource-path     "/public"
              ::http/type              :jetty
              ::http/port              8080
              ::http/container-options {:h2c? true
                                        :h2?  false
                                        :ssl? false
                                        }})
