(ns inflacao-pedestal-service.server
  (:gen-class)                                              ; for -main method in uberjar
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [inflacao-pedestal-service.service :as service]))

(defonce runnable-service (http/create-server service/service))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server...")
  (http/start runnable-service))

