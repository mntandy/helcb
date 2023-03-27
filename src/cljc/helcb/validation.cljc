(ns helcb.validation
  (:require
   [struct.core :as st]))

(defn str-min-2-validator [k] [[st/required :message (str (name k) " is required.")]
                               [st/min-count 2 :message "Be generous. Use at least two letters in the uri."]])

(def csv-schema
  {:sep [[st/required :message "Separator is required."]
         [st/string :message "Must be a string."]
         [{:validate (fn [s] (re-matches #",|;" s))} :message "there are only two options."]]
   :uri (str-min-2-validator :uri)})

(defn csv-import [params]
  (first (st/validate params csv-schema)))

(def csv-import-success-schema
  {[:result :count] [[st/required :message "Something went wrong with the import"]
                     [st/number-str :message "Something went wrong with the import"]]})

(defn csv-import-success [params]
  (first (st/validate params csv-import-success-schema)))
