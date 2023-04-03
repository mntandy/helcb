(ns helcb.db.core
  (:require
   [next.jdbc.date-time]
   [next.jdbc.result-set]
   [mount.core :as mount]
   [conman.core :as conman]
   [helcb.columns :as columns]
   [helcb.config :refer [env]]))

(mount/defstate ^:dynamic *db*
  :start (conman/connect! {:jdbc-url (env :database-url)})
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")

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


(defn column-with-data-type-string [columns types]
  (str (first columns) " " (first types)
       (apply str (map #(str ", " %1 " " %2 "") (next columns) (next types)))))

(defn create-stations-table []
  (create-new-table! {:name "stations" :columns (column-with-data-type-string (columns/for-db :stations name :key) (columns/for-db :stations :type))}))

(defn create-journeys-table []
  (create-new-table! {:name "journeys" :columns (column-with-data-type-string (columns/for-db :journeys name :key) (columns/for-db :journeys :type))}))

;departure_statistics  integer ARRAY [24]
;return_statistics  integer ARRAY [24]

;UPDATE stations SET departures [:i:hour] = departures [:i:hour] + 1
;WHERE stationid = i:stationid
