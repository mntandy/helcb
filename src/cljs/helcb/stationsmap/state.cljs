(ns helcb.stationsmap.state
  (:require
   [helcb.http :as http]
   [helcb.station.state :as station.state]
   [reagent.core :as r]))

(def leaflet-map (r/atom nil))
(def map-width (r/atom 400))
(def map-added (r/atom false))

(defn gotostation [id]
  (helcb.station.state/initialise-with-id! id))

(defn to-float [s]
  (. js/Number parseFloat s))

(defn popup-html [name stationid]
  (str "<b>" name "</b><br /><a onclick='helcb.stationsmap.state.gotostation(\"" stationid "\")'>Open station info</a><br />
                             <a onclick='helcb.stationsmap.state.gotojourneys()'>Open journeys view</a>"))

(defn add-stations-to-map []
  (http/get
   :stations-for-map nil
   (fn [data]
     (reset! map-added true)
     (doseq [{stationid :stationid
             name :name
             x :x
             y :y} (:stations data)] 
      (. (. (. js/L marker (array (to-float y) (to-float x))) addTo @leaflet-map)
         bindPopup (popup-html name stationid))))))
