(ns helcb.http.routes
  (:require
   [clojure.edn :as edn]
   [ring.util.http-response :as response]
   [helcb.validation :as validate]
   [helcb.http.middleware :as middleware]
   [helcb.db.import :as db.import]
   [helcb.db.lookup :refer [look-up]]))

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
   (check-for-errors-and-reply params validate/csv-import #(db.import/stations-from-csv %))))

(defn get-data [{:keys [path-params]}]
  (let [params (edn/read-string (:data path-params))]
    (response/ok (check-for-errors-and-reply params validate/data-request
                                             (fn [m] {:rows (look-up m)})))))
(def routes
  [""
   {:middleware [middleware/wrap-formats]}
   ["/" {:get html-handler}]
   ["/import-stations" {:post import-stations}]
   ["/data/:data" {:get get-data}]
   ])
