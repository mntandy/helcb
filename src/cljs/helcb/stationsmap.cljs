(ns helcb.stationsmap
  (:require
   [react :as react] 
   [helcb.http :as http]
   [helcb.state :as state]
   [helcb.leaflet-utils :as leaflet]
   [reagent.core :as r]))

(def map-width (r/atom 400))

(def show-loader (r/atom false))
(def show-apology (r/atom false))

(defn download-every-station-for-map []
  (reset! show-loader true) 
  (js/setTimeout (fn [] (reset! show-apology true)) 2000)
  (http/get! :stations-for-map nil
            (fn [data]
              (reset! show-loader false)
              (reset! show-apology false)
              (leaflet/create-every-station-layer (:stations data))
              (leaflet/show-every-station))
            #(state/set-error-message! %)))

(defn reset-map-width [] (let [windowWidth (. js/window -innerWidth)]
                           (reset! map-width (- (min 500 windowWidth) 16))))

(defn main []
  (react/useEffect (fn []
                     (reset-map-width)
                     (leaflet/initialise-map)
                     (. js/window addEventListener "resize" reset-map-width)
                     (fn [] (. js/window removeEventListener "resize" reset-map-width)))
                   (array []))
  [:div
   [:div.columns.is-centered.mt-3.mb-1.is-mobile [:div {:style {:position "relative" :z-index "0" :width @map-width} :id "map"}]]
   [:div.columns.is-centered.mt-1.mb-3.is-mobile
    [:div.column.is-narrow 
     (if-not (= @leaflet/stations-display :every-station)
      (if @show-loader 
        [:div.centered-flex.row [:div.map-loader.loader-colors] (and @show-apology [:div "apologies for the slow database..."])] 
        [:a {:on-click #(if-not @leaflet/every-station-layer 
                         (download-every-station-for-map)
                         (leaflet/show-every-station))} 
        "Show all stations "])
      [:a {:on-click leaflet/hide-every-station} "Hide all stations "])]
    (when (= @leaflet/stations-display :some-stations)
      [:div.column.is-narrow [:a {:on-click leaflet/hide-some-stations} "Clear map"]])]])
