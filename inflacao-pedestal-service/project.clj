(defproject inflacao-pedestal-service "0.0.2"
  :description "Api for the calculator running on the server"
  :url "https//api-calculadora-inflacao.giovanialtelino.com"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [io.pedestal/pedestal.service "0.5.7"]
                 [io.pedestal/pedestal.jetty "0.5.7"]
                 [clj-http "3.10.0"]
                 [org.clojure/java.jdbc "0.7.10"]
                 [org.postgresql/postgresql "42.2.9"]
                 [com.mchange/c3p0 "0.9.5.2"]
                 [com.stuartsierra/component.repl "0.2.0"]
                 [clojure.java-time "0.3.2"]
                 [com.taoensso/tufte "2.1.0"]
                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [com.stuartsierra/component "0.4.0"]
                 [org.slf4j/jul-to-slf4j "1.7.25"]
                 [org.slf4j/jcl-over-slf4j "1.7.25"]
                 [org.slf4j/log4j-over-slf4j "1.7.25"]
                 [hikari-cp "2.10.0"]
                 [ring/ring-core "1.8.0"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  ;; If you use HTTP/2 or ALPN, use the java-agent to pull in the correct alpn-boot dependency
  ;:java-agents [[org.mortbay.jetty.alpn/jetty-alpn-agent "2.0.5"]]
  ;:main ^{:skip-aot true} inflacao-pedestal-service.server
  :profiles {:dev     {:aliases      {"run-dev" ["trampoline" "run" "-m" "inflacao-pedestal-service.server/run-dev"]}
                       :dependencies [[io.pedestal/pedestal.service-tools "0.5.7"]]}
             :uberjar {:aot [inflacao-pedestal-service.server]}}
  :main inflacao-pedestal-service.server)
