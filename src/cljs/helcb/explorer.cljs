(ns helcb.explorer
  (:require
   [helcb.stations :as stations]
   [helcb.filters :as filters]
   [helcb.commons :as commons]
   [helcb.columns :as columns]
   [helcb.language :as language]
   [reagent.core :as r]
   [helcb.state :as state]
   [helcb.leaflet-utils :as leaflet]
   [helcb.http :as http]))

(def initial-settings {:offset 0 :got-all false :limit 10 :sort-by-column "" :sort-direction "" :stationtip 0 :locationtip 0})

(def settings (r/atom initial-settings))

(def rows (r/atom []))

(defn reset-to-initial! []
  (reset! settings initial-settings)
  (reset! rows []))

(defn update-sorting! [column]
  (if (not= (:sort-by-column @settings) column)
    (swap! settings assoc
           :sort-by-column column
           :sort-direction "ASC")
    (swap! settings assoc
           :sort-direction (case (:sort-direction @settings) "ASC" "DESC" "DESC" "ASC" ""))))

(defn update-filter-for-column! [column data-type]
  (fn [value]
    (if (contains? (:filters @settings) column)
        (swap! settings assoc-in [:filters column :text] value)
        (swap! settings assoc-in [:filters column] {:text value :option (first (filters/options-for-type data-type))}))))

(defn update-filter-selector! [column value]
  (when (not= value "Filter")
    (swap! settings assoc-in [:filters column :option] value)))

(defn update-rows! [v reset]
  (swap! settings assoc :got-all (< (count v) (:limit @settings)))
  (reset! rows (if reset v (into @rows v))))

(defn column-label-with-direction [column label]
  (str label (when (= (:sort-by-column @settings) column) 
               (case (:sort-direction @settings)
                 "ASC" " \u2191"
                 "DESC" " \u2193"
                 ""))))

(defn prepare-for-request [reset add-offset-to-limit]
  (assoc (select-keys @settings [:sort-by-column :sort-direction :filters :offset :limit])
                 :offset (if reset "0" (str (:offset @settings)))
                 :limit (if add-offset-to-limit (str (+ (:limit @settings) (:offset @settings))) (str (:limit @settings)))))

(defn get-data! [reset add-offset-to-limit]
  (http/get! :data 
            (assoc (prepare-for-request reset add-offset-to-limit)
                            :name (case @state/display
                                    :explore-journeys "journeys"
                                    :explore-stations "stations"
                                    nil)) 
            #(update-rows! (:rows %) reset)
            #(state/set-error-message! %)))

(defn get-filtered-data []
  (if-let [element (filters/first-without-option (:filters @settings))]
    (state/set-error-message! (str "\"Filter\" is not a kind of filter for " (get-in @settings [:filters element :text] "") "."))
    (get-data! true true)))

(defn initialise []
  (reset-to-initial!)
  (get-data! true false))

(defn add-limit-to-offset-and-get-data []
  (swap! settings update :offset + (:limit @settings))
  (get-data! false false))

(defn filter-input [key data-type style]
  (commons/text-input
   key
   (get-in (:filters @settings) [key :text])
   (update-filter-for-column! key data-type)
   get-filtered-data
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

(defn select-station-with-id [id text]
  (let [stip (:stationtip @settings)] 
    (if (< stip 3) 
      [:div.stationlink
       [:a {:onMouseOver #(swap! settings assoc :stationtip (inc stip))
            :on-click #(stations/initialise-with-id! id)}
        text]
       [:span.stationtip "Click to open station info"]]
      [:a {:on-click #(stations/initialise-with-id! id)}
       text])))

(defn open-location-on-map [row text]
  (let [stip (:locationtip @settings)] 
    (if (< stip 3) 
      [:div.stationlink
       [:a {:onMouseOver #(swap! settings assoc :locationtip (inc stip))
            :on-click #(leaflet/goto-station (:stationid row) (or (:name row) (:nimi row)) (:x row) (:y row))}
        text]
       [:span.stationtip "Click to open location on map"]]
      [:a {:on-click #(leaflet/goto-station (:stationid row) (or (:name row) (:nimi row)) (:x row) (:y row))}
       text])))
  

(defn add-links [row type key text]
  (case type
    :journeys 
    (cond
      (some #{key} [:departure.name :departure.nimi :departure.namn]) (select-station-with-id (:departure_station_id row) text)
      (some #{key} [:return.name :return.nimi :return.namn]) (select-station-with-id (:return_station_id row) text)
      :else text)
    :stations 
    (cond 
      (some #{key} [:name :nimi :namn]) (select-station-with-id (:stationid row) text)
      (some #{key} [:osoite :adress]) (open-location-on-map row text) 
      :else text))) 

(defn table [type] 
   (let [columns (language/table-display type)]
    [:table.table.is-narrow
     [:thead
      (into [:tr] (for [{key :key label :label} columns]
                    [:th {:key key :style {:text-align "center"}}
                     [:a {:on-click #(do (update-sorting! key)
                                         (get-filtered-data))}
                      (column-label-with-direction key label)]]))
      (into [:tr] (for [{key :key data-type :type} columns]
                    [:th {:key (str key "filter") :style {:text-align (align-by-type data-type)}}
                     (selector-input
                      {:width "40%"}
                      #(update-filter-selector! key (-> % .-target .-value))
                      (filters/options-for-type data-type))
                     (filter-input key data-type {:width "40%"})]))]
     (into [:tbody]
           (for [row @rows]
             (into [:tr]
                   (for [{key :key data-type :type} columns
                         :let [text (get row key)]]
                     [:td {:key (str row key) :style {:text-align (align-by-type data-type)}}
                      (add-links row type key text)]))))]))

(defn get-more-rows []
  [:div.buttons.is-centered
   [:button.button
   {:on-click #(add-limit-to-offset-and-get-data)
    :disabled (:got-all @settings)}
   "Get more rows"]])

(def heading 
  {:journeys "Journeys by bike"  
   :stations "Bike stations"})

(defn main []
  (when-let [type (case @state/display
                    :explore-journeys :journeys
                    :explore-stations :stations
                    nil)]
    [:div.columns.is-centered>div.column.is-two-thirds
     [:div.has-background-white-ter.has-text-grey-dark.has-text-centered
      [:span {:style {:float "left"}} [language/selector]]
      [:button.delete.is-large {:style {:float "right"} :on-click #(state/reset-display!) :aria-label "delete"}]
      [:h1.title (get heading type)]]
     [:div.has-background-white
      [table type]]
     [get-more-rows]]))
