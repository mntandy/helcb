(ns helcb.explore.table
  (:require 
   [helcb.state :as state]
   [helcb.explore.state :as explore.state]
   [helcb.station.state :as station.state]
   [helcb.filters :as filters]
   [helcb.language :as language]
   [helcb.http :as http]
   [helcb.commons :as commons]))

(defn filter-input [key data-type style]
  (commons/text-input
   key
   (get-in (explore.state/filters) [key :text])
   (explore.state/update-filter-for-column! key data-type)
   http/get-filtered-data
   style))

(defn selector-input [style handler options]
  [:select {:style style :on-change handler}
   (for [o options]
     [:option {:key o :value o} o])])

(defn align-by-type [type]
  (get {"decimal" "right"
        "integer" "right"
        "text" "equals"}
       type))


(defn select-station [row text]
  [:a {:on-click #(do
                    (http/get-station-traffic (:stationid row))
                    (station.state/initialise! @state/display row)
                    (state/update-state! :single-station))} text])

(defn link-to-station [row path text]
  (if-let [linked (get-in
                   {:journeys
                    {:departure-station select-station
                     :return-station select-station}
                    :stations
                    {:name select-station}}
                   path)]
    (linked row text)
    text))
  

(defn table []
  (println @explore.state/settings)
  (println @explore.state/rows)
  (let [type (case @state/display
               :explore-journeys :journeys
               :explore-stations :stations)
        columns (language/table-display type)]
    [:div 
     [:div.columns.is-centered>section>div.m-5>p.title (get (language/heading-by-language) type)]
     [:div.columns.is-centered.m-5>div.column.is-two-thirds
     [:table.table.is-narrow
      [:thead
       (into [:tr] (for [{key :key label :label} columns]
                     [:th {:key key :style {:text-align "center"}}
                      [:a {:on-click #(do (explore.state/update-sorting! key)
                                          (http/get-filtered-data))}
                       (explore.state/column-label-with-direction key label)]]))
       (into [:tr] (for [{key :key data-type :type} columns]
                     [:th {:key (str key "filter") :style {:text-align (align-by-type data-type)}}
                      (selector-input
                       {:width "40%"}
                       #(explore.state/update-filter-selector! key (-> % .-target .-value))
                       (filters/options-for-type data-type))
                      (filter-input key data-type {:width "40%"})]))]
      (into [:tbody]
            (for [row @explore.state/rows]
              (into [:tr]
                    (for [{key :key data-type :type} columns
                          :let [text (get row key)]]
                      [:td {:key (str row key) :style {:text-align (align-by-type data-type)}} 
                       (link-to-station row [type key] text)]))))]]]))

(defn get-more-rows []
  [:input.button
   {:type :submit
    :on-click #(do
                 (explore.state/add-limit-to-offset)
                 (http/get-data! false false))
    :value "Get more rows"
    :disabled (:got-all @explore.state/settings)}])
