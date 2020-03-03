(ns inflacao-pedestal-service.components.pedestal
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.interceptor.helpers :refer [before]]
            [io.pedestal.http.route :as route]
            [io.pedestal.http :as bootstrap]))

(defn- add-system [service]
  (before (fn [context] (assoc-in context [:request :components] service))))

(defn system-interceptors
  [service-map service]
  (update-in service-map
             [::bootstrap/interceptors] #(vec (->> % (cons (add-system service))))))

(defn base-prod [routes port]
  {:env                        :prod
   ::bootstrap/routes          #(route/expand-routes (deref routes))
   ::bootstrap/type            ::jetty
   ::bootstrap/secure-headers  {:content-security-policy {:object-src "none"}}
   ::bootstrap/allowed-origins {:creds true :allowed-origins (constantly true)}
   ::bootstrap/port            port})

(defn prod-init [service-map]
  (bootstrap/default-interceptors service-map))

(defn dev-init [service-map]
  (-> service-map
      (merge {:env                        :dev
              ::bootstrap/join?           false
              ::bootstrap/secure-headers  {:content-security-policy {:object-src "none"}}
              ::bootstrap/allowed-origins {:creds true :allowed-origins (constantly true)}
              })
      bootstrap/default-interceptors
      bootstrap/dev-interceptors))

;(system-interceptors service)
(defn runnable-service [config routes service]
  (let [env (:environment config)
        port (:port config)
        service-conf (base-prod routes port)]
    (-> (if (= :prod env)
          (prod-init service-conf)
          (dev-init service-conf))
        (system-interceptors service))))

(defrecord Service [config routes]
  component/Lifecycle
  (start [this]
    (assoc this :runnable-service (runnable-service (:config config) (:routes routes) this)))
  (stop [this]
    (assoc this :runnable-service nil))
  Object
  (toString [_] "<Service>"))

(defn new-service [] (map->Service {}))
