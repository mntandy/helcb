(ns helcb.import.state
  (:require
   [reagent.core :as r]))

(def initial-import {:success false
                     :columns {}
                     :uri "journeys.csv"
                     :sep \,});:data {:labels []} {:labels :label {:content "" :type "" :restrictions {:nlt "" :ngt ""}}}  )

(def import-data (r/atom initial-import))

(defn reset-to-initial! []
  (reset! import-data initial-import))

(def success (r/cursor import-data [:success]))
(def uri (r/cursor import-data [:uri]))
(def sep (r/cursor import-data [:sep]))
(def columns (r/cursor import-data [:columns]))

(defn success! []
  (swap! import-data assoc :success true))

(defn csv []
  (select-keys @import-data [:uri :sep]))

(defn update-column! [key]
  (fn [params]
    (swap! import-data assoc-in [:columns key] params)))

(defn update! [key]
  (fn [params]
    (swap! import-data assoc key params)))
