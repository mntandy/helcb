(ns helcb.state
  (:require
   [reagent.core :as r]))

(def initial-state {:msg nil 
                    :display :initial})

(def state (r/atom initial-state))

(def display (r/cursor state [:display]))
(def msg (r/cursor state [:msg]))

(defn set-error-message! [text]
  (swap! state assoc :msg {:error true :text text}))

(defn set-message! [text]
  (swap! state assoc :msg {:error nil :text text}))

(defn is-exploring [x] 
  (some #{x} [:explore-journeys 
              :explore-stations]))

(defn close-msg! []
  (swap! state assoc :msg nil))

(defn update-state! [x]
  (swap! state assoc
         :msg nil
         :display x))

(defn reset-display! []
  (swap! state assoc :display :initial))

(defn reset-to-initial! []
  (reset! state initial-state))
  
