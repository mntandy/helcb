(ns helcb.explore.table
  (:require 
   [helcb.state :as state]
   [helcb.explore.state :as explore.state]
   [helcb.stations :as stations]
   [helcb.filters :as filters]
   [helcb.language :as language]
   [helcb.commons :as commons]))

(defn filter-input [key data-type style]
  (commons/text-input
   key
   (get-in (explore.state/filters) [key :text])
   (explore.state/update-filter-for-column! key data-type)
   explore.state/get-filtered-data
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

(defn select-station-with-row [row text]
  [:a {:on-click 
       (fn [] (stations/initialise-with-id! (:stationid row)))}
   text])

(defn select-station-with-id [id text]
  [:a {:on-click
       (fn [] (stations/initialise-with-id! id))}
   text])

(defn link-to-station [row type key text]
  (case type
    :journeys 
    (if (some #{key} [:departure.name :departure.nimi :departure.namn]) 
      (select-station-with-id (:departure_station_id row) text)
      (if (some #{key} [:return.name :return.nimi :return.namn]) 
        (select-station-with-id (:return_station_id row) text)
        text))
    :stations 
    (if (some #{key} [:name :nimi :namn]) 
      (select-station-with-row row text)
      text))) 

(defn table []
  (println @explore.state/settings)
  (println @explore.state/rows) 
  (let [type (case @state/display
               :explore-journeys :journeys
               :explore-stations :stations)
        columns (language/table-display type)]
    [:div 
     [:div.columns.is-centered>section>div.m-5>p.title (get (language/heading-by-language) type)]
     [:div.columns.is-centered>div.column.is-two-thirds.has-text-right [language/selector]]
     [:div.columns.is-centered.m-5>div.column.is-two-thirds
     [:table.table.is-narrow
      [:thead
       (into [:tr] (for [{key :key label :label} columns]
                     [:th {:key key :style {:text-align "center"}}
                      [:a {:on-click #(do (explore.state/update-sorting! key)
                                          (explore.state/get-filtered-data))}
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
                       (link-to-station row type key text)]))))]]]))

(defn get-more-rows []
  [:input.button
   {:type :submit
    :on-click #(explore.state/add-limit-to-offset-and-get-data)
    :value "Get more rows"
    :disabled (:got-all @explore.state/settings)}])
