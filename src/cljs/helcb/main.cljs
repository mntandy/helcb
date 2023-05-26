(ns helcb.main
  (:require [reagent.dom :as dom]
            [helcb.notification :as notification]
            [helcb.importer :as importer]
            [helcb.explorer :as explorer]
            [helcb.stations :as stations]
            [helcb.stationsmap :as stationsmap]
            [helcb.state :as state]
            [helcb.menu :refer [menu]]))

(defn main []
  [:div
   [menu] 
   [notification/main]
   (if (some #{@state/display} (keys importer/options))
     [importer/main]
     [:div
      [:f> stationsmap/main]
      [stations/main]
      [explorer/main]])])

(dom/render
 [main]
 (.getElementById js/document "app"))