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
                   "Eng" {:departure-station (:departure_name row)
                          :return-station (:return_name row)}
                   "Fi" {:departure-station (:departure_nimi row)
                         :return-station (:return_nimi row)}
                   "Sv" {:departure-station (:departure_namn row)
                         :return-station (:return_namn row)})
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
             {:key :osoite :label "Address" :type "text"}
             {:key :kaupunki :label "Municipality" :type "text"}]
      "Fi" [{:key :nimi :label "Nimi" :type "text"}
            {:key :osoite :label "Osoite" :type "text"}
            {:key :kaupunki :label "Kaupunki" :type "text"}]
      "Sv" [{:key :namn :label "Namn" :type "text"}
            {:key :adress :label "Adress" :type "text"}
            {:key :stad :label "Stad" :type "text"}])
    :journeys 
    (case @current
      "Eng" [{:key :departure :label "Departure" :type "timestamp"}
             {:key :departure.name :label "Departure station" :type "text"}
             {:key :return :label "Return" :type "timestamp"}
             {:key :return.name :label "Return station" :type "text"}
             {:key :distance :label "Covered distance (m)" :type "integer"}
             {:key :duration :label "Duration (sec.)" :type "integer"}]
      "Fi" [{:key :departure :label "Lähtö" :type "timestamp"}
            {:key :departure.nimi :label "Lähtöasema" :type "text"}
            {:key :return :label "Palata" :type "timestamp"}
            {:key :return.nimi :label "Paluuasema" :type "text"}
            {:key :distance :label "Katettu etäisyys (m)" :type "integer"}
            {:key :duration :label "Kesto (sek.)" :type "integer"}]
      "Sv" [{:key :departure :label "Avresa" :type "timestamp"}
            {:key :departure.namn :label "Avgångsstation" :type "text"}
            {:key :return :label "Retur" :type "timestamp"}
            {:key :return.namn :label "Returstation" :type "text"}
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

