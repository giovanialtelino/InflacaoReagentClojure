(ns inflacao-pedestal-service.database
  (:require
    [clojure.java.jdbc :as jdbc]
    [clj-time.core :as t]
    [clj-time.coerce :as c]
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

(defn check-if-exists-precos12-ipca12 [date]
  (let [exist (count (jdbc/query pg-db ["SELECT valvalor AS valor FROM precos12_ipca12 WHERE valdata = ?" date]) )]
    (if (> exist 0)
      true
      false)))

(def create-table-igp12_igpm12
  (jdbc/create-table-ddl :igp12_igpm12
                         [[:valdata :date :primary :key]
                          [:valvalor :decimal :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))

(defn check-if-exists-igp12-igpm12 [date]
  (let [exist (count (jdbc/query pg-db ["SELECT valvalor AS valor FROM igp12_igpm12 WHERE valdata = ?" date]) )]
    (if (> exist 0)
      true
      false)))

(def create-table-igp12_igpdi12
  (jdbc/create-table-ddl :igp12_igpdi12
                         [[:valdata :date :primary :key]
                          [:valvalor :decimal :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))

(defn check-if-exists-igp12-igpdi12 [date]
  (let [exist (count (jdbc/query pg-db ["SELECT valvalor AS valor FROM igp12_igpdi12 WHERE valdata = ?" date]) )]
    (if (> exist 0)
      true
      false)))

(def create-table-igp12_ipc12
  (jdbc/create-table-ddl :igp12_ipc12
                         [[:valdata :date :primary :key]
                          [:valvalor :decimal :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))

(defn check-if-exists-igp12-ipc12 [date]
  (let [exist (count (jdbc/query pg-db ["SELECT valvalor AS valor FROM igp12_ipc12 WHERE valdata = ?" date]) )]
    (if (> exist 0)
      true
      false)))

(def create-table-precos12_inpc12
  (jdbc/create-table-ddl :precos12_inpc12
                         [[:valdata :date :primary :key]
                          [:valvalor :decimal :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))

(defn check-if-exists-precos12-inpc12 [date]
  (let [exist (count (jdbc/query pg-db ["SELECT valvalor AS valor FROM precos12_inpc12 WHERE valdata = ?" date]) )]
    (if (> exist 0)
      true
      false)))

(def last-update
  (jdbc/create-table-ddl :last_update
                         [[:id :serial :primary :key]
                          [:updated :date]]))

(defn initialize-first-update []
  (let [date  (c/to-sql-date (t/date-time 1990 1 1))]
    (println date)
    (jdbc/insert! pg-db :last_update
                  {:updated date})))

(defn init-system []
  (jdbc/db-do-commands pg-db
                       [last-update
                       create-table-precos12_inpc12
                       create-table-igp12_ipc12
                       create-table-igp12_igpdi12
                       create-table-igp12_igpm12
                       create-table-precos12_ipca12])
  (initialize-first-update))

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
  (:max (nth (jdbc/query pg-db ["SELECT MAX (updated) FROM last_update"]) 0)))

(defn update-last-update []
  (jdbc/insert! pg-db :last_update
                {:updated (c/to-sql-date(t/now))}))

(defn check-if-date-key-already-exists [table date]
    (case table
      "precos12_inpc12" (check-if-exists-precos12-inpc12 date)
      "igp12_ipc12" (check-if-exists-igp12-ipc12 date)
      "igp12_igpdi12" (check-if-exists-igp12-igpdi12 date)
      "igp12_igpm12" (check-if-exists-igp12-igpm12 date)
      "precos12_ipca12" (check-if-exists-precos12-ipca12 date)
      true))

(defn insert-data-inflacao [data]
  (let [{:keys [SERCODIGO VALDATA VALVALOR NIVNOME TERCODIGO]} data]
    (if (true? (check-if-date-key-already-exists (string/lower-case SERCODIGO) VALDATA ))
      (println "Key was already presented")
      (jdbc/insert! pg-db (keyword (string/lower-case SERCODIGO))
                  {:valdata VALDATA
                   :valvalor VALVALOR
                   :nivnome NIVNOME
                   :tercodigo TERCODIGO}))))

(defn get-value-date-table [table date]
    (let [query (str "SELECT valvalor FROM " table " WHERE valdata = ? LIMIT 1")
          result (jdbc/query pg-db [query date])]
      (if (< 0 (count result))
        (:valvalor (nth result 0))
        0
        )))

(defn get-value-date-all-table [date]
  (let [all-values {:precos12_inpc12 (get-value-date-table "precos12_inpc12" date )
                    :igp12_ipc12 (get-value-date-table "igp12_ipc12" date )
                    :igp12_igpdi12 (get-value-date-table "igp12_igpdi12" date )
                    :igp12_igpm12 (get-value-date-table "igp12_igpm12" date )
                    :precos12_ipca12 (get-value-date-table "precos12_ipca12" date)}]
    all-values))

