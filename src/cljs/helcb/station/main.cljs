(ns helcb.station.main
  (:require [helcb.station.state :as station.state]
            [helcb.state :as state]
            [helcb.http :as http]
            [helcb.columns :as columns]
            [helcb.commons :as commons]))


(defn buttons []
  [:div.columns.m-3>div.column.is-offset-7.is-1.has-text-right
   [commons/button "Back to list" #(do (station.state/reset-to-initial!)
                                       (state/update-state! @station.state/parent))]
   ])

(defn station-view []
  (println @station.state/settings)
  (when (= @state/display :single-station) 
    [:div 
     [buttons] 
       [:div.columns.is-centered 
       [:table.table
        (into 
         [:tbody]
          (for [{key :key label :label} (:stations columns/columns)
                :let [value (get @station.state/row key)
                      id (get @station.state/row :id)
                      post #(do 
                              (http/post-update-station! {:id id :column (name key) :value value})
                              ())]]
            (into [:tr [:td label]]
                  (if (= @station.state/edit key)
                    [[:td {:key id} (commons/text-input label value #(station.state/update-row! key %) post {:width "auto"})]
                     [:td {:key key}
                      [:a {:on-click post} "Save"] " " [:a {:on-click #(station.state/set-edit! nil)} "Cancel"]]]
                    [[:td {:key id} value]
                     [:td {:key key} [:a {:on-click #(station.state/set-edit! key)} "Edit"]]]))))]]]))