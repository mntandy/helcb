(ns helcb.http
  (:require
   [ajax.core :refer [GET POST]]
   [helcb.state :as state]
   [helcb.validation :as validate]
   [helcb.utils :refer [all-vals]]))

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

(def routes
  {:data
   {:route "/data/"
    :validator validate/rows}
   :station-traffic
   {:route "/station-traffic/"
    :validator validate/station-traffic}
   :station-info
   {:route "/station-info/"
    :validator validate/station-info}
   :stations-for-map
   {:route "/stations-for-map/"
    :validator validate/stations-for-map}})

(defn get [route-key data update]
  (let [{route :route
         validator :validator} (route-key routes)]
    (GET (str route data)
      {:headers {"accept" "application/transit+json"}
       :handler #(when-not (check-for-errors % validator)
                   (update %))})))