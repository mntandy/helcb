(ns helcb.table
  (:require 
   [helcb.state :as state]
   [helcb.explore-data :as explore-data]
   [helcb.filters :as filters]
   [helcb.language :as language]
   [helcb.columns :as columns]))

(defn input [type name on-change on-enter style]
  [:label.label {:for name} name]
  [:input {:type type
           :name name
           :style style
           :on-key-up (fn [event] (when (= (. event -key) "Enter") (on-enter)))
           :on-change #(on-change (-> % .-target .-value))
           }])

(defn text-input [name on-change on-enter style]
  [input :text name on-change on-enter style])

(defn filter-input [column style]
  (text-input
   column
   (explore-data/update-filter-for-column! column)
   :get-filtered-data
   style))

(defn selector-input [style default handler options]
  [:select {:style style :on-change handler}
   [:option {:value default} default]
   (for [o options]
     [:option {:key o :value o} o])])

(defn align-by-type [type]
  (get {"decimal" "right"
        "integer" "right"
        "text" "equals"}
       type))


(defn select-station [text]
  [:a {:on-click #(println text)} text])

(defn link-to-station [path text]
  ((get-in
    {:journeys
     {:departure-station select-station
      :return-station select-station}
     :stations
     {:name select-station}}
    path identity)
   text))
  

(defn table []
  (let [type (case @state/display
               :explore-journeys :journeys
               :explore-stations :stations)
        columns (language/table-display type)]
    [:div 
     [:div.columns.is-centered>section>div.m-5>p.title (get (language/heading-by-language) type)]
     [:table.table
      [:thead
       (into [:tr] (for [{key :key label :label} columns]
                     [:th {:key key :style {:text-align "center"}}
                      [:a {:on-click #(explore-data/update-sorting! key)}
                       (explore-data/column-label-with-direction key label)]]))
       (into [:tr] (for [{key :key label :label data-type :type} columns]
                     [:th {:key (str key "filter") :style {:text-align (align-by-type data-type)}}
                      (selector-input
                       {:width "auto"}
                       "Filter"
                       #(explore-data/update-filter-selector! key (-> % .-target .-value))
                       (filters/options-for-type data-type))
                      (filter-input label {:width "30%"})]))]
      (into [:tbody]
            (for [row (map (language/row-by-language type) @explore-data/rows)]
              (into [:tr]
                    (for [{key :key data-type :type} columns
                          :let [text (get row key)]]
                      [:td {:key (str row key) :style {:text-align (align-by-type data-type)}} 
                       (link-to-station [type key] text)]))))]]))

(defn get-more-rows []
  [:input.button
   {:type :submit
    :on-click #(do
                 (explore-data/add-limit-to-offset))
    :value "Get more rows"
    :disabled (:got-all @explore-data/settings)}])
