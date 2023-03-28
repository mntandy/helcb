(ns helcb.station.main
  (:require [helcb.station.state :as station.state]
            [helcb.state :as state]
            [helcb.language :as language]))
  
(defn back-to-list-button []
  [:div.columns.m-3>div.column.is-offset-7.is-1.has-text-right
   [:input.button
    {:type :submit
     :value "Back to list"
     :on-click #(state/update-state! @station.state/parent)}]])

(defn station-view []
  (println @station.state/settings)
  (when (= @state/display :single-station)
    (let [station ((language/row-by-language :stations) @station.state/row)] 
      [:div
      [back-to-list-button] 
       [:div.columns.is-centered 
       [:table.table
        [:tbody
         [:tr [:td {:key "Name"} "Name"] [:td {:key  (:name station)}  (:name station)]]
         [:tr [:td {:key "Address"} "Address"] [:td {:key (:address station)} (:address station)]]]]]])))