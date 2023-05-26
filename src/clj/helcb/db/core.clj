(ns helcb.db.core
  (:require
   [next.jdbc.date-time]
   [next.jdbc.result-set]
   [clojure.java.jdbc :as jdbc]
   [mount.core :as mount]
   [conman.core :as conman]
   [helcb.columns :as columns]
   [clojure.string :as str]
   [helcb.config :refer [env]]))

(mount/defstate ^:dynamic *db*
  :start (conman/connect! {:jdbc-url (env :database-url)})
  :stop (conman/disconnect! *db*))

(defn rebind []
  (conman/bind-connection *db* "sql/queries.sql"))

(rebind)

(defn timestamp-sql->java [t]
  (-> t
      (.toLocalDateTime)
      (.format (java.time.format.DateTimeFormatter/ofPattern "dd.MM.uuuu HH:mm:ss"))))

(extend-protocol next.jdbc.result-set/ReadableColumn
  java.sql.Timestamp
  (read-column-by-label [^java.sql.Timestamp v _]
    (timestamp-sql->java v))
  (read-column-by-index [^java.sql.Timestamp v _2 _3]
    (timestamp-sql->java v))
  java.sql.Date
  (read-column-by-label [^java.sql.Date v _]
    (.toLocalDate v))
  (read-column-by-index [^java.sql.Date v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (read-column-by-label [^java.sql.Time v _]
    (.toLocalTime v))
  (read-column-by-index [^java.sql.Time v _2 _3]
    (.toLocalTime v)))

(defn create-table [table key]
  (let [columns (columns/for-db key name :key)
        types (columns/for-db key :type)]
  (create-new-table! {:name table 
                      :columns (str/join ", " columns) 
                      :columns-datatype (str/join ", " (map #(str %1 " " %2) columns types))})))

(defn create-stations-table []
  (create-table "stations" :stations))
  
(defn create-journeys-table []
  (create-table "journeys" :journeys))

(defn reset-journeys []
  (drop-table! {:name "journeys"})
  (create-journeys-table))


