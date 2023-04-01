(ns helcb.db.update
  (:require [helcb.db.core :as db]
            [helcb.db.utils :as utils]))

(defn column-in-row! [m]
  {:result (db/update-column-in-row! (update m :value utils/double-up-single-quotes))})