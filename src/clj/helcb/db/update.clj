(ns helcb.db.update
  (:require [helcb.db.core :as db]
            [helcb.clj-utils :as utils]
            [helcb.db.lookup :as db.lookup]))

(defn column-in-row! [m]
  {:result (db/update-column-in-row! (update m :value utils/double-up-single-quotes))})

(defn fix-leading-zeros []
  (for [m (db.lookup/find-with-zeros)]
    (db/update-column-in-row! 
     {:name "stations"
      :id (:id m)
      :column "stationid"
      :value (utils/trim-leading-zeros (:stationid m))})))