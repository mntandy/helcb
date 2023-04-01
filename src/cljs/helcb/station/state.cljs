(ns helcb.station.state
  (:require [reagent.core :as r]))

(def initial-settings
  {:parent nil :row nil :edit nil})

(def settings (r/atom initial-settings))

(def parent (r/cursor settings [:parent]))

(def row (r/cursor settings [:row]))

(def edit (r/cursor settings [:edit]))

(defn update-row! [column text]
  (swap! settings assoc-in [:row column] text))

(defn set-edit! [field]
  (swap! settings assoc
         :edit field))

(defn reset-to-initial! []
  (reset! settings initial-settings))

(defn initialise! [parent row]
  (swap! settings assoc
         :parent parent
         :row row))
