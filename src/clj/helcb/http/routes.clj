(ns helcb.http.routes
  (:require
   [clojure.edn :as edn]
   [ring.util.http-response :as response]
   [helcb.validation :as validate]
   [helcb.http.middleware :as middleware]
   [helcb.db.import :as db.import]
   [helcb.db.update :as db.update]
   [helcb.db.lookup :refer [station-by-stationid 
                            look-up 
                            average-trips-to-and-from-station 
                            stations-for-map 
                            get-top-five-return-and-departure]]))

(defn html-handler [_]
  (response/ok
   (slurp "resources/html/index.html")))

(defn print-and-return-error [e]
  (println (.getMessage e))
  {:error "Something's wrong with the database!"})

(defn check-for-errors-and-reply [params validator response]
  (if-let [errors (validator params)]
    {:error (apply str (vals errors))}
    (try
      (response params)
      (catch Exception e (print-and-return-error e)))))

(defn import-stations [{:keys [params]}]
  (response/ok
   (check-for-errors-and-reply params validate/csv-import #(db.import/batch-stations-from-csv %))))

(defn import-journeys [{:keys [params]}]
  (response/ok 
   (check-for-errors-and-reply params validate/csv-import #(db.import/batch-journeys-from-csv %))))
   
(defn update-station [{:keys [params]}]
  (response/ok
   (check-for-errors-and-reply params validate/updated-station #(db.update/column-in-row! (merge {:name "stations"} %)))))

(defn get-data [{:keys [path-params]}]
  (let [params (edn/read-string (:data path-params))]
    (response/ok (check-for-errors-and-reply params validate/data-request
                                             (fn [m] {:rows (look-up m)})))))

(defn get-station-info [{:keys [path-params]}]
  (let [params (edn/read-string (:data path-params))]
    (println params)
    (response/ok (check-for-errors-and-reply params validate/map-with-id
                                             (fn [m] {:row (station-by-stationid (:id m))
                                                      :traffic (average-trips-to-and-from-station (:id m))
                                                      :top-five (get-top-five-return-and-departure (:id m))})))))

(defn get-traffic [{:keys [path-params]}]
  (let [params (edn/read-string (:data path-params))]
    (response/ok (check-for-errors-and-reply params validate/map-with-id
                                             (fn [m] {:traffic (average-trips-to-and-from-station (:id m))})))))

(defn get-stations-top-five [{:keys [path-params]}]
  (let [params (edn/read-string (:data path-params))]
    (response/ok (check-for-errors-and-reply params validate/map-with-id
                                             (fn [m] {:top-five (get-top-five-return-and-departure (:id m))})))))

(defn get-stations-for-map [_]
  (response/ok {:stations (stations-for-map)}))

(def routes
  [""
   {:middleware [middleware/wrap-formats]}
   ["/" {:get html-handler}]
   ["/import-stations" {:post import-stations}]
   ["/import-journeys" {:post import-journeys}]
   ["/update-station" {:post update-station}]
   ["/stations-for-map/" {:get get-stations-for-map}]
   ["/station-top-five/:data" {:get get-stations-top-five}]
   ["/station-traffic/:data" {:get get-traffic}]
   ["/station-info/:data" {:get get-station-info}]
   ["/data/:data" {:get get-data}]])
