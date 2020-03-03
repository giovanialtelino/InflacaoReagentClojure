(ns inflacao-pedestal-service.database
  (:require
    [cheshire.core :as cs]
    [com.stuartsierra.component :as component]
    [clojure.java.jdbc :as jdbc]
    [java-time :as jt]
    [clj-http.client :as client]
    [clojure.string :as string]
    [inflacao-pedestal-service.utils :as utils]))

(defn- pool-query
  ([conn query]
   (jdbc/with-db-connection [pool-conn conn]
                            (let [result (jdbc/query pool-conn query)]
                              result)))
  ([conn query args]
   (jdbc/with-db-connection [pool-conn conn]
                            (let [result (jdbc/query pool-conn [query args])] result))))

(defn check-if-exists-precos12-ipca12 [conn date]
  (let [exist (count (pool-query conn "SELECT valvalor AS valor FROM precos12_ipca12 WHERE valdata::date = ?" date))]
    (if (> exist 0)
      true
      false)))

(defn check-if-exists-igp12-igpm12 [conn date]
  (let [exist (count (pool-query conn "SELECT valvalor AS valor FROM igp12_igpm12 WHERE valdata::date = ?" date))]
    (if (> exist 0)
      true
      false)))

(defn check-if-exists-igp12-igpdi12 [conn date]
  (let [exist (count (pool-query conn "SELECT valvalor AS valor FROM igp12_igpdi12 WHERE valdata::date = ?" date))]
    (if (> exist 0)
      true
      false)))

(defn check-if-exists-igp12-ipc12 [conn date]
  (let [exist (count (pool-query conn "SELECT valvalor AS valor FROM igp12_ipc12 WHERE valdata::date = ?" date))]
    (if (> exist 0)
      true
      false)))

(defn check-if-exists-precos12-inpc12 [conn date]
  (let [exist (count (pool-query conn "SELECT valvalor AS valor FROM precos12_inpc12 WHERE valdata::date = ?" date))]
    (if (> exist 0)
      true
      false)))

(defn get-last-update [conn]
  (:max (nth (pool-query conn ["SELECT MAX (updated) FROM last_update"]) 0)))

(defn update-last-update [conn]
  (jdbc/insert! conn :last_update {:updated (jt/to-sql-date (jt/local-date-time))}))

(defn check-if-date-key-already-exists [table date conn]
  (let [format-date (utils/format-date-to-javatime date)]
    (case table
      "precos12_inpc12" (check-if-exists-precos12-inpc12 conn format-date)
      "igp12_ipc12" (check-if-exists-igp12-ipc12 conn format-date)
      "igp12_igpdi12" (check-if-exists-igp12-igpdi12 conn format-date)
      "igp12_igpm12" (check-if-exists-igp12-igpm12 conn format-date)
      "precos12_ipca12" (check-if-exists-precos12-ipca12 conn format-date)
      true)))

(defn insert-data-inflacao [conn data]
  (let [{:keys [SERCODIGO VALDATA VALVALOR NIVNOME TERCODIGO]} data]
    (if (true? (check-if-date-key-already-exists conn (string/lower-case SERCODIGO) VALDATA))
      (println "Key was already presented")
      (jdbc/insert! conn (keyword (string/lower-case SERCODIGO))
                    {:valdata   (utils/format-date-to-javatime VALDATA)
                     :valvalor  VALVALOR
                     :nivnome   NIVNOME
                     :tercodigo TERCODIGO}))))

(defn get-value-date-table [conn table date]
  (let [query (str "SELECT valvalor FROM " table " WHERE valdata =  ? LIMIT 1")
        result (pool-query conn query date)]
    (prn "Result? Result")
    (prn result)
    (if (< 0 (count result))
      (:valvalor (nth result 0))
      0
      )))

(defn get-value-date-all-table [conn date]
  (let [formated-date (utils/format-date-to-javatime date)
        all-values {:precos12_inpc12 (get-value-date-table conn "precos12_inpc12" formated-date)
                    :igp12_ipc12     (get-value-date-table conn "igp12_ipc12" formated-date)
                    :igp12_igpdi12   (get-value-date-table conn "igp12_igpdi12" formated-date)
                    :igp12_igpm12    (get-value-date-table conn "igp12_igpm12" formated-date)
                    :precos12_ipca12 (get-value-date-table conn "precos12_ipca12" formated-date)}]
    all-values))

(defn get-value-all-data [conn table]
  (let [query (str "SELECT TO_CHAR(valvalor::FLOAT, 'FM999999990.00000000') AS Valor, TO_CHAR(valdata, 'dd/mm/yyyy') AS Data FROM " table " ORDER BY valdata DESC")
        result (pool-query conn query)]
    (prn "RESULT BELLOW!!!")
    (prn result)
    result))

(defn get-all-use-data [conn]
  (let [all-values {:precos12_inpc12 (get-value-all-data conn "precos12_inpc12")
                    :igp12_ipc12     (get-value-all-data conn "igp12_ipc12")
                    :igp12_igpdi12   (get-value-all-data conn "igp12_igpdi12")
                    :igp12_igpm12    (get-value-all-data conn "igp12_igpm12")
                    :precos12_ipca12 (get-value-all-data conn "precos12_ipca12")
                    :last-update     (jt/format "dd/MM/yyyy" (jt/local-date (get-last-update conn)))}]
    all-values))

;-------

(def links-vector ["http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='PRECOS12_IPCA12')",
                   "http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IGPM12')",
                   "http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IGPDI12')",
                   "http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IPC12')",
                   "http://ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='PRECOS12_INPC12')"])

(defn get-data [link]
  (client/get link {:accept :json} true))

(defn parse-data [json]
  (:value (cs/parse-string (:body json) true)))

(defn same-month [last-update]
  (if (= (jt/format "MM" (jt/local-date)) (jt/format "MM" (jt/local-date last-update)))
    true
    false))

(defn same-year [last-update]
  (if (= (jt/format "yyyy" (jt/local-date)) (jt/format "yyyy" (jt/local-date last-update)))
    true
    false))

(defn check-last-data [conn]
  (let [data (get-last-update conn)]
    (if (nil? data)
      true
      (if (false? (same-month data))
        true
        (if (false? (same-year data))
          true
          false)))))

(defn save-data [clean-data conn]
  (let [vector-size (count clean-data)]
    (loop [i 0]
      (if (< i vector-size)
        (do
          (insert-data-inflacao conn (nth clean-data i))
          (recur (inc i)))))))

(defn access-data [conn]
  (if (true? (check-last-data conn))
    (let [vector-size (count links-vector)]
      (loop [i 0]
        (if (< i vector-size)
          (do (save-data (parse-data (get-data (get links-vector i))) conn)
              (recur (inc i)))))
      (update-last-update conn))))