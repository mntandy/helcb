(ns helcb.columns)

(def type-table
  {:journeys "journeys"
   :stations "stations"})

(defn table->type [s]
  (get {"journeys" :journeys
        "stations" :stations}
       s))

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
   [{:key :stationid :label "ID" :type "text"} 
    {:key :nimi :label "Nimi" :type "text"}
    {:key :namn :label "Namn" :type "text"}
    {:key :name :label "Name" :type "text"}
    {:key :osoite :label "Osoite" :type "text"}
    {:key :adress :label "Adress" :type "text"}
    {:key :kaupunki :label "Kaupunki" :type "text"}
    {:key :stad :label "Stad" :type "text"}
    {:key :x :label "x" :type "text"}
    {:key :y :label "y" :type "text"}]})


(defn csv-labels [type]
  (map #(:label %) (get columns type)))

(defn db-keys [type]
  (map #(:key %) (get columns type)))

(defn keys-as-names [type]
  (map name (db-keys type)))

(defn data-types [type]
  (mapv #(:type %) (get columns type)))


(defn key->data-type [type]
  (zipmap (db-keys type) (data-types type)))

(defn data-type-for-key [type key]
  (get (key->data-type type) key))


(defn label->key [type]
  (zipmap (csv-labels type) (db-keys type)))



