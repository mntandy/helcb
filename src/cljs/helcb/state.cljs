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

(def button-options
  {:initial
   {:disabled true :text "Proceed!" :on-click nil}
   :selected
   {:disabled false :text "Proceed!" :on-click :open}
   :explore
   {:disabled false :text "Close" :on-click :reset}
   :add
   {:disabled false :text "Cancel" :on-click :reset}
   :single-station
   {:disabled false :text "Close" :on-click :reset}
   })


(def initial-state {:msg nil
                    :selected :initial 
                    :display :initial
                    :main-button (:initial button-options)})

(def state (r/atom initial-state))

(def display (r/cursor state [:display]))
(def msg (r/cursor state [:msg]))
(def selected (r/cursor state [:selected]))
(def main-button (r/cursor state [:main-button]))

(defn set-error-message! [text]
  (swap! state assoc :msg {:error true :text text}))

(defn set-message! [text]
  (swap! state assoc :msg {:error nil :text text}))

(defn csv-import-success! [result]
  (swap! state assoc
         :msg {:text [:div [:p "Imported " (:imported result) " rows out of " (:line result) ". "]
                      [:p "The following lines were not imported:" [:br]
                      (apply str (first (:ignored result)) (map (fn [x y] (str x y)) (repeat ", ") (rest (:ignored result))))
                      "."]]}
         :main-button (get button-options :explore)))

(defn is-initial []
  (= @display :initial))
(defn is-not-initial []
  (not= @display :initial))

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
         :display x
         :main-button (get button-options (if (is-exploring x)
                                            :explore
                                            :add))))

(defn update-selector! [selection]
  (let [k (keyword selection)]
    (swap! state assoc
           :selected k
           :main-button (get button-options (if (= k :initial) :initial :selected)))))

(defn reset-to-initial! []
  (reset! state initial-state))
  
