(ns helcb.clj-utils
  (:require [clojure.string :as str]))

(defn convert-to-bigint-or-zero [s]
  (try (bigint s) (catch Exception e 0)))

(defn convert-time [s]
  (try (java.time.LocalDateTime/parse s) (catch Exception e nil)))

(defn double-up-single-quotes [s]
  (str/replace s #"(?<!')'(?!')" "''"))

(defn trim-leading-zeros [input]
  (loop [s input]
    (if-not (= (get s 0) \0)
      s
      (recur (subs s 1)))))
