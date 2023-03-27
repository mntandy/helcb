(ns helcb.http
  (:require
   [ajax.core :refer [GET POST]]
   [helcb.state :as state]
   [helcb.validation :as validate]
   [helcb.utils :refer [all-vals]]))

(defn post-object [params handler]
  (println params)
  {:format :json
   :headers
   {"accept" "application/transit+json"}
   :params params
   :handler handler
   :error-handler #(state/set-error-message! (:error %))})

(defn check-for-errors [data validator]
  (println data)
  (if (contains? data :error)
    (state/set-error-message! (:error data))
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
    (state/set-error-message! (all-vals errors))
    (POST (if (= type :journeys ) "/import-journeys" "/import-stations")
      (post-object
       data
       #(when-not (check-for-errors % validate/csv-import-success)
          (state/csv-import-success! (:count (:result %))))))))