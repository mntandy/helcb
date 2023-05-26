(ns helcb.importer
  (:require
   [clojure.string :as str]
   [helcb.state :as state] 
   [helcb.commons :as commons]
   [reagent.core :as r]
   [helcb.http :as http]
   [helcb.columns :as columns]))

(def initial-settings {:importing false
                       :uri ""
                       :sep \,})

(def settings (r/atom initial-settings))

(defn reset-to-initial! []
  (reset! settings initial-settings))

(defn csv-import-success! [columns-key result]
  (reset-to-initial!)
  (state/set-message! (if (= 0 result)
                        [:div [:p "The file did not contain any importable rows. It must contain the following columns: "
                               (str/join ", " (columns/for-db columns-key :label))]]
                        [:div [:p "Imported " result " rows."]
                         [:p "Did you expect more lines to be imported? Duplicates and errorous entries are ignored."]])))

(defn submit-csv! [columns route]
  (swap! settings assoc :importing true)
  (state/set-message! "The link has been sent to the server. Importing could take a while, depending on the number of entries. You will get feedback eventually as long as you remain on the site.")
  (http/post! route (select-keys @settings [:uri :sep]) (fn [response] (csv-import-success! columns response))
              #(state/set-error-message! %)))

(defn uri-and-separator [columns route]
  [:div.columns
   [:div.column.is-offset-4.is-3
    [:div.field
     [:label.label "Address to CSV-file:"]
     [commons/text-input 
      "Uri" 
      (:uri @settings) 
      #(swap! settings assoc :uri %) 
      #(submit-csv! columns route) 
      nil 
      (:importing @settings)]]]
   [:div.column.is-1
    [:div.control [:label.label {:for :options} "Separator:"]
     [:div.select.is-normal
      [:select {:value (:sep @settings)
                :name :options
                :disabled (:importing @settings)
                :on-change #(swap! settings assoc :sep (-> % .-target .-value))}
       [:option {:value \,} "Comma (,)"]
       [:option {:value \;} "Semi-colon (;)"]]]]]])

(defn import-button [on-click]
  [:div.columns.m-3>div.column.is-offset-7.is-1.has-text-right
   [:button.button
    {:disabled (:importing @settings)
     :on-click (when-not (:importing @settings) on-click)} 
    "Import!"]])

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

(defn main []
  (when-let [{text :text
              route :route
              columns :columns} (get options @state/display nil)]
    [:div
     [heading text]
     [uri-and-separator columns route]
     [import-button #(submit-csv! columns route)]]))