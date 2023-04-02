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
  (db/create-new-table! {:name "stations" :columns (column-with-data-type-string (columns/for-db :stations name :key) (columns/for-db :stations :type))}))

(defn create-journeys-table []
  (db/create-new-table! {:name "journeys" :columns (column-with-data-type-string (columns/for-db :journeys name :key) (columns/for-db :journeys :type))}))