(ns helcb.leaflet-utils
  (:require [reagent.core :as r]
            [leaflet :as L]))

(def leaflet-map (r/atom nil))

(def every-station-layer (r/atom nil))
(def some-stations-layer (r/atom nil))
(def added-stations (r/atom (hash-set)))
(def stations-display (r/atom :initial))

(defn set-stations-display! [v]
  (reset! stations-display v))

(defn popup-html [name stationid]
  (str "<b>" name "</b><br /><a onclick='helcb.stations.openstation(\"" stationid "\")'>Open station info</a><br />"))

(defn display-journey [x1 y1 x2 y2]
  (println "displaying")
  (. @leaflet-map fitBounds (. (. (. js/L polyline
                                    (array (array (. js/Number parseFloat y1), (. js/Number parseFloat x1)) (array (. js/Number parseFloat y2), (. js/Number parseFloat x2)))
                                    #js {:color "red"}) 
                                 addTo @leaflet-map) 
                              getBounds)))

(defn create-marker [stationid name x y] 
  (. (. js/L marker (array (. js/Number parseFloat y) (. js/Number parseFloat x)))
       bindPopup (popup-html name stationid)))

(defn clear-some-stations-layer []
  (when (and @some-stations-layer @leaflet-map)
    (. @some-stations-layer remove @leaflet-map)
    (reset! some-stations-layer nil)
    (reset! added-stations (hash-set))))

(defn add-every-station-layer-to-map []
  (set-stations-display! :every-station)
  (. @every-station-layer addTo @leaflet-map))

(defn hide-every-station [] 
  (set-stations-display! :initial)
  (when (and @every-station-layer @leaflet-map)
    (. @every-station-layer remove @leaflet-map)))

(defn hide-some-stations []
  (set-stations-display! :initial)
  (clear-some-stations-layer))

(defn show-every-station []
  (when (= @stations-display :some-stations)
    (clear-some-stations-layer))
  (add-every-station-layer-to-map))

(defn create-stations-markers [data]
  (apply array
         (for [{stationid :stationid
                name :name
                x :x
                y :y} data]
           (create-marker stationid name x y))))

(defn add-to-some-stations-layer [stationid name x y]
  (when-not (contains? @added-stations stationid)
    (if-not @some-stations-layer
      (reset! some-stations-layer (. js/L layerGroup (array (create-marker stationid name x y))))
      (. @some-stations-layer addLayer (create-marker stationid name x y)))
    (swap! added-stations conj stationid)))

(defn create-every-station-layer [data]
  (reset! every-station-layer (. js/L layerGroup (create-stations-markers data))))

(defn add-and-display-some-stations [stationid name x y]
  (add-to-some-stations-layer stationid name x y)
  (. @some-stations-layer addTo @leaflet-map))

(defn flyto [x y]
  (. @leaflet-map flyTo (array (. js/Number parseFloat y) (. js/Number parseFloat x))))

(defn goto-station [stationid name x y]
  (flyto x y)
  (when-not (= @stations-display :every-station)
    (set-stations-display! :some-stations)
    (add-and-display-some-stations stationid name x y)))

(defn initialise-map []
  (reset! leaflet-map (. (. js/L map "map") setView #js [60.1661, 24.9458] 13))
  (. (. js/L tileLayer "https://tile.openstreetmap.org/{z}/{x}/{y}.png", 
        #js {:maxZoom 19 :attribution "<a href='http://www.openstreetmap.org/copyright'>OpenStreetMap</a>"})
   addTo @leaflet-map))

