(ns helcb.clj-utils
  (:require [clojure.string :as str]))

(defn integer-string? [s] (re-matches #"-?(0|[1-9]\d*+)" s))

(defn convert-time [s]
  (try (java.time.LocalDateTime/parse s) (catch Exception e nil)))

(defn double-up-single-quotes [s]
  (str/replace s #"(?<!')'(?!')" "''"))

(defn trim-leading-zeros [input]
  (loop [s input]
    (if-not (= (get s 0) \0)
      s
      (recur (subs s 1)))))
