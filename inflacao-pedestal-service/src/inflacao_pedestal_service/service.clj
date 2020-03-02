(ns inflacao-pedestal-service.service
  (:require [io.pedestal.http :as http]


            [com.stuartsierra.component :as component]))



(def service {:env                     :prod
              ::http/routes            routes
              ::http/resource-path     "/public"
              ::http/type              :jetty
              ::http/port              8080
              ::http/container-options {:h2c? true
                                        :h2?  false
                                        :ssl? false
                                        }})

;https://github.com/ptaoussanis/tufte