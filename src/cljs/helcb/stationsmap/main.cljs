(ns helcb.stationsmap.main
  (:require
   [react :as react] 
   [helcb.stationsmap.state :as stationsmap.state]))

(defn stationsmap []
  (react/useEffect (fn []
                     (reset! stationsmap.state/map-width (. (. js/document getElementById "navbar") -offsetWidth))
                     (reset! stationsmap.state/leaflet-map (. (. js/L map "map") setView #js [60.1661, 24.9458] 13))
                     (. (. js/L tileLayer "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
                           #js {:maxZoom 19 :attribution "<a href='http://www.openstreetmap.org/copyright'>OpenStreetMap</a>"})
                        addTo @stationsmap.state/leaflet-map)) (array []))
  [:div
   [:div.columns.is-centered.m-3 [:div {:style {:position "relative" :z-index "0" :width @stationsmap.state/map-width} :id "map"}]]
   (if-not @stationsmap.state/stations-display 
     [:div.columns.is-centered.m-3 [:a {:on-click stationsmap.state/show-stations} "Show all stations"]]
     [:div.columns.is-centered.m-3 [:a {:on-click stationsmap.state/hide-stations} "Hide all stations"]])])
