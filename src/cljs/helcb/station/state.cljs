(ns helcb.station.state
  (:require [reagent.core :as r]))

(def initial-settings
  {:parent nil :row nil :edit nil :traffic nil :display-traffic :to-weekends})

(def settings (r/atom initial-settings))

(def parent (r/cursor settings [:parent]))

(def row (r/cursor settings [:row]))

(def edit (r/cursor settings [:edit]))

(def display-traffic (r/cursor settings [:display-traffic]))

(defn update-traffic-display! [k]
  (swap! settings assoc :display-traffic k))

(defn update-row! [column text]
  (swap! settings assoc-in [:row column] text))

(defn update-traffic! [traffic]
  (swap! settings assoc :traffic traffic))

(defn set-edit! [field]
  (swap! settings assoc
         :edit field))

(defn reset-to-initial! []
  (reset! settings initial-settings))

(defn initialise! [parent row] 
  (swap! settings assoc
         :parent parent
         :row row))
