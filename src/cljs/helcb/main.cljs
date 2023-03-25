(ns helcb.main
  (:require [reagent.dom :as dom]
            [helcb.select :refer [select]]
            [helcb.notification :refer [notification]]
            [helcb.add :refer [importer]]
            [helcb.explore :refer [explorer]]
            [helcb.station-view :refer [station-view]]))

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