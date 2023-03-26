(ns helcb.db.core
  (:require
   [mount.core :as mount]
   [conman.core :as conman]
   [helcb.config :refer [env]]))

(mount/defstate ^:dynamic *db*
  :start (conman/connect! {:jdbc-url (env :database-url)})
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")
