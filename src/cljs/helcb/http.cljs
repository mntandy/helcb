(ns helcb.http
  (:require
   [ajax.core :refer [GET POST]]
   [helcb.state :as state]
   [helcb.validation :as validate]
   [helcb.utils :refer [all-vals]]
   [helcb.explore.state :as explore.state]
   [helcb.import.state :as import.state]
   [helcb.filters :as filters]))

(defn post-object [params handler]
  (println params)
  {:format :json
   :headers
   {"accept" "application/transit+json"}
   :params params
   :handler handler
   :error-handler #(state/set-error-message! (str "from server: " (:error %)))})

(defn check-for-errors [data validator]
  (if (contains? data :error)
    (state/set-error-message! (str "from server: " (:error data)))
    (when-let [errors (validator data)]
      (state/set-error-message! (all-vals errors)))))

(defn post-import-columns! [type data]
  (if-let [errors (validate/csv-import data)]
    (state/set-error-message! (all-vals errors))
    (POST (if (= type :journeys) "/journeys" "/stations")
      (post-object
       data
       #(when-not (check-for-errors % validate/csv-import-success)
          (state/csv-import-success! (:count (:result %))))))))

(defn post-import-csv! [type data]
  (if-let [errors (validate/csv-import data)]
    (state/set-error-message! (str "OK.." (all-vals errors)))
    (POST (if (= type :journeys ) "/import-journeys" "/import-stations")
      (post-object
       data
       #(when-not (check-for-errors % validate/csv-import-success)
          (import.state/success!)
          (state/csv-import-success! (:count (:result %))))))))

(defn get-data! [reset add-offset-to-limit]
  (GET (str "/data/" (explore.state/prepare-for-request reset add-offset-to-limit))
    {:headers {"accept" "application/transit+json"}
     :handler #(when-not (check-for-errors % validate/rows)
                 (explore.state/update-rows! (:rows %) reset))}))

(defn get-filtered-data []
  (if-let [element (filters/first-without-option (explore.state/filters))]
    (state/set-error-message! (str "\"Filter\" is not a kind of filter for " (explore.state/filter-text-for-column element) "."))
    (get-data! true true)))

(defn download-initial-data [selected]
  (when (state/is-exploring selected)
    (explore.state/set-name selected)
    (get-data! true false)))