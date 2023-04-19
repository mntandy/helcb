(ns helcb.http.routes
  (:require
   [clojure.edn :as edn]
   [ring.util.http-response :as response]
   [helcb.validation :as validate]
   [helcb.http.middleware :as middleware]
   [helcb.db.import :as db.import]
   [helcb.db.update :as db.update]
   [helcb.db.lookup :refer [look-up average-trips-to-and-from-station]]))

(defn html-handler [_]
  (response/ok
   (slurp "resources/html/index.html")))

(defn print-and-return-error [e]
  (println "error!!" (.getMessage e))
  {:error "Something's wrong with the database!"})

(defn check-for-errors-and-reply [params validator response]
  (if-let [errors (validator params)]
    {:error (apply str (vals errors))}
    (try
      (response params)
      (catch Exception e (print-and-return-error e)))))

(defn import-stations [{:keys [params]}]
  (response/ok
   (check-for-errors-and-reply params validate/csv-import #(db.import/stations-from-csv %))))

(defn import-journeys [{:keys [params]}]
  (response/ok 
   (check-for-errors-and-reply params validate/csv-import #(db.import/journeys-from-csv %))))
   
(defn update-station [{:keys [params]}]
  (response/ok
   (check-for-errors-and-reply params validate/updated-station #(db.update/column-in-row! (merge {:name "stations"} %)))))

(defn get-data [{:keys [path-params]}]
  (let [params (edn/read-string (:data path-params))]
    (response/ok (check-for-errors-and-reply params validate/data-request
                                             (fn [m] {:rows (look-up m)})))))

(defn get-traffic [{:keys [path-params]}]
  (let [params (edn/read-string (:data path-params))]
    (response/ok (check-for-errors-and-reply params validate/map-with-id
                                             (fn [m] {:traffic (average-trips-to-and-from-station (:id m))})))))

(def routes
  [""
   {:middleware [middleware/wrap-formats]}
   ["/" {:get html-handler}]
   ["/import-stations" {:post import-stations}]
   ["/import-journeys" {:post import-journeys}]
   ["/update-station" {:post update-station}]
   ["/station-traffic/:data" {:get get-traffic}]
   ["/data/:data" {:get get-data}]
   ])
