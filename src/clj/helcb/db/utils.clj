(ns helcb.db.utils
  (:require [clojure.string :as str]))

(defn double-up-single-quotes [s]
  (str/replace s #"(?<!')'(?!')" "''"))

(defn trim-leading-zeros [input]
  (loop [s input]
    (if-not (= (get s 0) \0)
      s
      (recur (subs s 1)))))
