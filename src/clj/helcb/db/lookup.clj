(ns helcb.db.lookup
  (:require [helcb.db.core :as db]
            [helcb.columns :as columns]
            [helcb.clj-utils :as utils]
            [clojure.set :refer [rename-keys]]
            [clojure.string :refer [blank?]]))


(defn connective-str [connective]
  (case connective 
    :and " AND " 
    :or " OR "
    :null ""
    " AND "))

(defn where-clause [data-type option filter]
  (case data-type
    "integer" (str (get {"equal to" " = " "not equal to" " != " "greater than" " > " "less than" " < "} option "") (bigdec filter))
    "timestamp" (str (get {"before" " < " "after" " > " "equal to" " = "} option "") " '" filter "'")
    "text" (str " LIKE " (case option
                           "equal to" (str " '" filter "'")
                           "begins with" (str " '" filter "%'")
                           "ends with" (str " '%" filter "'")
                           "contains" (str " '%" filter "%'")))
    ""))

(defn produce-str [type connective k v]
  (apply str
         (connective-str connective)
         (name k)
         (where-clause
          (columns/data-type-from-key-for-lookup type k)
          (:option v)
          (utils/double-up-single-quotes (:text v)))))

(defn where-string [type filters connective] 
  (let [vec (filter #(not (blank? (:text (second %)))) (seq filters))
            [key val] (first vec)]
        (if (empty? vec) ""
            (apply str
                   "WHERE " (produce-str type :null key val)
                   (mapv (fn [[k v]] (str (produce-str type connective k v))) (rest vec))))))

(defn zero< [s]
  (< 0 (count s)))

(defn sort-string [sort-direction sort-by]
  (if (and (zero< sort-direction) (zero< sort-by))
    (str "ORDER BY " sort-by " " sort-direction)
    ""))

(defn generate-lookup-map [params connective]
  (let [r {:table-name (get params :name)
   :sort (sort-string (get params :sort-direction "") (name (get params :sort-by-column "")))
   :filters (where-string (columns/table-name>key (get params :name)) (:filters params) connective)
   :offset (:offset params)
   :limit (:limit params)}]
    r))

(defn look-up [params]
  (if (= (get params :name) "journeys") 
      (mapv #(rename-keys % columns/journeys-transformation-from-db) (db/get-journeys-with-station-names (generate-lookup-map params :and)))
      (db/get-from-table (generate-lookup-map params :and))))

(defn station-exists? [id]
  (boolean (:count (first (db/count-rows-with-value {:name "stations" :column "stationid" :value id})))))

(defn station-from-stationid [id]
  (first (db/get-rows-with-value {:name "stations" :column "stationid" :value id})))

(defn get-all-entries-with-filter [params]
  (db/get-from-table-no-limit-no-offset (generate-lookup-map params :and)))

(defn find-with-zeros []
  (get-all-entries-with-filter {:name "stations", :filters {:stationid {:text "0", :option "begins with"}}}))

(defn first-and-last-date-in-db []
  (let [minmax (db/first-and-last-journey)]
    {:first (utils/convert-time-with-pattern (:min (first minmax)))
     :last (utils/convert-time-with-pattern (:max (first minmax)))}))

(defn weekdays-in-db []
  (let [dates (first-and-last-date-in-db)] 
    (utils/weekdays-between (:first dates) (:last dates))))

(defn weekend-days-in-db []
  (let [dates (first-and-last-date-in-db)]
    (utils/weekend-days-between (:first dates) (:last dates))))

(defn hour-as-key-and-average [d]
  (fn [m] [(int (:extract m)) (float (with-precision 2 (/ (:count m) (bigdec d))))]))

(defn average-trips-per-hour-from-station-during-weekdays [id]
  (into (sorted-map) 
        (map
         (hour-as-key-and-average (weekdays-in-db))
         (db/count-journeys-per-hour-from-station-during-weekdays {:departure_station_id id}))))

(defn average-trips-per-hour-from-station-during-weekends [id]
  (into (sorted-map) 
        (map
         (hour-as-key-and-average (weekend-days-in-db))
         (db/count-journeys-per-hour-from-station-during-weekends {:departure_station_id id}))))

(defn average-trips-per-hour-to-station-during-weekdays [id]
  (into (sorted-map) 
        (map
         (hour-as-key-and-average (weekdays-in-db))
         (db/count-journeys-per-hour-to-station-during-weekdays {:return_station_id id}))))

(defn average-trips-per-hour-to-station-during-weekends [id]
  (into (sorted-map) 
        (map
         (hour-as-key-and-average (weekend-days-in-db))
         (db/count-journeys-per-hour-to-station-during-weekends {:return_station_id id}))))

(defn average-trips-to-and-from-station [id]
  {:to-weekends (average-trips-per-hour-to-station-during-weekends id)
   :to-weekdays (average-trips-per-hour-to-station-during-weekdays id)
   :from-weekends (average-trips-per-hour-from-station-during-weekends id)
   :from-weekdays (average-trips-per-hour-from-station-during-weekdays id)})