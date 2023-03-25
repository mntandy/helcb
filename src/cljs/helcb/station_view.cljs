(ns helcb.station-view
  (:require [helcb.station-data :as station-data]
            [helcb.state :as state]
            [helcb.language :as language]))
  
(defn station-view []
  (when (= @state/display :single-station)
    (let [station (language/station-by-language @station-data/info)]
      [:div 
       [:div.columns.is-centered 
       [:table.table
        [:tbody
         [:tr [:td {:key "Name"} "Name"] [:td {:key  (:name station)}  (:name station)]]
         [:tr [:td {:key "Address"} "Address"] [:td {:key (:address station)} (:address station)]]]]]])))