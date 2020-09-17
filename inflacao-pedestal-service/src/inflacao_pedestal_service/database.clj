(ns inflacao-pedestal-service.database
  (:require
    [hikari-cp.core :as hikari]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as string]
    [java-time :as jt]
    [inflacao-pedestal-service.utils :as utils]
    [clojure.string :as str]))

(def datasource-opts {:auto-commit        true
                      :read-only          false
                      :connection-timeout 30000
                      :validation-timeout 5000
                      :idle-timeout       600000
                      :max-lifetime       1800000
                      :minimum-idle       10
                      :maximum-pool-size  10
                      :pool-name          "inflacao"
                      :adapter            "postgresql"
                      :username           "inflacao"
                      :password           "inflacaopwd"
                      :database-name      "inflacao"
                      :server-name        "localhost"
                      :port-number        5432
                      :register-mbeans    false})

(defonce datasource (hikari/make-datasource datasource-opts))

(defn ds-get
  [ds]
  {:datasource ds})

(defn- pool-query
  ([conn query]
   (jdbc/with-db-connection [pool-conn (ds-get conn)]
                            (let [result (jdbc/query  pool-conn query)]
                              result)))
  ([conn query args]
   (jdbc/with-db-connection [pool-conn (ds-get conn)]
                            (let [result (jdbc/query pool-conn [query args])]
                              result))))

(defn get-index [datasource name]
  (:index_id (first (pool-query datasource "SELECT index_id FROM indexes WHERE index_name = ?" (str/lower-case name)))))

(defn check-if-exists [date table-name]
  (let [exist (count (pool-query datasource ["SELECT inflacao.valvalor AS valor FROM inflacao
                                              LEFT JOIN indexes
                                              ON inflacao.indexname = indexes.index_id
                                              WHERE inflacao.valdata::date = ?
                                              AND indexes.index_name = ?"
                                             date table-name]))]
    (if (> exist 0)
      true
      false)))

(defn check-if-date-key-already-exists [table date]
  (let [format-date (utils/format-date-to-javatime date)
        inpc12 "precos12_inpc12"
        ipc12 "igp12_ipc12"
        igpdi12 "igp12_igpdi12"
        igpm12 "igp12_igpm12"
        ipca12 "precos12_ipca12"]
    (cond
      (= table inpc12) (check-if-exists format-date inpc12)
      (= table ipc12) (check-if-exists format-date ipc12)
      (= table igpdi12) (check-if-exists format-date igpdi12)
      (= table igpm12) (check-if-exists format-date igpm12)
      (= table ipca12) (check-if-exists format-date ipca12)
      :else "error")))

(defn need-to-update? [key]
  (let [low-string-key (str/lower-case (name key))
        last-update (pool-query datasource ["SELECT MAX(valdata) FROM inflacao
                                             LEFT JOIN indexes
                                             ON inflacao.indexname = indexes.index_id
                                             WHERE indexes.index_name = ?" low-string-key])
        date (:max (first last-update))]
    (if (nil? date)
      true
      (if (and (utils/same-month date) (utils/same-year date))
        false
        true
        ))))

(defn insert-data-inflacao [data]
  (let [{:keys [SERCODIGO VALDATA VALVALOR NIVNOME TERCODIGO]} data
        already-added? (check-if-date-key-already-exists (string/lower-case SERCODIGO) VALDATA)]
    (if (= false already-added?)
      (jdbc/insert! (ds-get datasource) :inflacao
                    {:valdata   (utils/format-date-to-javatime VALDATA)
                     :valvalor  VALVALOR
                     :nivnome   NIVNOME
                     :tercodigo TERCODIGO
                     :addedat (jt/sql-date (jt/local-date))
                     :indexname (get-index datasource SERCODIGO) }))))

(defn- get-value-date-table [table date]
  (let [query (str "SELECT valvalor FROM " table " WHERE valdata =  ?")
        result (pool-query @datasource [query date])]
    (if (< 0 (count result))
      (:valvalor (nth result 0))
      0
      )))

(defn get-value-date-all-table [date]
  (let [formated-date (utils/format-date-to-javatime date)
        all-values {:precos12_inpc12 (get-value-date-table "precos12_inpc12" formated-date)
                    :igp12_ipc12     (get-value-date-table "igp12_ipc12" formated-date)
                    :igp12_igpdi12   (get-value-date-table "igp12_igpdi12" formated-date)
                    :igp12_igpm12    (get-value-date-table "igp12_igpm12" formated-date)
                    :precos12_ipca12 (get-value-date-table "precos12_ipca12" formated-date)}]
    all-values))

(defn- get-value-all-data [table]
  (let [query (str "SELECT TO_CHAR(inflacao.valvalor::FLOAT, 'FM999999990.00000000') AS Valor, TO_CHAR(inflacao.valdata, 'dd/mm/yyyy') AS Data, TO_CHAR(inflacao.addedat, 'dd/mm/yyyy') AS AddedAt "
                   "FROM inflacao "
                   "LEFT JOIN indexes "
                   "ON inflacao.indexname = indexes.index_id "
                   "WHERE indexes.index_name = ? "
                   "ORDER BY inflacao.valdata DESC")
        result (pool-query datasource [query table])]
    result))

(defn get-all-data []
      "Return all the data which is in the database"
  (let [all-values {:precos12_inpc12 (get-value-all-data "precos12_inpc12")
                    :igp12_ipc12     (get-value-all-data "igp12_ipc12")
                    :igp12_igpdi12   (get-value-all-data "igp12_igpdi12")
                    :igp12_igpm12    (get-value-all-data "igp12_igpm12")
                    :precos12_ipca12 (get-value-all-data "precos12_ipca12")}]
    all-values))