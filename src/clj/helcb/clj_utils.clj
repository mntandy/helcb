(ns helcb.clj-utils
  (:require [clojure.string :as str]))

(defn convert-to-bigint-or-zero [s]
  (try (bigint s) (catch Exception e 0)))

(defn convert-time [s]
  (try (java.time.LocalDateTime/parse s) (catch Exception e nil)))

(defn convert-time-with-pattern [s]
    (try 
      (java.time.LocalDateTime/parse 
       s 
       (java.time.format.DateTimeFormatter/ofPattern "dd.MM.uuuu HH:mm:ss")) 
      (catch Exception e nil)))

(defn days-between-inclusive [ldt-x ldt-y]
  (inc (.until ldt-x ldt-y java.time.temporal.ChronoUnit/DAYS)))

(defn days-between [ldt-x ldt-y] 
  (.until ldt-x ldt-y java.time.temporal.ChronoUnit/DAYS))

(defn weeks-between [ldt-x ldt-y]
  (.until ldt-x ldt-y java.time.temporal.ChronoUnit/WEEKS))

(defn dow [ldt]
  (.getValue (.getDayOfWeek ldt)))

(defn extra-days [ldt-x ldt-y]
  (let [days-left (rem (days-between ldt-x ldt-y) 7)
        d (.minusDays ldt-y days-left)] 
    (map #(.plusDays d %) (range (inc days-left)))))

(defn weekend-days-between [ldt-x ldt-y]
  (+
   (* 2 (weeks-between ldt-x ldt-y))
   (count (filter (fn [ldt] (some #{(dow ldt)} [6,7])) (extra-days ldt-x ldt-y)))))

(defn weekdays-between [ldt-x ldt-y]
  (- (days-between-inclusive ldt-x ldt-y) (weekend-days-between ldt-x ldt-y)))

(defn is-at-least-ten-seconds-after? [ldt-x ldt-y]
  (.isBefore (.plusSeconds ldt-x 10) ldt-y))

(defn double-up-single-quotes [s]
  (str/replace s #"(?<!')'(?!')" "''"))

(defn trim-leading-zeros [input]
  (loop [s input]
    (if-not (= (get s 0) \0)
      s
      (recur (subs s 1)))))


(defn split-into-bigrams [input]
  (let [lc-input (str/lower-case input)]
    (reduce (fn [result next]
              (conj result (subs lc-input next (+ next 2))))
            []
            (range (dec (count input))))))

;below code is from https://andersmurphy.com/2020/12/13/clojure-string-similarity.html
(defn mag [v]
  (->> (map #(* % %) v)
       (reduce +)
       Math/sqrt))

(defn dot [a b]
  (->> (map * a b)
       (reduce +)))

(defn cosine [a-string b-string]
  (let [a-tokens (split-into-bigrams a-string)
        b-tokens (split-into-bigrams b-string)
        all-tokens (distinct (concat a-tokens b-tokens))
        a-vector (map #(get (frequencies a-tokens) % 0) all-tokens)
        b-vector (map #(get (frequencies b-tokens) % 0) all-tokens)]
    (/ (dot a-vector b-vector)
       (* (mag a-vector) (mag b-vector)))))
