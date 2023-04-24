(ns helcb.import.main
  (:require 
   [helcb.state :as state]
   [helcb.import.state :as import.state]
   [helcb.commons :as commons]))

(defn uri-and-separator [columns route]
  [:div.columns
   [:div.column.is-offset-4.is-3
    [:div.field
     [:label.label "Address to CSV-file:"]
     [commons/text-input 
      "Uri" 
      @import.state/uri 
      (import.state/update! :uri) 
      #(import.state/submit-csv! columns route) 
      nil 
      @import.state/importing]]]
   [:div.column.is-1
    [:div.control [:label.label {:for :options} "Separator:"]
     [:div.select.is-normal
      [:select {:value @import.state/sep
                :name :options
                :disabled @import.state/importing
                :on-change #((import.state/update! :sep) (-> % .-target .-value))}
       [:option {:value \,} "Comma (,)"]
       [:option {:value \;} "Semi-colon (;)"]]]]]])

(defn import-button [on-click]
  [:div.columns.m-3>div.column.is-offset-7.is-1.has-text-right
   [:input.button
    {:type :submit
     :value "Import!"
     :disabled @import.state/importing
     :on-click (when-not @import.state/importing on-click)}]])

(defn heading [text]
  [:div.columns.is-centered>section>div.m-5>p.title text])

(def options 
  {:import-journeys
   {:columns :journeys
    :text "Import journeys"
    :route "/import-journeys"}
   :import-stations 
   {:columns :stations
    :text "Import stations"
    :route "/import-stations"}})

(defn multi-importer []
  (when-let [{text :text
              route :route
              columns :columns} (get options @state/display nil)]
    (println @import.state/import-data)
    [:div
     [heading text]
     [uri-and-separator columns route]
     [import-button #(import.state/submit-csv! columns route)]]))

(defn importer []
  [multi-importer])