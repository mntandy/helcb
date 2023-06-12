(ns helcb.explorer
  (:require
   [helcb.stations :as stations]
   [helcb.filters :as filters]
   [helcb.commons :as commons]
   [helcb.language :as language]
   [reagent.core :as r]
   [react :as react]
   [helcb.state :as state]
   [helcb.leaflet-utils :as leaflet]
   [helcb.http :as http]))

(def options [:explore-journeys :explore-stations])

(def initial-settings {:scroll true :offset 0 :got-all false :limit 10 :sort-by-column "" :sort-direction "" :stationtip 0 :locationtip 0})

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
  (let [stip (:locationtip @settings)
        scroll-and-go-to-station (fn []
               (commons/scroll-to-element-by-id "map")
               (leaflet/goto-station (:stationid row) (or (:name row) (:nimi row)) (:x row) (:y row)))] 
    (if (< stip 3) 
      [:div.stationlink
       [:a {:onMouseOver #(swap! settings assoc :locationtip (inc stip))
            :on-click scroll-and-go-to-station}
        text]
       [:span.stationtip "Click to open location on map"]]
      [:a {:on-click scroll-and-go-to-station}
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
     [:div {:style {:overflow-x "auto"}}
      [:table.table.is-narrow
       [:thead
        (into [:tr] (for [{key :key label :label} columns]
                      [:th {:key key :style {:text-align "center"}}
                       [:a {:on-click #(do (update-sorting! key)
                                           (get-filtered-data))}
                        (column-label-with-direction key label)]]))]
       (into [:tbody]
             (for [row @rows]
               (into [:tr]
                     (for [{key :key data-type :type} columns
                           :let [text (get row key)]]
                       [:td {:key (str row key) :style {:text-align (align-by-type data-type)}}
                        (add-links row type key text)]))))]]))

(defn get-more-rows []
  [:div.buttons.is-centered
   [:button.button
   {:on-click #(add-limit-to-offset-and-get-data)
    :disabled (:got-all @settings)}
   "Get more rows"]])

(def heading 
  {:journeys "Journeys by bike"  
   :stations "Bike stations"})

(defn remove-filter [key]
  (swap! settings assoc
         :filters (dissoc (get @settings :filters) key)))

(defn update-filter-if-value-not-empty [key filter]
  (when (seq (get-in @settings [:filters key :text]))
    (swap! settings assoc-in [:filters key :option] filter)))

(defn filter-input [key filter]
  (commons/text-input
   key
   (get-in (:filters @settings) [key :text])
   (fn [value]
     (if (seq value)
       (swap! settings assoc-in [:filters key] {:text value :option filter})
       (remove-filter key)))
   get-filtered-data
   {}))

(defn selector-input [value handler options]
  [:select {:value value :on-change handler}
   (for [o options]
     [:option {:key o :value o} o])])

(defn add-filter [type]
  (let [columns (language/table-display type)
        labels (map :label columns)
        label->data-type (into {} (mapv (fn [c] [(:label c) (:type c)]) columns))
        label->key (into {} (mapv (fn [c] [(:label c) (:key c)]) columns))
        [label set-label] (react/useState "Add filter")
        [filter set-filter] (react/useState "")
        reset (fn [] 
                (set-label "Add filter")
                (set-filter ""))]
    (into [:div.field.is-grouped.is-grouped-multiline
           [:p.control (selector-input
            label
            #(let [new-label (-> % .-target .-value)]
               (set-label new-label)
               (set-filter (first (filters/options-for-type (get label->data-type new-label [""])))))
            (into ["Add filter"] labels))]] 
          (when-let [key (get label->key label)] 
            [
             [:p.control.has-addons
               (selector-input
                filter
                #(let [new-filter (-> % .-target .-value)]
                   (set-filter new-filter)
                   (update-filter-if-value-not-empty key new-filter))
                (filters/options-for-type (get label->data-type label)))
              (filter-input
               key
               filter)]
             [:p.control
              [:button.button.is-small {:on-click get-filtered-data} "Update!"]
              [:button.button.is-small {:on-click reset} "Close"]]]))))

(defn current-filters [type]
  (let [columns (language/table-display type)
        key->label (into {} (mapv (fn [c] [(:key c) (:label c)]) columns))]
  (into [:div.field.is-grouped.is-grouped-multiline]
        (for [[key _] (:filters @settings)]
          [:div.control
           [:div.tags.has-addons
            [:span.tag (get key->label key)]
            [:a.tag.is-delete {:on-click #(remove-filter key)}]]]))))

(defn main []
  (react/useEffect (fn [] (commons/scroll-to-element-by-id "explorer"))
                   (array []))
  (let [type (case @state/display
               :explore-journeys :journeys
               :explore-stations :stations
               nil)]
    [:div.container.is-max-desktop {:id "explorer"}
     [:div.has-background-white-ter.has-text-grey-dark.has-text-centered
      [:span {:style {:float "left"}} [language/selector]]
      [:button.delete.is-large {:style {:float "right"} :on-click #(state/reset-display!) :aria-label "delete"}]
      [:h1.title (get heading type)]]
     [:div
      [:f> add-filter type]
      [:f> current-filters type]]
     [:div.has-background-white {:style {:display "flex" :justify-content "center"}}
      [table type]]
     [get-more-rows]]))
