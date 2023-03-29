(ns helcb.db.import
  (:require
   [clojure.set :refer [rename-keys]]
   [helcb.csv.core :as csv]
   [helcb.db.core :as db]
   [helcb.columns :as columns]))


(defn prepare-station-row-for-import [m]
  (let [result (merge (zipmap (columns/db-keys :stations) (repeat ""))
                      (rename-keys m (columns/label->key :stations)))]
    {:name "stations" :column-names (map name (keys result)) :column-values (vals result)}))

(defn stations-from-csv [params]
  (csv/import-from-uri (:uri params) (:sep params)
                         #(db/insert-row! (prepare-station-row-for-import %))
                         (columns/csv-labels :stations)))

(defn prepare-journey-row-for-import [m] 
  (let [result (dissoc (merge (zipmap (columns/keys-for-journeys-csv-import) (repeat ""))
                      m) :departure_station :return_station)]
    {:name "journeys" :column-names (map name (keys result)) :column-values (vals result)}))


(defn check-station-id [m]
  (let [departure-station (first (db/get-rows-with-value {:name "stations" :column "stationid" :value (:departure_station_id m)}))
        return-station (first (db/get-rows-with-value {:name "stations" :column "stationid" :value (:return_station_id m)}))]
    (and (some #{(:departure_station m)} (vals (select-keys departure-station [:name :namn :nimi])))
         (some #{(:return_station m)} (vals (select-keys return-station [:name :namn :nimi]))))))

(defn integer-string? [s] (re-matches #"-?(0|[1-9]\d*+)" s))

(defn integer-str-distance-and-duration? [m]
  (and (integer-string? (:distance m)) (integer-string? (:duration m))))   

(defn convert-time [s]
  (try (java.time.LocalDateTime/parse s) (catch Exception e nil)))

(defn convert-to-time-and-int [m] 
  (-> m
      (update :distance bigint)
      (update :duration bigint)
      (update :departure convert-time)
      (update :return convert-time)))
    
(defn import-or-print! [m]
  (let [renamed-m (rename-keys m (columns/journeys-csv-import-label->key))]
    (if-not (check-station-id renamed-m)
      (println "failed check-station-id:" renamed-m)
      (if-not (integer-str-distance-and-duration? renamed-m)
        (println "failed time or int conversion:" renamed-m)
        (db/insert-row! (prepare-journey-row-for-import (convert-to-time-and-int renamed-m)))))))

(defn journeys-from-csv [params] 
  (csv/import-from-uri (:uri params) (:sep params)
                         import-or-print!
                         (columns/labels-for-journeys-csv-import)))

