(ns helcb.stationsmap
  (:require
   [react :as react] 
   [helcb.http :as http]
   [helcb.state :as state]
   [helcb.leaflet-utils :as leaflet]
   [reagent.core :as r]))

(def map-width (r/atom 400))

(defn download-every-station-for-map []
  (http/get :stations-for-map nil
            (fn [data]
              (leaflet/create-every-station-layer (:stations data))
              (leaflet/show-every-station))
            #(state/set-error-message! %)))

(defn main []
  (react/useEffect (fn []
                     (reset! map-width (. (. js/document getElementById "navbar") -offsetWidth))
                     (leaflet/initialise-map)) (array []))
  [:div
   [:div.columns.is-centered.mt-3.mb-1 [:div {:style {:position "relative" :z-index "0" :width @map-width} :id "map"}]]
   [:div.columns.is-centered.mt-1.mb-3 
    [:div.column.is-narrow 
     (if-not (= @leaflet/stations-display :every-station)
      [:a {:on-click #(if-not @leaflet/every-station-layer 
                         (download-every-station-for-map)
                         (leaflet/show-every-station))} "Show all stations "]
      [:a {:on-click leaflet/hide-every-station} "Hide all stations "])]
    (when (= @leaflet/stations-display :some-stations)
      [:div.column.is-narrow [:a {:on-click leaflet/hide-some-stations} "Clear map"]])]])
