(ns helcb.db.import
  (:require
   [clojure.set :refer [rename-keys]]
   [helcb.csv.core :as csv]
   [helcb.db.core :as db]
   [helcb.db.lookup :as db.lookup]
   [helcb.columns :as columns]
   [helcb.clj-utils :as utils]
   [maailma.core :as m]))

(defn update-stationid [m]
  (update m :stationid utils/trim-leading-zeros))

(defn prepare-station-row-for-import [m]
  (let [label->key (zipmap (columns/for-db :stations :label) (columns/for-db :stations :key))
        result (merge (zipmap (columns/for-db :stations :key) (repeat ""))
                      (update-stationid (rename-keys m label->key)))]
    {:name "stations" :column-names (map name (keys result)) :column-values (vals result)}))

(defn stations-from-csv [params]
  (csv/import-from-uri (:uri params) (:sep params)
                         #(run! (comp db/insert-row! prepare-station-row-for-import) %)
                         (columns/for-db :stations :label)))

(defn prepare-journey-row-for-import [m] 
  (let [result (merge (zipmap (columns/for-db :journeys :key) (repeat "")) 
                      (dissoc m :departure_station :return_station))]
    {:name "journeys" :column-names (map name (keys result)) :column-values (vals result)}))

(defn station-with-that-id-has-that-name [id name] 
  (some #{name} (vals (select-keys (db.lookup/station-from-stationid id) [:name :namn :nimi]))))

(defn check-station-id [m]
  (or (and
         (station-with-that-id-has-that-name (:departure_station_id m) (:departure_station m))
         (station-with-that-id-has-that-name (:return_station_id m) (:return_station m)))
        (println "failed check-station-id: " m)))

(defn check-distance-and-duration [m]
  (or (and (<= 10 (:distance m)) (<= 10 (:duration m)))
      (println "bad or short distance or duration: " m)))
  
(defn import-journey! [m]
  (let [preprocessed-m
        (-> m
            (rename-keys (zipmap (columns/for-import :journeys :label) (columns/for-import :journeys :key)))
            (update :departure_station_id utils/trim-leading-zeros)
            (update :return_station_id utils/trim-leading-zeros)
            (update :distance utils/convert-to-bigint-or-zero)
            (update :duration utils/convert-to-bigint-or-zero)
            (update :departure utils/convert-time)
            (update :return utils/convert-time))]
    (if (and (check-station-id preprocessed-m) (check-distance-and-duration preprocessed-m))
      (db/insert-row! (prepare-journey-row-for-import preprocessed-m)) 
      0)))


(defn import-reducer [save-row!]
  (fn [col]
    (reduce (fn [result next]
               (case (save-row! next)
                 0 (-> result
                       (update :line inc)
                       (update :ignored conj (+ 2 (:line result))))
                 1 (-> result
                       (update :line inc)
                       (update :imported inc))))
             {:line 0 :ignored [] :imported 0}
             col)))

(defn print-and-return-file-error [e]
  (println (.getMessage e))
  {:error "Something's wrong with the file. Does it exist? Is it a CSV file? Go find out!"})

(defn journeys-from-csv [params]
  (try
    {:result (csv/import-from-uri (:uri params) (:sep params)
                                  (import-reducer import-journey!)
                                  (columns/for-import :journeys :label))}
    (catch Exception e (print-and-return-file-error e))))

