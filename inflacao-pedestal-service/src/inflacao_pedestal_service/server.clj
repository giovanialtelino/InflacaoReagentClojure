(ns inflacao-pedestal-service.server
  (:gen-class)                                              ; for -main method in uberjar
  (:require [inflacao-pedestal-service.component :as components]))

(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nCreating your [DEV] server...")
  (components/create-and-start-system!))

(defn -main
  [& args]
  (println "\nCreating your [PROD] server...")
  (components/create-and-start-system!))
