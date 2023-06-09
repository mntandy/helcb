(ns helcb.csv.core
  (:require
   [clojure.java.io :as io]
   [clojure.data.csv :as csv]))

(defn remove-BOM [seq]
  (if (= (int (first seq)) 65279)
    (subs seq 1)
    seq))

(defn get-labels [first-line]
  (into [(remove-BOM (first first-line))] (rest first-line)))

(defn separator [sep]
  (case sep
    "," \,
    ";" \;
    \,))

(defn read-csv [reader sep]
  (csv/read-csv reader :separator (separator sep)))

(defn csv-data->maps [csv-data]
  (map zipmap
       (repeat (get-labels (first csv-data)))
       (rest csv-data)))

(defn filter-labels [labels coll]
  (if (coll? labels)
    (map #(select-keys % labels) coll)
    coll))

(defn import-from-uri [uri sep consume! labels]
  (with-open [reader (io/reader uri)] 
      (consume! (filter not-empty (filter-labels labels (csv-data->maps (read-csv reader sep)))))))
