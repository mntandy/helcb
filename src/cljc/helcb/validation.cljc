(ns helcb.validation
  (:require
   [struct.core :as st]
   [helcb.columns :as columns]
   [helcb.filters :as filters] 
   [clojure.string :as str]))

(defn str-min-2-validator [k] [[st/required :message (str (name k) " is required.")]
                               [st/min-count 2 :message "Be generous. Use at least two letters in the uri."]])

(def csv-schema
  {:sep [[st/required :message "Separator is required."]
         [st/string :message "Must be a string."]
         [{:validate (fn [s] (re-matches #",|;" s))} :message "there are only two options."]]
   :uri (str-min-2-validator :uri)})

(defn csv-import [params]
  (first (st/validate params csv-schema)))

(def csv-import-success-schema
  {:result [[st/required :message "Something went wrong with the import"]
              [st/map :message "Something went wrong with the import"]]})

(defn csv-import-success [params]
  (first (st/validate params csv-import-success-schema)))

(def name-schema
  {:name [[st/required :message "No table?"]
          [st/string :message "Something is wrong with table name."]
          [{:validate (fn [s] (re-matches #"stations|journeys" s))} :message "there are only two options."]]})

(def id-schema
  {:id [[st/required :message "No id?"]
        [st/string :message "Something is wrong with id."]]})

(defn map-with-id [params]
  (first (st/validate params id-schema)))

(defn map-with-name [params]
  (first (st/validate params name-schema)))

(defn is-acceptable-datetime-format-or-blank? [m key] 
  (let [s (get-in m [key :text])] 
    (or (str/blank? s)
        (re-matches #"^\d{2}.\d{2}.\d{4} \d{2}:\d{2}:\d{2}$" s))))

(defn label-data [type]
  {:sort-by-column [[{:validate (fn [s] (or (= s "") (some #{s} (columns/for-lookup type :key))))
                      :message "Sort-by-column is wrong."}]]
   :sort-direction [[{:message "Sort-direction is wrong."
                      :validate (fn [s] (or (= s "") (some #{s} ["ASC" "DESC"])))}]]
   :filters [[{:message "Something is wrong with some filter"
               :validate (fn [m] 
                           (every? (set (columns/for-lookup type :key)) (keys m)))}]
             [{:message "Something is wrong with the option of some filter"
               :validate (fn [m] (every?
                                  (fn [k]
                                    (some #{(get-in m [k :option])}
                                          (filters/options-for-type
                                           (columns/data-type-from-key-for-lookup type k))))
                                  (keys m)))}]
             [{:message "Something is wrong with some search text"
               :validate (fn [m] (every? (fn [k] (get-in m [k :text])) (keys m)))}]
             [{:message "I am sorrt, but please search with complete date-time description of the form \"dd.MM.uuuu HH:mm:ss\""
               :validate (fn [m] (and (is-acceptable-datetime-format-or-blank? m :departure)
                                      (is-acceptable-datetime-format-or-blank? m :return)))}]]
   :offset [[st/number-str :message "offset no good."]]
   :limit [[st/number-str :message "limit no good."]]})

(defn data-request [params]
  (if-let [errors (map-with-name params)]
    errors
    (first (st/validate params (label-data (columns/table-name>key (get params :name)))))))

(def rows-schema
  {:rows [[st/required :message "No data."]
          [st/vector :message "Something is wrong with the data."]]})

(defn rows [params]
  (first (st/validate params rows-schema)))

(defn station-traffic [_]
  nil)

(defn station-info [_]
  nil)

(defn stations-for-map [_]
  nil)

(def update-station-schema
  {:id [[st/required :message "No db-id."]
          [st/number-str :message "Something is wrong with ID."]]
   :value [[st/required :message "No new value."]
        [st/string :message "Something is wrong with value."]]
   :column [[st/required :message "No column."]
           [st/string :message "Something is wrong with column."]]
   })

(defn updated-station [params]
  (first (st/validate params update-station-schema)))

(defn post [route]
  (case route
    "/update-station" updated-station
    ("/import-journeys" "/import-stations") csv-import))

(defn response [route]
  (case route
    "/update-station" (constantly nil)
    ("/import-journeys" "/import-stations") csv-import-success))

