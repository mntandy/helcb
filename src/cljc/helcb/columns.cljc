(ns helcb.columns)

(def required-columns 
  {:journeys 
   ["Departure station id"
    "Return station id"
    "Covered distance (m)"
    "Duration (sec.)"]
   :stations
   ["ID"
    "Nimi"
    "Namn"
    "Name"
    "Osoite"
    "Adress"
    "Kaupunki"
    "Stad"
    "x"
    "y"]})

(def columns
  {:journeys
   [{:key :departure-station :label "Departure station name" :type "text"}
    {:key :return-station :label "Return station name" :type "text"}
    {:key :distance :label "Covered distance (m)" :type "integer"}
    {:key :duration :label "Duration (sec.)" :type "integer"}]
   :stations
   [{:key :station-id :label "ID" :type "text"} 
    {:key :nimi :label "Nimi" :type "text"}
    {:key :namn :label "Namn" :type "text"}
    {:key :name :label "Name" :type "text"}
    {:key :osoite :label "Osoite" :type "text"}
    {:key :adress :label "Adress" :type "text"}
    {:key :kaupunki :label "Kaupunki" :type "text"}
    {:key :stad :label "Stad" :type "text"}
    {:key :x :label "x" :type "decimal"}
    {:key :y :label "y" :type "decimal"}]})


(defn column-labels [type]
  (mapv #(:label %) (get columns type)))

(defn column-data-types [type]
  (mapv #(:type %) (get columns type)))


