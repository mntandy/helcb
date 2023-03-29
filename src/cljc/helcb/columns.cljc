(ns helcb.columns)

(def type-table
  {:journeys "journeys"
   :stations "stations"})

(defn table->type [s]
  (get {"journeys" :journeys
        "stations" :stations}
       s))


(def import-columns 
  {:journeys
   [{:key :departure_station :label "Departure station name" :type "text"}
    {:key :return_station :label "Return station name" :type "text"}]})

(def db-columns
  {:journeys
   [{:key :departure :label "Departure" :type "timestamp"}
    {:key :return :label "Return" :type "timestamp"}
    {:key :departure_station_id :label "Departure station id" :type "text"}
    {:key :return_station_id :label "Return station id" :type "text"}
    {:key :distance :label "Covered distance (m)" :type "integer"}
    {:key :duration :label "Duration (sec.)" :type "integer"}]})

(defn labels-for-journeys-csv-import []
  (map #(:label %) (concat (:journeys db-columns) (:journeys import-columns))))

(defn keys-for-journeys-csv-import []
  (map #(:key %) (concat (:journeys db-columns) (:journeys import-columns))))

(defn journeys-csv-import-label->key []
  (zipmap (labels-for-journeys-csv-import) (keys-for-journeys-csv-import)))

(defn journeys-db-csv-labels []
  (map #(:label %) (get db-columns :journeys)))

(defn journeys-db-keys []
  (map #(:key %) (get db-columns :journeys)))

(defn journeys-db-types []
  (map #(:type %) (get db-columns :journeys)))

(defn journeys-db-keys-as-names []
  (map name (journeys-db-keys)))

(defn journeys-label->key []
  (zipmap (journeys-db-csv-labels) (journeys-db-keys)))


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
   [{:key :departure :label "Departure" :type "timestamp"}
    {:key :return :label "Return" :type "timestamp"}
    {:key :departure_station_id :label "Departure station id" :type "text"}
    {:key :return_station_id :label "Return station id" :type "text"}
    {:key :departure.name :label "Departure station name" :type "text"}
    {:key :departure.namn :label "Departure station name" :type "text"}
    {:key :departure.nimi :label "Departure station name" :type "text"}
    {:key :return.name :label "Return station name" :type "text"}
    {:key :return.namn :label "Return station name" :type "text"}
    {:key :return.nimi :label "Return station name" :type "text"}
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


(def keys-for-filters
  {:journeys
   [:return :departure :return.name :return.namn :return.nimi
    :departure.name :departure.nimi :departure.namn
    :duration :distance]
   :stations
   [:stationid :nimi :namn :osoite :adress :kaupunki :stad]})

(def journeys-underline->dot
  {:departure_name :departure.name
   :departure_namn :departure.namn
   :departure_nimi :departure.nimi
   :return_name :return.name
   :return_namn :return.namn
   :return_nimi :return.nimi})

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



