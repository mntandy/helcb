(ns helcb.db.import
  (:require
   [clojure.set :refer [rename-keys]]
   [helcb.csv.core :as csv]
   [helcb.db.core :as db]
   [helcb.db.lookup :as db.lookup]
   [helcb.columns :as columns]
   [helcb.clj-utils :as utils]))

(defn print-and-return-file-error [e]
  (println (.getMessage e))
  {:error "Something's wrong with the file. Does it exist? Is it a CSV file? Go find out!"})

(defn update-stationid [m]
  (update m :stationid utils/trim-leading-zeros))

(defn prepare-station-row-for-import [m]
  (let [label->key (zipmap (columns/for-db :stations :label) (columns/for-db :stations :key))
        result (merge (zipmap (columns/for-db :stations :key) (repeat ""))
                      (update-stationid (rename-keys m label->key)))]
    {:name "stations" :column-names (map name (keys result)) :column-values (vals result)}))

(defn prepare-journey-row-for-import [m] 
  (let [result (merge (zipmap (columns/for-db :journeys :key) (repeat "")) 
                      (dissoc m :departure_station :return_station))]
    {:name "journeys" :column-names (map name (keys result)) :column-values (vals result)}))

(defn stations-not-exists? [m]
  (when-not (and 
             (db.lookup/station-exists? (:departure_station_id m)) 
             (db.lookup/station-exists? (:return_station_id m)))
    (str "something is wrong with the station ids: " (:departure_station_id m) (:return_station_id m))))

(defn distance-too-short? [m]
  (when-not (<= 10 (:distance m)) (str "bad or too short distance.")))

(defn duration-too-short? [m]
  (let [dep (:departure m)
           ret (:return m)]
    (when-not (and (some? dep) (some? ret) (utils/is-at-least-ten-seconds-after? dep ret))
      (str "return time is less than 10 seconds after departure time."))))

(defn is-error-in-journey? [v m] 
  (loop [fs v]
    (if (seq fs)
      (if-let [error ((first fs) m)] 
        error
        (recur (rest fs)))
      false)))

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
    (if-let [error (is-error-in-journey? [stations-not-exists? distance-too-short? duration-too-short?] preprocessed-m)]
      0
      (try 
        (db/insert-row! (prepare-journey-row-for-import preprocessed-m))
        (catch Exception e (do (println e) 0))))))

(defn import-station! [m]
  (try
    (db/insert-row! (prepare-station-row-for-import m))
    (catch Exception e (do (println e) 0))))

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

(defn journeys-from-csv [params]
  (try
    {:result (csv/import-from-uri (:uri params) (:sep params)
                                  (import-reducer import-journey!)
                                  (columns/for-import :journeys :label))}
    (catch Exception e (print-and-return-file-error e))))

(defn stations-from-csv [params]
  (try
    {:result (csv/import-from-uri (:uri params) (:sep params)
                                  (import-reducer import-station!)
                                  (columns/for-db :stations :label))}
    (catch Exception e (print-and-return-file-error e))))
