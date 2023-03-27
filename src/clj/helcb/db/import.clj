(ns helcb.db.import
  (:require
   [clojure.set :refer [rename-keys]]
   [helcb.csv.core :as csv]
   [helcb.db.core :as db]
   [helcb.columns :as columns]))


(defn prepare-row-for-import [type s m]
  (let [result (merge (zipmap (columns/db-keys type) (repeat ""))
                      (rename-keys m (columns/label->key type)))]
    {:name s :column-names (map name (keys result)) :column-values (vals result)}))


(defn stations-from-csv [params]
  (csv/import-from-uri (:uri params) (:sep params)
                         #(db/insert-row! (prepare-row-for-import :stations "stations" %))
                         (columns/csv-labels :stations)))

