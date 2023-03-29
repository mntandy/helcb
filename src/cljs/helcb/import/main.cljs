(ns helcb.import.main
  (:require 
   [helcb.state :as state]
   [helcb.import.state :as import.state]
   [helcb.language :as language]
   [helcb.http :as http]))

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
     [text-input "Uri" (import.state/update! :uri) @import.state/uri @import.state/success]]]
   [:div.column.is-1
    [:div.control [:label.label {:for :options} "Separator:"]
     [:div.select.is-normal
      [:select {:value @import.state/sep
                :name :options
                :disabled @import.state/success
                :on-change #((import.state/update! :sep) (-> % .-target .-value))}
       [:option {:value \,} "Comma (,)"]
       [:option {:value \;} "Semi-colon (;)"]]]]]])


(defn import-button [on-click]
  [:div.columns.m-3>div.column.is-offset-7.is-1.has-text-right
   [:input.button
    {:type :submit
     :value "Import!"
     :disabled @import.state/success
     :on-click (when-not @import.state/success on-click)}]])

(defn single-importer [type]
  (let [columns (language/table-display type)] 
    [:div
     [:div.block
      [:div.columns.is-centered.m-3
       [:table.table.is-bordered
        [:thead>tr [:th [:i "Headers"]]
         (for [{key :key label :label} columns]
           [:th {:key key} label])]
        [:tbody
         (into [:tr [:td [:i "Data"]]]
               (for [{key :key} columns]
                 [:td {:key key}
                  [text-input key (import.state/update-column! key) (get @import.state/columns (keyword key)) false]]))]]]]
     [import-button #(http/post-import-columns! type @import.state/columns)]]))
   
(defn multi-importer [type]
  [:div
  [uri-and-separator]
  [import-button #(http/post-import-csv! type (import.state/csv))]])

(defn importer []
  (case @state/display
     :add-single-journey [single-importer :journeys]
     :add-multiple-journeys [multi-importer :journeys]
     :add-single-station [single-importer :stations]
     :add-multiple-stations [multi-importer :stations]
    nil))

