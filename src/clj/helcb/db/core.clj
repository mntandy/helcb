(ns helcb.db.core
  (:require
   [next.jdbc.date-time]
   [next.jdbc.result-set]
   [mount.core :as mount]
   [conman.core :as conman]
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

