(ns helcb.explore.state
  (:require [reagent.core :as r]
            [helcb.filters :as filters]))

(def initial-settings {:name nil :offset 0 :got-all false :limit 3 :sort-by-column "" :sort-direction ""})


(def test-rows-journeys 
  [{:departure-id "094"
    :departure-nimi "Laajalahden aukio"
    :departure-name "Laajalahden aukio"
    :departure-namn "Bredviksplatsen"
    :return-id "100"
    :return-nimi "Teljäntie"
    :return-name "Teljäntie"
    :return-namn "Täljevägen"
    :distance "2043"
    :duration "500"}])

(def settings (r/atom initial-settings))

(def rows (r/atom []))

(defn reset-to-initial! []
  (reset! settings initial-settings)
  (reset! rows []))

(defn set-name [type]
  (swap! settings assoc :name 
         (case type 
           :explore-journeys "journeys"
           :explore-stations "stations"
           nil)))

(defn filters []
  (get @settings :filters))

(defn add-limit-to-offset []
  (swap! settings update :offset + (:limit @settings)))

(defn update-sorting! [column]
  (if (not= (:sort-by-column @settings) column)
    (swap! settings assoc
           :sort-by-column column
           :sort-direction "ASC")
    (swap! settings assoc
           :sort-direction (case (:sort-direction @settings) "ASC" "DESC" "DESC" "ASC" ""))))

(defn update-filter-for-column! [column data-type]
  (fn [value]
    (if (contains? (filters) column)
      (swap! settings assoc-in [:filters column :text] value)
      (swap! settings assoc-in [:filters column] {:text value :option (first (filters/options-for-type data-type))}))))

(defn update-filter-selector! [column value]
  (when (not= value "Filter")
    (swap! settings assoc-in [:filters column :option] value)))

(defn filter-text-for-column [column]
  (get-in @settings [:filters column :text] ""))

(defn update-got-all [value]
  (swap! settings assoc :got-all value))

(defn filter-option-for-column [column]
  (get-in @settings [:filters column :option] "Filter"))

(defn filters []
  (get @settings :filters))

(defn limit []
  (get @settings :limit))

(defn update-rows! [v reset]
  (update-got-all (< (count v) (limit)))
  (reset! rows (if reset v (into @rows v))))

(defn column-label-with-direction [column label]
  (str label (when (= (:sort-by-column @settings) column) 
               (case (:sort-direction @settings)
                 "ASC" " \u2191"
                 "DESC" " \u2193"
                 ""))))

(defn prepare-for-request [reset add-offset-to-limit]
  (assoc (select-keys @settings [:name :sort-by-column :sort-direction :filters :offset :limit])
                 :offset (if reset "0" (str (:offset @settings)))
                 :limit (if add-offset-to-limit (str (+ (:limit @settings) (:offset @settings))) (str (:limit @settings)))))
