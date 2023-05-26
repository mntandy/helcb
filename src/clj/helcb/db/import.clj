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

(defn prepare-single-import-map [table]
  (fn [m]
    {:name table 
     :column-names (map name (keys m)) 
     :column-values (vals m)}))

(defn prepare-batch-import-map [table]
  (fn [coll]
    (let [first-map (first coll)]
      {:name table 
       :column-names (map name (keys first-map)) 
       :column-values (concat [(vals first-map)] (map vals (rest coll)))})))

(defn stations-not-exists? [m]
  (when-not (and
             (db.lookup/station-exists? (:departure_station_id m)) 
             (db.lookup/station-exists? (:return_station_id m)))
    (str "something is wrong with one of the station ids " (:departure_station_id m) " " (:return_station_id m))))

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

(defn preprocess-station [m]
  (update (reduce
           (fn [result,next] 
             (update result next utils/at-least-empty-string))
           (rename-keys m (zipmap (columns/for-db :stations :label) (columns/for-db :stations :key)))
           (columns/for-db :stations :key)) 
          :stationid utils/trim-leading-zeros))

(defn preprocess-journey [m]
  (-> m
      (rename-keys (zipmap (columns/for-db :journeys :label) (columns/for-db :journeys :key)))
      (update :departure_station_id utils/trim-leading-zeros)
      (update :return_station_id utils/trim-leading-zeros)
      (update :distance utils/convert-to-bigint-or-zero)
      (update :duration utils/convert-to-bigint-or-zero)
      (update :departure utils/convert-time)
      (update :return utils/convert-time)))

(defn no-error-with-journey [m]
  (if (is-error-in-journey? [stations-not-exists? distance-too-short? duration-too-short?] m)
    false
    true))

(defn try-or-catch-zero [func coll]
  (try
    (func coll)
    (catch Exception e (do (println e) 0))))

(defn single-import! [preprocess prepare] 
  (fn [coll]
    (reduce + (map
               #(try-or-catch-zero db/insert-row! (prepare %))
               (preprocess coll)))))

(defn batch-import! [preprocess prepare]
  (fn [coll]
  (reduce + (map
             #(try-or-catch-zero db/insert-rows! (prepare %))
             (partition 5000 5000 [] (preprocess coll))))))

(defn journeys-from-csv [params]
  (try
    {:result (csv/import-from-uri (:uri params) (:sep params)
                                  (single-import! #(filter no-error-with-journey (map preprocess-journey %)) (prepare-single-import-map "journeys"))
                                  (columns/for-db :journeys :label))}
    (catch Exception e (print-and-return-file-error e))))

(defn batch-journeys-from-csv [params]
  (try
    {:result (csv/import-from-uri (:uri params) (:sep params)
                                  (batch-import! #(filter no-error-with-journey (map preprocess-journey %)) (prepare-batch-import-map "journeys")) 
                                  (columns/for-db :journeys :label))}
    (catch Exception e (print-and-return-file-error e))))


(defn stations-from-csv [params]
  (try
    {:result (csv/import-from-uri (:uri params) (:sep params)
                                  (single-import! #(map preprocess-station %) (prepare-single-import-map "stations"))
                                  (columns/for-db :stations :label))}
    (catch Exception e (print-and-return-file-error e))))

(defn batch-stations-from-csv [params]
  (try
    {:result (csv/import-from-uri (:uri params) (:sep params)
                                  (batch-import! #(map preprocess-station %) (prepare-batch-import-map "stations")) 
                                  (columns/for-db :stations :label))}
    (catch Exception e (print-and-return-file-error e))))
