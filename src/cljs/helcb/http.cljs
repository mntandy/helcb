(ns helcb.http
  (:require
   [ajax.core :refer [GET POST]]
   [helcb.validation :as validate]
   [helcb.flattenmap :refer [all-vals]]))

(defn check-for-errors [data validator error!]
  (if (contains? data :error)
    (error! (str "from server: " (:error data)))
    (when-let [errors (validator data)]
      (error! (all-vals errors)))))

(defn post! [route data success error!]
  (if-let [errors ((validate/post route) data)]
    (error! (all-vals errors))
    (POST route
      {:format :json
       :headers
       {"accept" "application/transit+json"}
       :params data
       :handler #(when-not (check-for-errors % (validate/response route) error!) (success (:result %)))
       :error-handler #(error! (str "From server: " %))})))

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

(defn get! [route-key data update error!]
  (let [{route :route
         validator :validator} (route-key routes)]
    (GET (str route data)
      {:headers {"accept" "application/transit+json"}
       :handler #(when-not (check-for-errors % validator error!)
                   (update %))})))