(ns helcb.http.routes
  (:require
   [ring.util.http-response :as response]
   [helcb.validation :as validate]
   [helcb.http.middleware :as middleware]
   [helcb.db.import :as db.import]))

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

(def routes
  [""
   {:middleware [middleware/wrap-formats]}
   ["/" {:get html-handler}]
   ["/import-stations" {:post import-stations}]
   ])
