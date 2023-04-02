(ns helcb.columns)


(defn table-name>key [s]
  (get {"journeys" :journeys
        "stations" :stations}
       s))

(def db-columns
  {:journeys
   [{:key :departure :label "Departure" :type "timestamp"}
    {:key :return :label "Return" :type "timestamp"}
    {:key :departure_station_id :label "Departure station id" :type "text"}
    {:key :return_station_id :label "Return station id" :type "text"}
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

(def import-columns
  {:journeys
   (concat (:journeys db-columns)
           [{:key :departure_station :label "Departure station name" :type "text"}
            {:key :return_station :label "Return station name" :type "text"}])
   :stations (:stations db-columns)})

(def lookup-columns
  {:journeys
   (concat 
    (:journeys db-columns) 
    [{:key :departure.name :label "Departure station name" :type "text"}
     {:key :departure.namn :label "Departure station name" :type "text"}
     {:key :departure.nimi :label "Departure station name" :type "text"}
     {:key :return.name :label "Return station name" :type "text"}
     {:key :return.namn :label "Return station name" :type "text"}
     {:key :return.nimi :label "Return station name" :type "text"}])
   :stations (:stations db-columns)})

(defn for-import
  ([type key] (map key (type import-columns)))
  ([type func key] (map (comp func key) (type import-columns))))

(defn for-db
  ([type key] (map key (type db-columns)))
  ([type func key] (map (comp func key) (type db-columns))))

(defn for-lookup
  ([type key] (map key (type lookup-columns)))
  ([type func key] (map (comp func key) (type lookup-columns))))

(def journeys-transformation-from-db
  {:departure_name :departure.name
   :departure_namn :departure.namn
   :departure_nimi :departure.nimi
   :return_name :return.name
   :return_namn :return.namn
   :return_nimi :return.nimi})

(defn data-type-from-key-for-lookup [table key]
  (key (zipmap (for-lookup table :key) (for-lookup table :type))))


