(ns helcb.main
  (:require [reagent.dom :as dom]
            [helcb.select :refer [select]]
            [helcb.notification :refer [notification]]
            [helcb.import.main :refer [importer]]
            [helcb.explore.main :refer [explorer]]
            [helcb.station.main :refer [station-view]]))

(defn main []
  (fn []
    [:div
     [select]
     [notification]
     [importer]
     [explorer]
     [station-view]
     ]))

(dom/render
 [main]
 (.getElementById js/document "app"))