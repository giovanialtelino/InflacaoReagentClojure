(ns inflacao-pedestal-service.manual-init
  (:require [clojure.string :as str]
            [java-time :as jt]))


(def create-table-precos12_ipca12
  (jdbc/create-table-ddl :precos12_ipca12
                         [[:valdata :date :primary :key]
                          [:valvalor :float :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))



(def create-table-igp12_igpm12
  (jdbc/create-table-ddl :igp12_igpm12
                         [[:valdata :date :primary :key]
                          [:valvalor :float :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))



(def create-table-igp12_igpdi12
  (jdbc/create-table-ddl :igp12_igpdi12
                         [[:valdata :date :primary :key]
                          [:valvalor :float :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))



(def create-table-igp12_ipc12
  (jdbc/create-table-ddl :igp12_ipc12
                         [[:valdata :date :primary :key]
                          [:valvalor :float :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))



(def create-table-precos12_inpc12
  (jdbc/create-table-ddl :precos12_inpc12
                         [[:valdata :date :primary :key]
                          [:valvalor :float :not :null]
                          [:nivnome "varchar(32)"]
                          [:tercodigo "varchar(32)"]]))

(def last-update
  (jdbc/create-table-ddl :last_update
                         [[:id :serial :primary :key]
                          [:updated :date]]))

(defn initialize-first-update []
  (let [date (jt/to-sql-date (jt/local-date-time 1990 1 1))]
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