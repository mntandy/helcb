(ns helcb.db.update
  (:require [helcb.db.core :as db]
            [helcb.db.utils :as utils]
            [helcb.db.lookup :as db.lookup]))

(defn column-in-row! [m]
  {:result (db/update-column-in-row! (update m :value utils/double-up-single-quotes))})


(defn update-id [m]
  (db/update-column-in-row! (update m :value utils/double-up-single-quotes)))

;{:id id :column (name key) :value value}

(defn find-with-zeros []
  (db.lookup/get-stations-with-filter {:name "stations", :offset "0", :filters {:stationid {:text "0", :option "begins with"}}}))

(defn fix-leading-zeros []
  (for [m (find-with-zeros)]
    (update-id {:name "stations" :id (:id m) :column "stationid" :value (utils/trim-leading-zeros (:stationid m))})))