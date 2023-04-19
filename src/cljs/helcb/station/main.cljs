(ns helcb.station.main
  (:require [reagent.core :as r]
            [react :as react]
            [helcb.station.state :as station.state]
            [helcb.state :as state]
            [helcb.http :as http]
            [helcb.columns :as columns]
            [helcb.commons :as commons]
            [cljsjs.chartjs]))

(defn buttons []
  [:div.columns.m-3>div.column.is-offset-7.is-1.has-text-right
   [commons/button "Back to list" (fn [] 
                                    (state/update-state! @station.state/parent)
                                    (station.state/reset-to-initial!))]])

(defn update-station-data! [data]
  (http/post! "/update-station" data
            (fn [_]
              (station.state/set-edit! nil)
              (state/set-message! "Update successful!"))))

(def tabs-options 
  [[:from-weekends "Departures" "weekends"]
   [:from-weekdays "Departures" "weekdays"]
   [:to-weekends "Returns" "weekends"]
   [:to-weekdays "Returns" "weekdays"]])

(defn traffic-tabs [tabs]
  [:div.columns.is-centered 
   [:div.tabs.is-centered {:ref tabs}
    (into [:ul]
     (for [[key t1 t2] tabs-options]
       [(if (= @station.state/display-traffic key) :li.is-active :li)
        [:a {:on-click #(station.state/update-traffic-display! key)} t1 [:br] t2]]))]])

(defn find-next-interval [x]
  (when x
    (loop [t (js/Math.ceil x)]
      (if (= (rem t 3) 0)
        [(/ t 3) 3]
        (if (= (rem t 2) 0)
          [(/ t 2) 2]
          (recur (inc t)))))))

(defn traffic-canvas [tabs]
  (let [dataset (@station.state/display-traffic (:traffic @station.state/settings))
        width (r/atom 400)
        [interval divisor] (find-next-interval (apply max (vals dataset)))
        boxWidth (/ @width 25)
        scale (if (= divisor 2) 40 30)]
    (react/useEffect
     (fn [] (reset! width (* 0.9 (. (. tabs -current) -offsetWidth))))
     (array []))
    [:div 
     [:div.columns.is-centered.m-5
      (into [:svg {:width @width :height "115"}
             [:line.line {:x1 (* boxWidth 0.8) :y1 "100" :x2 (* boxWidth 0.8) :y2 "10"}]]
            (concat
             (for [[n y] (map list
                              (range interval (* interval (inc divisor)) interval)
                              (range 70 (- 70 (* scale divisor)) (- 0 scale)))]
               [:g 
                [:text.bartext {:x 0 :y (+ y 6)} (str n)]
                [:line.line {:x1 (* boxWidth 0.6) :y1 y :x2 (* boxWidth 0.9) :y2 y}]])
             (for [hour (range 24)
                   :let [value (get dataset hour 0)
                         height (* (/ value interval) scale)]] 
               [:rect.bar {:rx 5 :x (+ boxWidth (* hour boxWidth)) :y (- 100 height) :width boxWidth :height height}])
             (for [hour [0 4 8 12 16 20]]
               [:text.bartext {:x (+ boxWidth (* hour boxWidth)) :y "115"} (str hour)])))]]))

(defn loading-data []
  [:div.columns.is-centered.m-5 "Loading data..."])

(defn station-view []
  (println @station.state/settings)
  (let [tabs (react/useRef)]
  (when (= @state/display :single-station)
    [:div 
     [:div.columns.is-centered>h2.title (str "Station " (get @station.state/row :stationid))]
     [buttons]
     [:f> traffic-tabs tabs]
     (if (:traffic @station.state/settings) [:f> traffic-canvas tabs] [loading-data])
     [:div.columns.is-centered.m-5
      [:table.table
       (into 
        [:tbody]
        (for [{key :key label :label} (:stations columns/db-columns)
              :let [value (get @station.state/row key)
                    id (get @station.state/row :id)
                    post #(update-station-data! {:id id :column (name key) :value value})]
              :when (not= key :stationid)]
          (into [:tr [:td label]]
                (if (= @station.state/edit key)
                  [[:td {:key id} (commons/text-input label value #(station.state/update-row! key %) post {:width "auto"})]
                   [:td {:key key}
                    [:a {:on-click post} "Save"] " " [:a {:on-click #(station.state/set-edit! nil)} "Cancel"]]]
                  [[:td {:key id} value]
                   [:td {:key key} [:a {:on-click #(station.state/set-edit! key)} "Edit"]]]))))]]])))