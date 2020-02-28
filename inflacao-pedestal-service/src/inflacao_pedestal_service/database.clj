(ns inflacao-pedestal-service.database
  (:require
    [com.stuartsierra.component :as component]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [java-time :as jt]
    [inflacao-pedestal-service.utils :as utils])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(defn- pooled-data-source
  [host dbname user pwd port]
  {:datasource
   (doto (ComboPooledDataSource.)
     (.setDriverClass "org.postgresql.Driver")
     (.setJdbcUrl (str "jdbc:postgresql://" host ":" port "/" dbname))
     (.setUser user)
     (.setPassword pwd))})

(defrecord inflacao-pedestal-database [ds]
  component/Lifecycle
  (start [this]
    (assoc this :ds (pooled-data-source "localhost" "inflacao" "postgres" "pedestalTHISisVERT!" 5432)))
  (stop [this]
    (-> ds :datasource .close)
    (assoc this :ds nil)))

(defn new-db []
  {:db (map->inflacao-pedestal-database {})})

(defn- query-c3p0
  [component statement]
  (jdbc/query (:ds component) statement))

(defn check-if-exists-precos12-ipca12 [component date]
  (let [exist (count (query-c3p0 component ["SELECT valvalor AS valor FROM precos12_ipca12 WHERE valdata::date = ?" date]))]
    (if (> exist 0)
      true
      false)))

(defn check-if-exists-igp12-igpm12 [component date]
  (let [exist (count (query-c3p0 component ["SELECT valvalor AS valor FROM igp12_igpm12 WHERE valdata::date = ?" date]))]
    (if (> exist 0)
      true
      false)))

(defn check-if-exists-igp12-igpdi12 [component date]
  (let [exist (count (query-c3p0 component ["SELECT valvalor AS valor FROM igp12_igpdi12 WHERE valdata::date = ?" date]))]
    (if (> exist 0)
      true
      false)))

(defn check-if-exists-igp12-ipc12 [component date]
  (let [exist (count (query-c3p0 component ["SELECT valvalor AS valor FROM igp12_ipc12 WHERE valdata::date = ?" date]))]
    (if (> exist 0)
      true
      false)))

(defn check-if-exists-precos12-inpc12 [component date]
  (let [exist (count (query-c3p0 component ["SELECT valvalor AS valor FROM precos12_inpc12 WHERE valdata::date = ?" date]))]
    (if (> exist 0)
      true
      false)))

(defn get-last-update []
  (:max (nth (query-c3p0 component ["SELECT MAX (updated) FROM last_update"]) 0)))

(defn update-last-update []
  (jdbc/insert! pg-db :last_update
                {:updated (jt/to-sql-date (jt/local-date-time))}))

(defn check-if-date-key-already-exists [component table date]
  (let [format-date (utils/format-date-to-javatime date)]
    (case table
      "precos12_inpc12" (check-if-exists-precos12-inpc12 component format-date)
      "igp12_ipc12" (check-if-exists-igp12-ipc12 component format-date)
      "igp12_igpdi12" (check-if-exists-igp12-igpdi12 component format-date)
      "igp12_igpm12" (check-if-exists-igp12-igpm12 component format-date)
      "precos12_ipca12" (check-if-exists-precos12-ipca12 component format-date)
      true)))

(defn insert-data-inflacao [component data]
  (let [{:keys [SERCODIGO VALDATA VALVALOR NIVNOME TERCODIGO]} data]
    (if (true? (check-if-date-key-already-exists component (string/lower-case SERCODIGO) VALDATA))
      (println "Key was already presented")
      (jdbc/insert! pg-db (keyword (string/lower-case SERCODIGO))
                    {:valdata   (format-date-to-javatime VALDATA)
                     :valvalor  VALVALOR
                     :nivnome   NIVNOME
                     :tercodigo TERCODIGO}))))

(defn get-value-date-table [component table date]
  (let [
        query (str "SELECT valvalor FROM " table " WHERE valdata =  ? LIMIT 1")
        result (query-c3p0 component [query date])]
    (if (< 0 (count result))
      (:valvalor (nth result 0))
      0
      )))

(defn get-value-date-all-table [component date]
  (let [formated-date (format-date-to-javatime date)
        all-values {:precos12_inpc12 (get-value-date-table component "precos12_inpc12" formated-date)
                    :igp12_ipc12     (get-value-date-table component "igp12_ipc12" formated-date)
                    :igp12_igpdi12   (get-value-date-table component "igp12_igpdi12" formated-date)
                    :igp12_igpm12    (get-value-date-table component "igp12_igpm12" formated-date)
                    :precos12_ipca12 (get-value-date-table component "precos12_ipca12" formated-date)}]
    all-values))

(defn get-value-all-data [component table]
  (let [query (str "SELECT TO_CHAR(valvalor::FLOAT, 'FM999999990.00000000') AS Valor, TO_CHAR(valdata, 'dd/mm/yyyy') AS Data FROM " table " ORDER BY valdata DESC")
        result (query-c3p0 component [query])]
    result
    ))

(defn get-all-use-data [component]
  (let [all-values {:precos12_inpc12 (get-value-all-data component "precos12_inpc12")
                    :igp12_ipc12     (get-value-all-data component "igp12_ipc12")
                    :igp12_igpdi12   (get-value-all-data component "igp12_igpdi12")
                    :igp12_igpm12    (get-value-all-data component "igp12_igpm12")
                    :precos12_ipca12 (get-value-all-data component "precos12_ipca12")
                    :last-update     (jt/format "dd/MM/yyyy" (jt/local-date (get-last-update)))}]
    all-values))

