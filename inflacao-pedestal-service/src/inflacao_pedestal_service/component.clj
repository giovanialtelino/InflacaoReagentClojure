(ns inflacao-pedestal-service.component
  (:require [com.stuartsierra.component :as component]
            [inflacao-pedestal-service.components.routes :as routes]
            [inflacao-pedestal-service.components.servlet :as servlet]
            [inflacao-pedestal-service.components.system-utils :as system-util]
            [inflacao-pedestal-service.components.database :as database]
            [inflacao-pedestal-service.components.pedestal :as service]
            [inflacao-pedestal-service.components.dummy-config :as config]
            [inflacao-pedestal-service.service]))

(def prod-config-map {:env  :prod
                      :port 8081})

(def web-app-deps
  [:config :routes :storage])

(defn base []
  (component/system-map
    :config (config/new-config prod-config-map)
    :storage (database/new-database-pool)
    :routes (routes/new-routes #'inflacao-pedestal-service.service/routes)
    :service (component/using (service/new-service) web-app-deps)
    :servlet (component/using (servlet/new-servlet) [:service])
    ))

(def systems-map
  {:base-system base})

(defn create-and-start-system!
  ([] (create-and-start-system! :base-system))
  ([env] (system-util/bootstrap! systems-map env)))

(defn ensure-system-up! [env]
  (or (deref system-util/system)
      (create-and-start-system! env)))

(defn stop-system! [] (system-util/stop-components!))






