(ns helcb.stations
  (:require [reagent.core :as r]
            [helcb.state :as state]
            [helcb.http :as http]
            [helcb.columns :as columns]
            [helcb.commons :as commons]))

(def stations (r/atom {}))

(defn remove-station! [id]
  (swap! stations dissoc id))

(defn initialise-with-id! [id]
  (when-not (contains? @stations id)
    (http/get :station-info {:id id} #(swap! stations assoc id {:traffic (:traffic %) :row (:row %)}))))

(def tabs-options 
  [[:from-weekends "Departures" "weekends"]
   [:from-weekdays "Departures" "weekdays"]
   [:to-weekends "Returns" "weekends"]
   [:to-weekdays "Returns" "weekdays"]])

(defn traffic-tabs [id current-display]
  [:div.columns.is-centered 
   [:div.tabs.is-centered {:id (str "tabs" id)}
    (into [:ul]
     (for [[key t1 t2] tabs-options]
       [(if (= current-display key) :li.is-active :li)
        [:a {:on-click #(swap! stations assoc-in [id :current-traffic] key)} t1 [:br] t2]]))]])

(defn find-next-interval [x]
  (when x
    (loop [t (js/Math.ceil x)]
      (if (= (rem t 3) 0)
        [(/ t 3) 3]
        (if (= (rem t 2) 0)
          [(/ t 2) 2]
          (recur (inc t)))))))

(defn traffic-canvas [id traffic-data]
  (when traffic-data
    (let [current-display (get-in @stations [id :current-traffic] :from-weekdays)
          canvas-width (get-in @stations [id :canvas-width] nil)
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
                  (* 0.9 (. (. js/document getElementById (str "tabs" id)) -offsetWidth))))))
      [:div
       [traffic-tabs id current-display]
       [:div.columns.is-centered.m-5
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
                 [:text.bartext {:x (+ (* 2 boxWidth) (* hour boxWidth)) :y "115"} (str hour)])))]])))


(defn update-station-data! [stationid key data]
  (http/post! "/update-station" data
              (fn [_]
                (swap! stations assoc-in [stationid :edit] nil)
                (swap! stations assoc-in [stationid :row key] (:value data))
                (state/set-message! "Update successful!"))))

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
         kaupunki :kaupunki} (:row data)]
    [:article.message
     [:div.message-header.has-background-white-ter.has-text-grey-dark
      [:p (str "Station " stationid ": " name)]
      [:button.delete {:on-click #(remove-station! id) :aria-label "delete"}]]
     [:div.message-body.has-background-white
      [:div.columns.m-3
      [:div.column "Address: " osoite " " kaupunki] 
      [:div.column.has-text-right
       [commons/button
        (if display-edit "Close edit" "Edit info")
        (fn []
          (swap! stations assoc-in [id :display-edit] (not display-edit)))]]]
      (when display-edit (edit-table id (get data :row nil)))
      [traffic-canvas id (get data :traffic nil)]]]))

(defn station-view []
  (println @stations)
  (into [:div] 
        (for [batch (partition 3 3 nil @stations)]
          (into [:div.columns.is-centered]
                (for [[id data] batch]
                  [:div.column.is-one-third [single-station id data]])))))