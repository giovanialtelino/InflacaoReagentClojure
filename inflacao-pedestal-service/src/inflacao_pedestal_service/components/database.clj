(ns inflacao-pedestal-service.components.database
  (:require [com.stuartsierra.component :as component]
            [clojure.java.jdbc :as jdbc]
            [hikari-cp.core :as hikari]))

;;Of course you should never use your password like this,
;a decent idea would have those normal files here and them use environ and System to assoc the password value to this map
(def datatasource-dev {:auto-commit        true
                          :read-only          false
                          :connection-timeout 30000
                          :validation-timeout 5000
                          :idle-timeout       600000
                          :max-lifetime       1800000
                          :minimum-idle       10
                          :maximum-pool-size  10
                          :pool-name          "db-pool"
                          :adapter            "postgresql"
                          :username           "docker"
                          :password           "docker"
                          :database-name      "postgres"
                          :server-name        "localhost"
                          :port-number        5432
                          :register-mbeans    false})

(def datatasource-config {:auto-commit        true
                          :read-only          false
                          :connection-timeout 30000
                          :validation-timeout 5000
                          :idle-timeout       600000
                          :max-lifetime       1800000
                          :minimum-idle       10
                          :maximum-pool-size  10
                          :pool-name          "db-pool"
                          :adapter            "postgresql"
                          :username           "postgres"
                          :password           "pedestalTHISisVERT"
                          :database-name      "inflacao"
                          :server-name        "localhost"
                          :port-number        5432
                          :register-mbeans    false})

(defonce datatasource (delay (hikari/make-datasource datatasource-config)))

(defrecord DatabasePoolComponent []
  component/Lifecycle
  (start [this]
    (let [conn {:datasource @datatasource}]
      (assoc this :database conn)))
  (stop [this]
    (assoc this :database nil))
  Object
  (toString [_] "<Database>"))

(defn new-database-pool [] (map->DatabasePoolComponent {}))



