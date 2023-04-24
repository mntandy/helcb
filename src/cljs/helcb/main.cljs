(ns helcb.main
  (:require [reagent.dom :as dom]
            [helcb.notification :refer [notification]]
            [helcb.import.main :refer [importer]]
            [helcb.explore.main :refer [explorer]]
            [helcb.stations :refer [station-view]]
            [helcb.stationsmap.main :refer [stationsmap]]
            [helcb.state :as state]
            [helcb.menu :refer [menu]]))

(defn main []
  (println @state/state)
  [:div
   [menu] 
   [notification]
   [:f> stationsmap]
   [importer]
   [explorer]
   [station-view]])

(dom/render
 [main]
 (.getElementById js/document "app"))