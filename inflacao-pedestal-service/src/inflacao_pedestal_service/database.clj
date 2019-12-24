(ns inflacao-pedestal-service.database
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as string]))

(def pg-db {:dbtype "postgresql"
            :dbname "docker"
            :host "localhost"
            :port "32768"
            :user "docker"
            :password "docker"
            :ssl false
            })

(def create-table-precos12_ipca12
  (jdbc/create-table-ddl :precos12_ipca12
                         [[:valdata :date :primary :key]
                           [:valvalor :decimal :not :null]
                           [:nivnome "varchar(32)"]
                           [:tercodigo "varchar(32)"]]))

(def create-table-igp12_igpm12
  (jdbc/create-table-ddl :igp12_igpm12
                         [[:valdata :date :primary :key]
                          [:valvalor :decimal :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))

(def create-table-igp12_igpdi12
  (jdbc/create-table-ddl :igp12_igpdi12
                         [[:valdata :date :primary :key]
                          [:valvalor :decimal :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))

(def create-table-igp12_ipc12
  (jdbc/create-table-ddl :igp12_ipc12
                         [[:valdata :date :primary :key]
                          [:valvalor :decimal :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))

(def create-table-precos12_inpc12
  (jdbc/create-table-ddl :precos12_inpc12
                         [[:valdata :date :primary :key]
                          [:valvalor :decimal :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))

(def last-update
  (jdbc/create-table-ddl :last_update
                         [[:updated :date]]))

(defn init-system []
  (jdbc/db-do-commands pg-db
                       [last-update
                       create-table-precos12_inpc12
                       create-table-igp12_ipc12
                       create-table-igp12_igpdi12
                       create-table-igp12_igpm12
                       create-table-precos12_ipca12]))

(defn restart-system []
  (jdbc/db-do-commands pg-db [
                              (jdbc/drop-table-ddl :last_update)
                              (jdbc/drop-table-ddl :precos12_inpc12)
                              (jdbc/drop-table-ddl :igp12_ipc12)
                              (jdbc/drop-table-ddl :igp12_igpdi12)
                              (jdbc/drop-table-ddl :igp12_igpm12)
                              (jdbc/drop-table-ddl :precos12_ipca12)])
  (init-system))

(defn get-last-update []
  (:max (jdbc/query pg-db ["SELECT MAX (updated) FROM last_update"])))

(defn check-if-date-key-already-exists [table date]
  (let [exist (jdbc/query pg-db ["SELECT valvalor FROM precos12_ipca12 WHERE valdata = ?" date])
        valor (:valvalor exist)]
    (if (nil? valor)
      false
      true)))

(defn insert-data-inflacao [data]
  (let [{:keys [SERCODIGO VALDATA VALVALOR NIVNOME TERCODIGO]} data]
    (if (false? (check-if-date-key-already-exists (string/lower-case SERCODIGO) VALDATA ))
      (jdbc/insert! pg-db (keyword (string/lower-case SERCODIGO))
                  {:valdata VALDATA
                   :valvalor VALVALOR
                   :nivnome NIVNOME
                   :tercodigo TERCODIGO})
      (println "Key was already presented"))))
