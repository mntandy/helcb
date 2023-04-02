(ns helcb.db.lookup
  (:require [helcb.db.core :as db]
            [helcb.columns :as columns]
            [helcb.db.utils :as db.utils]
            [clojure.set :refer [rename-keys]]))

(defn where-clause [data-type option filter]
  (println data-type option filter)
  (case data-type
    "integer" (str (get {"equal to" " = " "not equal to" " != " "greater than" " > " "less than" " < "} option "") (bigdec filter))
    "timestamp" (str (get {"before" " < " "after" " > "} option "") " '" filter "'")
    "text" (str " LIKE " (case option
                           "equals" (str " '" filter "'")
                           "begins with" (str " '" filter "%'")
                           "ends with" (str " '%" filter "'")
                           "contains" (str " '%" filter "%'")))
    ""))

(defn where-string [type filters connective]
  (when-let [connective-str (case connective
                              :and " AND "
                              :or " OR "
                              " AND ")]
    (if (= filters nil) ""
        (let [vec (seq filters)
              [key val] (first vec)
              produce-str (fn [k v] 
                            (apply str (name k) (where-clause (columns/data-type-for-key type k) (:option v) (db.utils/double-up-single-quotes (:text v)))))]
          (apply str
                 "WHERE " (produce-str key val)
                 (mapv (fn [[k v]] (str connective-str (produce-str k v))) (rest vec)))))))

(defn zero< [s]
  (< 0 (count s)))

(defn sort-string [sort-direction sort-by]
  (if (and (zero< sort-direction) (zero< sort-by))
    (str "ORDER BY " sort-by " " sort-direction)
    ""))

(defn generate-lookup-map [params connective]
  (let [r {:table-name (get params :name)
   :sort (sort-string (get params :sort-direction "") (name (get params :sort-by-column "")))
   :filters (where-string (columns/table->type (get params :name)) (:filters params) connective)
   :offset (:offset params)
   :limit (:limit params)}]
    (println "r " r)
    r))

(defn look-up [params]
  (if (= (get params :name) "journeys") 
      (mapv #(rename-keys % columns/journeys-underline->dot) (db/get-journeys-with-station-names (generate-lookup-map params :and)))
      (db/get-from-table (generate-lookup-map params :and))))

(defn get-stations-with-filter [params]
  (db/get-from-table-no-limit-no-offset (generate-lookup-map params :and)))

(defn extract-time-for-analysis [datetime]
  (.getHour datetime))
