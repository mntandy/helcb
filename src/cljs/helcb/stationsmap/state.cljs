(ns helcb.stationsmap.state
  (:require
   [helcb.http :as http]
   [helcb.stations :as stations]
   [reagent.core :as r]))

(def leaflet-map (r/atom nil))
(def map-width (r/atom 400))
(def stations-display (r/atom false))
(def stations-markers (r/atom nil))
(def stations-layer (r/atom nil))

(defn gotostation [id]
  (helcb.stations/initialise-with-id! id))

(defn to-float [s]
  (. js/Number parseFloat s))

(defn popup-html [name stationid]
  (str "<b>" name "</b><br /><a onclick='helcb.stationsmap.state.gotostation(\"" stationid "\")'>Open station info</a><br />
                             <a onclick='helcb.stationsmap.state.gotojourneys()'>Open journeys view</a>"))

(defn create-stations-markers [data]
  (apply array
                (for [{stationid :stationid
                       name :name
                       x :x
                       y :y} data]
                  (. (. js/L marker (array (to-float y) (to-float x)))
                     bindPopup (popup-html name stationid)))))

(defn create-stations-layer [data]
  (reset! stations-layer (. js/L layerGroup (create-stations-markers data))))

(defn add-stations-layer-to-map []
  (reset! stations-display true)
  (. @stations-layer addTo @leaflet-map))

(defn hide-stations []
  (reset! stations-display false)
  (. @stations-layer remove @leaflet-map))

(defn download-stations-for-map [add-to-map]
  (http/get
   :stations-for-map nil
   (fn [data]
     (create-stations-layer (:stations data))
     (when add-to-map (add-stations-layer-to-map)))))
  
(defn show-stations []
  (if (empty? @stations-markers)
    (download-stations-for-map true)
    (add-stations-layer-to-map)))
