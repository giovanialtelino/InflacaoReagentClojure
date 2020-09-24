(ns inflacao-pedestal-service.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [inflacao-pedestal-service.deflate :as deflate]
            [ring.util.response :as ring-resp]
            [cheshire.core :as cs]
            [inflacao-pedestal-service.database :as database]
            [clojure.core]
            [inflacao-pedestal-service.data-access :as da]
            [clojure.core.async :as async]))

;Would be better to keep the "last update" date in a atom at runtime, so there would be no need to query the database at all the times
;the thread "update-data" could return a new date, that would update that atom.
(def update-data (async/chan (async/dropping-buffer 1)))

(defn graph-generator
  [request]
  (let [json-params  (:json-params request)
        valor  (:valor json-params)
        inicio  (:inicio json-params)
        fins  (:fins json-params)
        graph-table (deflate/generate-graph valor inicio fins)]
    (ring-resp/content-type
    (ring-resp/response (cs/generate-string graph-table) )
    "application/json")))

(defn xls-generator
  [request]
  (let [update (future (da/access-data))
        all-used-data (database/get-all-data)]
    (ring-resp/content-type
      (ring-resp/response (cs/generate-string all-used-data) )
      "application/json")))

(def common-interceptors [(body-params/body-params) http/html-body])

(def routes #{["/graphgen" :post (conj common-interceptors `graph-generator)]
              ["/xlsgen" :get (conj common-interceptors `xls-generator)]})

(def service {:env                     :prod
              ::http/routes            routes
              ::http/resource-path     "/public"
              ::http/type              :jetty
              ::http/allowed-origins  (constantly true)
              ::http/port              8080
              ::http/container-options {:h2c? true
                                        :h2?  false
                                        :ssl? false}})