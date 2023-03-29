(ns helcb.core
  (:require [helcb.http.core :as http]
            [helcb.db.core :as db]
            [mount.core :as mount]
            [helcb.columns :as columns]))

(defn -main
  [& args]
  (mount/start))

(defn column-with-data-type-string [columns types]
  (str (first columns) " " (first types)
       (apply str (map #(str ", " %1 " " %2 "") (next columns) (next types)))))

(defn create-stations-table []
  (db/create-new-table! {:name "stations" :columns (column-with-data-type-string (columns/keys-as-names :stations) (repeat "text"))}))

(defn create-journeys-table []
  (db/create-new-table! {:name "journeys" :columns (column-with-data-type-string (columns/journeys-db-keys-as-names) (columns/journeys-db-types))}))