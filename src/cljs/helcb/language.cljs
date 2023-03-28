(ns helcb.language
  (:require [reagent.core :as r]))

(def options ["Eng" "Fi" "Sv"])

(def current (r/atom "Eng"))

(defn heading-by-language []
  (case @current
    "Eng" {:journeys "Journeys by bike"
           :stations "Bike stations"}
    "Fi" {:journeys "Matkat pyörällä"
          :stations "Pyöräasemat"}
    "Sv" {:journeys "Resor med cykel"
           :stations "Cykelstationer"}))


(defn row-by-language [type] 
  (case type
    :journeys (fn [row]
                (assoc
                 (case @current
                   "Eng" {:departure-station (:departure-name row)
                          :return-station (:return-name row)}
                   "Fi" {:departure-station (:departure-nimi row)
                         :return-station (:return-nimi row)}
                   "Sv" {:departure-station (:departure-namn row)
                         :return-station (:return-namn row)})
                 :duration (:duration row)
                 :distance (:distance row)))
    :stations (fn [row]
                (case @current
                  "Eng" {:name (:name row)
                         :address (str (:osoite row) ", " (:kaupunki row))}
                  "Fi" {:name (:nimi row)
                        :address (str (:osoite row) ", " (:kaupunki row))}
                  "Sv" {:name (:namn row)
                        :address (str (:adress row) ", " (:stad row))}))))

(defn table-display [type]
  (case type
    :stations
    (case @current
      "Eng" [{:key :name :label "Name" :type "text"}
             {:key :address :label "Address" :type "text"}]
      "Fi" [{:key :name :label "Nimi" :type "text"}
            {:key :address :label "Osotie" :type "text"}]
      "Sv" [{:key :name :label "Namn" :type "text"}
            {:key :address :label "Adress" :type "text"}])
    :journeys 
    (case @current
      "Eng" [{:key :departure-station :label "Departure station" :type "text"}
             {:key :return-station :label "Return station" :type "text"}
             {:key :distance :label "Covered distance (m)" :type "integer"}
             {:key :duration :label "Duration (sec.)" :type "integer"}]
      "Fi" [{:key :departure-station :label "Lähtöasema" :type "text"}
            {:key :return-station :label "Paluuasema" :type "text"}
            {:key :distance :label "Katettu etäisyys (m)" :type "integer"}
            {:key :duration :label "Kesto (sek.)" :type "integer"}]
      "Sv" [{:key :departure-station :label "Avgångsstation" :type "text"}
            {:key :return-station :label "Returstation" :type "text"}
            {:key :distance :label "Tillryggalagd sträcka (m)" :type "integer"}
            {:key :duration :label "Varaktighet (sek.)" :type "integer"}])))

(defn update! [value]
  (reset! current value))

(defn selector-input [style default handler options]
  [:select {:style style :on-change handler :defaultValue default}
   (for [o options]
     [:option {:key o :value o} o])])

(defn selector []
  (selector-input
   {:width "auto"}
   "Eng"
   #(update! (-> % .-target .-value))
   options))

