(ns helcb.add
  (:require 
   [helcb.state :as state]
   [helcb.import-data :as import-data]
   [helcb.columns :as columns]))

(defn text-input [name update! value disabled]
  [:label.label {:for name} name]
  [:input.input
   {:type :text
    :name name
    :on-change #(update! (-> % .-target .-value))
    :value value
    :disabled disabled}])

(defn uri-and-separator []
  [:div.columns
   [:div.column.is-offset-4.is-3
    [:div.field
     [:label.label "Address to CSV-file:"]
     [text-input "Uri" (import-data/update! :uri) @import-data/uri false]]]
   [:div.column.is-1
    [:div.control [:label.label {:for :options} "Separator:"]
     [:div.select.is-normal
      [:select {:value @import-data/sep
                :name :options
                :on-change #((import-data/update! :sep) (-> % .-target .-value))}
       [:option {:value \,} "Comma (,)"]
       [:option {:value \;} "Semi-colon (;)"]]]]]])



(defn single-importer []
  (let [type (case @state/display
               :add-single-journey :journeys
               :add-single-station :stations)
        keys (columns/db-labels type)] 
    [:div
     [:div.block
      [:div.columns.is-centered
       [:i "Here are the column labels and the first row. 
              Select data type and appropriate restrictions on rows before importing..."]]]
     [:div.block
      [:div.columns.is-centered.m-3
       [:table.table.is-bordered
        [:thead>tr [:th [:i "Headers"]]
         (for [column (get columns/labels type)]
           [:th {:key column} column])]
        [:tbody
         (into [:tr [:td [:i "Data"]]]
               (for [key keys]
                 [:td {:key key}
                  [text-input key (import-data/update-column! (keyword key)) (get @import-data/columns (keyword key)) false]]))]]]]]))
   
(defn multi-importer []
  [uri-and-separator])

(defn importer []
  (println @import-data/import-data)
  (when (state/is-importing)
    [:div
     (case @state/display
       (:add-multiple-journeys :add-multiple-stations) [multi-importer]
       (:add-single-journey :add-single-station) [single-importer]
       nil)
     [:div.columns.m-3>div.column.is-offset-7.is-1.has-text-right
      [:input.button
       {:type :submit
        :value "Import!"
        :on-click nil}]]]))

