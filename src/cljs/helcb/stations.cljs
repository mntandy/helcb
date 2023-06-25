(ns helcb.stations
  (:require [reagent.core :as r]
            [react :as react]
            [helcb.state :as state]
            [helcb.http :as http]
            [helcb.leaflet-utils :as leaflet]
            [helcb.commons :as commons]))

(def stations (r/atom {}))

(defn reset-to-initial! []
  (reset! stations {}))

(defn remove-station! [id]
  (swap! stations dissoc id))

(defn initialise-with-id! [id]
  (if (contains? @stations id)
    (commons/scroll-to-element-by-id (str "station-info" id))
    (do
      (swap! stations assoc id {})
      (http/get! :station-info
                 {:id id}
                 #(swap! stations assoc id {:top-five (:top-five %) :traffic (:traffic %) :row (:row %)})
                 #(state/set-error-message! %)))))

(defn ^:export openstation [id]
  (initialise-with-id! id))

(def tabs-options
  [[:to-weekends "Returns weekends"]
   [:from-weekends "Departures weekends"]
   [:to-weekdays "Returns weekdays"]
   [:from-weekdays "Departures weekdays"]])

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
    (let [canvas-width 300
          dataset (get traffic-data current-display {})
          [interval divisor] (find-next-interval (apply max (vals dataset)))
          boxWidth (/ canvas-width 26)
          scale (if (= divisor 2) 40 30)]
        (into [:svg.mb-3 {:width canvas-width :height "115"}
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

(def top-five-text {:from-weekdays "destinations on weekdays"
                :from-weekends "destinations on weekends"
                :to-weekdays "origins on weekdays"
                :to-weekends "origins on weekends"})

(def average-text  {:from-weekdays "departures on weekdays"
                    :from-weekends "departures on weekends"
                    :to-weekdays "returns on weekdays"
                    :to-weekends "returns on weekends"})


(defn top-five-table [id top-five current-display]
  (let [dataset (get top-five current-display {})]
    (into [:ul]
           (for [row dataset
                 :let [{station_id :station_id
                        station_name :station_name} row]]
             [:li {:key station_name} [:span.icon-text [:span station_name] [:a.stationlink.icon {:on-click #(initialise-with-id! station_id)}
                                                    [:img {:src "svg/open-in-new.svg"}][:span.stationtip "Click to open station"]]]]))))

(defn traffic [id data]
  (when data
    (let [current-display (get-in @stations [id :current-traffic] :from-weekdays)]
      (into [:nav.panel]
            (for [[key text] tabs-options]
              [:div 
               (if (not= current-display key)
                 [:a.panel-block.has-text-link {:on-click #(swap! stations assoc-in [id :current-traffic] key)} text] 
               [:div 
                [:a.panel-block.is-active {:on-click #(swap! stations assoc-in [id :current-traffic] nil)} [:b text]]
                 [:div 
                  [:h6.m-3 "Top 5 " (get top-five-text current-display)]
                  [:div.columns.is-visible.m-3 [top-five-table id (get data :top-five nil) current-display]]]
                 [:div
                  [:h6.m-3 "Daily " (get average-text current-display)]
                  [:div.columns.is-visible.m-3
                   [traffic-canvas id (get data :traffic nil) current-display]]]])])))))

(defn single-station [id data]
  (let [{stationid :stationid
         name :name
         osoite :osoite
         kaupunki :kaupunki
         x :x
         y :y} (:row data)]
    (react/useEffect (fn []
                      (commons/scroll-to-element-by-id (str "station-info" id)))
                     (array []))
    [:div.card {:id (str "station-info" id)}
     [:header.card-header.has-background-white-ter.has-text-grey-dark
      [:p.card-header-title "Station " stationid ": " name]
      [:button.delete {:on-click #(remove-station! id) :aria-label "close"}]]
     [:div.card-content.has-background-white
      [:div.content
       [:div.card.mb-3
        [:div.card-content.has-background-white
         [:div.content 
          [:div.columns.is-visible
           [:div.column "Address: "]
           [:div.column.has-text-right [:a {:on-click (fn []
                                        (commons/scroll-to-element-by-id "map")
                                        (leaflet/goto-station stationid name x y))} "Show on map"]]]
          [:p osoite " " kaupunki]]]]
      [traffic id data]]]]))


(defn equal-partitions [n m]
  (partition n n nil m))

(defn main []
  (let [[current-size set-current-size] (react/useState 3)
        update-current-size (fn []
                              (let [n (. js/window -innerWidth)]
                                (cond
                                  (and (< n 800) (not= current-size 1)) (set-current-size 1)
                                  (and (<= 800 n) (< n 1200) (not= current-size 2)) (set-current-size 2)
                                  (and (<= 1200 n) (< n 1600)) (set-current-size 3)
                                  (>= n 1600) (set-current-size 4))))]
    (react/useEffect (fn []
                       (. js/window addEventListener "resize" update-current-size)
                       (fn [] (. js/window removeEventListener "resize" update-current-size)))
                     (array []))
    (when (seq @stations)
      (into [:div]
            (for [batch (equal-partitions current-size @stations)]
              (into [:div.columns.is-centered.is-mobile]
                    (for [[id data] batch]
                      [:div {:style {:padding 10 :width 400} :id (str "station" id)} [:f> single-station id data]])))))))