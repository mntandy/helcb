(ns helcb.station.state
  (:require [reagent.core :as r]
            [helcb.http :as http]
            [helcb.state :as state]))

(def initial-settings
  {:parent nil :row nil :edit nil :traffic nil :display-traffic :to-weekends})

(def settings (r/atom initial-settings))

(def canvas-width (r/atom 400))

(def parent (r/cursor settings [:parent]))
(def row (r/cursor settings [:row]))
(def edit (r/cursor settings [:edit]))
(def display-traffic (r/cursor settings [:display-traffic]))

(defn update-traffic-display! [k]
  (swap! settings assoc :display-traffic k))

(defn update-row! [column text]
  (swap! settings assoc-in [:row column] text))

(defn set-edit! [field]
  (swap! settings assoc
         :edit field))

(defn reset-to-initial! []
  (reset! settings initial-settings))

(defn initialise-with-row! [row] 
  (swap! settings assoc
         :parent @state/display
         :row row)
  (state/update-state! :single-station)
  (http/get :station-traffic  {:id (:stationid row)} #(swap! settings assoc :traffic (:traffic %))))

(defn initialise-with-id! [id]
  (swap! settings assoc
         :parent @state/display)
  (state/update-state! :single-station)
  (http/get :station-info {:id id} #(swap! settings assoc :traffic (:traffic %) :row (:row %))))