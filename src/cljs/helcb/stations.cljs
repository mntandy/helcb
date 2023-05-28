(ns helcb.stations
  (:require [reagent.core :as r]
            [helcb.state :as state]
            [helcb.http :as http]
            [helcb.columns :as columns]
            [helcb.leaflet-utils :as leaflet]
            [helcb.commons :as commons]))

(def stations (r/atom {}))

(defn reset-to-initial! []
  (reset! stations {}))

(defn remove-station! [id]
  (swap! stations dissoc id))

(defn initialise-with-id! [id]
  (when-not (contains? @stations id)
    (swap! stations assoc id {})
    (http/get! :station-info 
              {:id id} 
              #(swap! stations assoc id {:top-five (:top-five %) :traffic (:traffic %) :row (:row %)})
              #(state/set-error-message! %))))

(defn ^:export openstation [id]
  (initialise-with-id! id))

(def tabs-options 
  [[:to-weekends "Returns" "weekends"]
   [:from-weekends "Departures" "weekends"]
   [:to-weekdays "Returns" "weekdays"]
   [:from-weekdays "Departures" "weekdays"]])

(defn traffic-tabs [id current-display]
  [:div.tabs.is-centered
    (into [:ul]
     (for [[key t1 t2] tabs-options]
       [(if (= current-display key) :li.is-active :li)
        [:a {:on-click #(swap! stations assoc-in [id :current-traffic] key)} t1 [:br] t2]]))])

(defn find-next-interval [x]
  (when x
    (loop [t (js/Math.ceil x)]
      (if (= (rem t 3) 0)
        [(/ t 3) 3]
        (if (= (rem t 2) 0)
          [(/ t 2) 2]
          (recur (inc t)))))))

(defn traffic-canvas [id traffic-data current-display]
  (when traffic-data
    (let [canvas-width (get-in @stations [id :canvas-width] nil)
          dataset (get traffic-data current-display {})
          [interval divisor] (find-next-interval (apply max (vals dataset)))
          boxWidth (/ canvas-width 26)
          scale (if (= divisor 2) 40 30)]
      (r/after-render
       (fn []
         (when-not canvas-width
           (swap! stations
                  assoc-in
                  [id :canvas-width]
                  (- (. (. js/document getElementById (str "columns" id)) -clientWidth) (+ 16 (. (. js/document getElementById (str "table" id)) -clientWidth) ))))))
      (into [:svg {:width canvas-width :height "115"}
             [:line.line {:x1 (* 1.8 boxWidth) :y1 "100" :x2 (* 1.8 boxWidth) :y2 "10"}]]
            (concat
             (for [[n y] (map list
                              (range interval (* interval (inc divisor)) interval)
                              (range 70 (- 70 (* scale divisor)) (- 0 scale)))]
               [:g
                [:text.bartext {:text-anchor "end" :x (* 1.5 boxWidth) :y (+ y 6)} (str n)]
                [:line.line {:x1 (* 1.5 boxWidth) :y1 y :x2 (* boxWidth 1.8) :y2 y}]])
             (for [hour (range 24)
                   :let [value (get dataset hour 0)
                         height (* (/ value interval) scale)]]
               [:rect.bar {:rx 5 :x (+ (* 2 boxWidth) (* hour boxWidth)) :y (- 100 height) :width boxWidth :height height}])
             (for [hour [0 4 8 12 16 20]]
               [:text.bartext {:x (+ (* 2 boxWidth) (* hour boxWidth)) :y "115"} (str hour)]))))))

(defn top-five-table [id top-five current-display]
  (let [dataset (get top-five current-display {})]
    [:table.table.is-narrow.is-size-6 {:id (str "table" id)}
     (into [:tbody]
            (for [row dataset 
              :let [{station_id :station_id
                     station_name :station_name} row]]
              [:tr
               [:td {:key station_id} station_id]
               [:td {:key station_name}
                [:a.has-text-link {:on-click #(initialise-with-id! station_id)} station_name]]]))]))

(def top "top five ")
(def avt " and average daily traffic ")
(def description {:from-weekdays (str "Weekdays " top "destinations" avt "from station")
                  :from-weekends (str "Weekends " top "destinations" avt "from station")
                  :to-weekdays (str "Weekdays " top "origins" avt "to station")
                  :to-weekends (str "Weekends " top "origins" avt "to station")})

(defn traffic [id data]
  (when data
    (let [current-display (get-in @stations [id :current-traffic] :from-weekdays)]
      [:div
       [:div.columns.is-centered
        [traffic-tabs id current-display]]
       [:div.columns.m-2
        [:div.column (get description current-display)]]
       [:div.columns.m-2
        [:div.column.is-one-third
         [top-five-table id (get data :top-five nil) current-display]]
        [:div.column.is-two-thirds
         [traffic-canvas id (get data :traffic nil) current-display]]]])))


(defn update-station-data! [stationid key data]
  (http/post! "/update-station" data
              (fn [_]
                (swap! stations assoc-in [stationid :row key] (:value data))
                (state/set-message! "Update successful!"))
              #(state/set-error-message! %)))

(defn set-edit-value! [stationid value]
  (swap! stations assoc-in [stationid :edit-value] value))

(defn set-edit [stationid key]
  (set-edit-value! stationid (get-in @stations [stationid :row key]))
  (swap! stations assoc-in [stationid :edit] key))

(defn edit-table [stationid row]
  (let [edit (get-in @stations [stationid :edit])]
    [:div.columns.is-centered.m-5
     [:table.table
      (into
       [:tbody]
       (for [{key :key label :label} (:stations columns/db-columns)
             :let [edit-value (get-in @stations [stationid :edit-value])
                   id (get row :id)
                   post #(update-station-data! stationid key {:id id :column (name key) :value edit-value})]
             :when (not= key :stationid)]
         (into [:tr [:td label]]
               (if (= edit key)
                 [[:td {:key id} (commons/text-input label edit-value #(set-edit-value! stationid %) post {:width "auto"})]
                  [:td {:key key}
                   [:a {:on-click post} "Save"] " " [:a {:on-click #(swap! stations assoc-in [stationid :edit] nil)} "Cancel"]]]
                 [[:td {:key id} (get row key)]
                  [:td {:key key} [:a {:on-click #(set-edit stationid key)} "Edit"]]]))))]]))

(defn single-station [id data]
  (let [display-edit (get-in @stations [id :display-edit])
        {stationid :stationid
         name :name
         osoite :osoite
         kaupunki :kaupunki
         x :x
         y :y} (:row data)]
    [:article.message {:id (str "article" id)}
     [:div.message-header.has-background-white-ter.has-text-grey-dark
      [:p (str "Station " stationid ": " name)]
      [:button.delete {:on-click #(remove-station! id) :aria-label "delete"}]]
     [:div.message-body.has-background-white 
      [:div.columns.m-3 {:id (str "columns" id)}
      [:div.column "Address: " osoite " " kaupunki] 
      [:div.column.has-text-right
       [commons/button "Show on map"
        (fn [] (leaflet/goto-station stationid name x y))]
       [commons/button
        (if display-edit "Close edit" "Edit info")
        (fn []
          (swap! stations assoc-in [id :display-edit] (not display-edit)))]]]
      (when display-edit (edit-table id (get data :row nil)))
      [traffic id data]]]))

(defn main []
  (into [:div] 
        (for [batch (partition 3 3 nil @stations)]
          (into [:div.columns.is-centered]
                (for [[id data] batch]
                  [:div.column.is-one-third [single-station id data]])))))