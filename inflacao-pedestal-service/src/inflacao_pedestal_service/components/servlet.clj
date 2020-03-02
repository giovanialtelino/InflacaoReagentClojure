(ns inflacao-pedestal-service.components.servlet
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as pedestal]
            [io.pedestal.service-tools.dev :as dev]))

(defrecord Servlet [service]
  component/Lifecycle
  (start [this]
    (assoc this :instance (-> service
                              :runnable-service
                              (assoc ::bootstrap/join? false)
                              pedestal/create-server
                              pedestal/start)))
  (stop [this]
    (pedestal/stop (:instance this))
    (assoc this :instance nil))

  Object
  (toString [_] "<Servlet>"))

(defn new-servlet [] (map->Servlet {}))

