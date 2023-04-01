(ns helcb.station.main
  (:require [helcb.station.state :as station.state]
            [helcb.state :as state]
            [helcb.http :as http]
            [helcb.columns :as columns]
            [helcb.commons :as commons]))

(defn buttons []
  [:div.columns.m-3>div.column.is-offset-7.is-1.has-text-right
   [commons/button "Back to list" (fn [] 
                                    (state/update-state! @station.state/parent)
                                    (station.state/reset-to-initial!))]])

(defn update-station-data! [data]
  (http/post! "/update-station" data
            (fn [_]
              (station.state/set-edit! nil)
              (state/set-message! "Update successful!"))))

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
                      post #(update-station-data! {:id id :column (name key) :value value})]]
            (into [:tr [:td label]]
                  (if (= @station.state/edit key)
                    [[:td {:key id} (commons/text-input label value #(station.state/update-row! key %) post {:width "auto"})]
                     [:td {:key key}
                      [:a {:on-click post} "Save"] " " [:a {:on-click #(station.state/set-edit! nil)} "Cancel"]]]
                    [[:td {:key id} value]
                     [:td {:key key} [:a {:on-click #(station.state/set-edit! key)} "Edit"]]]))))]]]))