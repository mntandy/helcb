(ns helcb.validation
  (:require
   [struct.core :as st]
   [helcb.columns :as columns]
   [helcb.filters :as filters]))

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
  {[:result :count] [[st/required :message "Something went wrong with the import"]
                     [st/number-str :message "Something went wrong with the import"]]})

(defn csv-import-success [params]
  (first (st/validate params csv-import-success-schema)))

(def name-schema
  {:name [[st/required :message "No table?"]
          [st/string :message "Something is wrong with table name."]
          [{:validate (fn [s] (re-matches #"stations|journeys" s))} :message "there are only two options."]]})

(defn map-with-name [params]
  (first (st/validate params name-schema)))

(defn label-data [type]
  {:sort-by-column [[{:validate (fn [s] (or (= s "") (some #{s} (columns/keys-as-names type))))
                      :message "Sort-by-column does not exists."}]]
   :sort-direction [[{:message "Sort-direction is wrong."
                      :validate (fn [s] (or (= s "") (some #{s} ["ASC" "DESC"])))}]]
   :filters [[{:message "Error with filters: some column is wrong"
               :validate (fn [m] 
                           (every? (set (get columns/keys-for-filters type)) (keys m)))}]
             [{:message "Error with filters: some option is wrong"
               :validate (fn [m] (every?
                                  (fn [k]
                                    (some #{(get-in m [k :option])}
                                          (filters/options-for-type
                                           (columns/data-type-for-key type k))))
                                  (keys m)))}]
             [{:message "Error with filters: some text is wrong"
               :validate (fn [m] (every? (fn [k] (get-in m [k :text])) (keys m)))}]]
   :offset [[st/number-str :message "offset no good."]]
   :limit [[st/number-str :message "limit no good."]]})

(defn data-request [params]
  (if-let [errors (map-with-name params)]
    errors
    (first (st/validate params (label-data (columns/table->type (get params :name)))))))

(def rows-schema
  {:rows [[st/required :message "No data."]
          [st/vector :message "Something is wrong with the data."]]})

(defn rows [params]
  (first (st/validate params rows-schema)))