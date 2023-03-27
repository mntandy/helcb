(ns helcb.csv.core
  (:require
   [clojure.java.io :as io]
   [clojure.data.csv :as csv]))

(defn remove-BOM [seq]
  (if (= (int (first seq)) 65279)
    (rest seq)
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

(defn print-and-return-file-error [e]
  (println (.getMessage e))
  {:error "Something's wrong with the file. Does it exist? Is it a CSV file? Go find out!"})

(defn csv-data->maps [csv-data]
  (map zipmap
       (repeat (get-labels (first csv-data)))
       (rest csv-data)))

(defn filter-labels [labels m]
  (if (coll? labels)
    (map #(select-keys % labels) m)
    m))

(defn import-from-uri [uri sep side-effect! labels]
  (try
    (with-open [reader (io/reader uri)]
      {:result {:count (count (map side-effect! (filter not-empty (filter-labels labels (csv-data->maps (read-csv reader sep))))))}})
    (catch Exception e (print-and-return-file-error e))))
