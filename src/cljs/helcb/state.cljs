(ns helcb.state
  (:require
   [reagent.core :as r]))


(def selector-options
  {:initial "What do you want to do?"
   :explore-journeys "Explore journeys"
   :explore-stations "Explore stations"
   :add-single-journey "Add a single journey"
   :add-multiple-journeys "Add multiple journeys"
   :add-single-station "Add single station"
   :add-multiple-stations "Add multiple stations"})

(def initial-state {:msg nil 
                    :display :initial})

(def state (r/atom initial-state))

(def display (r/cursor state [:display]))
(def msg (r/cursor state [:msg]))

(defn set-error-message! [text]
  (swap! state assoc :msg {:error true :text text}))

(defn set-message! [text]
  (swap! state assoc :msg {:error nil :text text}))

(defn is-importing [x]
  (some #{x} [:add-single-journey
              :add-multiple-journeys
              :add-single-station
              :add-multiple-stations]))

(defn is-exploring [x] 
  (some #{x} [:explore-journeys 
              :explore-stations]))

(defn close-msg! []
  (swap! state assoc :msg nil))

(defn update-state! [x]
  (swap! state assoc
         :msg nil
         :display x))

(defn reset-to-initial! []
  (reset! state initial-state))
  
