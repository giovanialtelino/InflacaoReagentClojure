(ns inflacao-pedestal-service.component
  (:require [com.stuartsierra.component :as component]
            [inflacao-pedestal-service.components.routes :as routes]
            [inflacao-pedestal-service.components.servlet :as servlet]
            [inflacao-pedestal-service.pedestal :as pedestal]
            [inflacao-pedestal-service.components.database :as database]
            [inflacao-pedestal-service.components.pedestal :as service]
            [inflacao-pedestal-service.components.dummy-config :as config]))

(def prod-config-map {:env  :prod
                      :port 8080})

(def dev-config-map {:env  dev
                     :port 8080})

(def web-app-deps
  [:config :routes :storage])

(defn base []
  (component/system-map
    :config (config/new-config prod-config-map)
    :storage (database/new-database-pool)
    :routes (routes/new-routes )
    :service (component/using (service/new-service) web-app-deps)
    :servlet (component/using (servlet/new-servlet) [:service])
    ))
