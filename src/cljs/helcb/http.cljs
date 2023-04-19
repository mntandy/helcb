(ns helcb.http
  (:require
   [ajax.core :refer [GET POST]]
   [helcb.state :as state]
   [helcb.validation :as validate]
   [helcb.utils :refer [all-vals]]
   [helcb.explore.state :as explore.state] 
   [helcb.station.state :as station.state]
   [helcb.filters :as filters]))

(defn check-for-errors [data validator]
  (if (contains? data :error)
    (state/set-error-message! (str "from server: " (:error data)))
    (when-let [errors (validator data)]
      (state/set-error-message! (all-vals errors)))))

(defn post! [route data success]
  (if-let [errors ((validate/post route) data)]
    (state/set-error-message! (all-vals errors))
    (POST route
      {:format :json
       :headers
       {"accept" "application/transit+json"}
       :params data
       :handler #(when-not (check-for-errors % (validate/response route)) (success (:result %)))
       :error-handler #(state/set-error-message! (str "From server: " %))})))

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

(defn get-station-traffic [id]
  (GET (str "/station-traffic/" {:id id})
    {:headers {"accept" "application/transit+json"}
     :handler #(when-not (check-for-errors % validate/station-traffic)
                 (station.state/update-traffic! (:traffic %)))}))