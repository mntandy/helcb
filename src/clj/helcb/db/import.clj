(ns helcb.db.import
  (:require
   [clojure.set :refer [rename-keys]]
   [helcb.csv.core :as csv]
   [helcb.db.core :as db]
   [helcb.db.lookup :as db.lookup]
   [helcb.columns :as columns]
   [helcb.clj-utils :as utils]))

(defn update-stationid [m]
  (update m :stationid utils/trim-leading-zeros))

(defn prepare-station-row-for-import [m]
  (let [label->key (zipmap (columns/for-db :stations :label) (columns/for-db :stations :key))
        result (merge (zipmap (columns/for-db :stations :key) (repeat ""))
                      (update-stationid (rename-keys m label->key)))]
    {:name "stations" :column-names (map name (keys result)) :column-values (vals result)}))

(defn stations-from-csv [params]
  (csv/import-from-uri (:uri params) (:sep params)
                         #(db/insert-row! (prepare-station-row-for-import %))
                         (columns/for-db :stations :label)))

(defn prepare-journey-row-for-import [m] 
  (let [result (merge (zipmap (columns/for-db :journeys :key) (repeat "")) 
                      (dissoc m :departure_station :return_station))]
    {:name "journeys" :column-names (map name (keys result)) :column-values (vals result)}))

(defn check-station-id [m]
  (let [departure-station (db.lookup/station-from-stationid (:departure_station_id m))
        return-station (db.lookup/station-from-stationid (:return_station_id m))]
    (and (some #{(:departure_station m)} (vals (select-keys departure-station [:name :namn :nimi])))
         (some #{(:return_station m)} (vals (select-keys return-station [:name :namn :nimi]))))))

(defn integer-str-distance-and-duration? [m]
  (and (utils/integer-string? (:distance m)) (utils/integer-string? (:duration m))))   

(defn convert-to-time-and-int [m] 
  (-> m
      (update :distance bigint)
      (update :duration bigint)
      (update :departure utils/convert-time)
      (update :return utils/convert-time)))

(defn import-or-print! [m]
  (let [renamed-m (rename-keys m (zipmap (columns/for-import :journeys :label) (columns/for-import :journeys :key)))]
    (if-not (check-station-id (update-stationid renamed-m))
      (println "failed check-station-id:" renamed-m)
      (if-not (integer-str-distance-and-duration? renamed-m)
        (println "failed time or int conversion:" renamed-m)
        (db/insert-row! (prepare-journey-row-for-import (convert-to-time-and-int renamed-m)))))))

(defn journeys-from-csv [params] 
  (csv/import-from-uri (:uri params) (:sep params)
                         import-or-print!
                         (columns/for-import :journeys :label)))

