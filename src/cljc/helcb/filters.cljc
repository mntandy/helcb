(ns helcb.filters)

(defn options-for-type [type]
  (case type
    ("integer" "decimal") ["equal to" "not equal to" "greater than" "less than"] 
    "text" ["begins with" "ends with" "contains" "equals"]
    "timestamp" ["before" "after"]))

(defn has-no-option? [filters]
  (fn [column]
    (= (get-in filters [column :option] "Filter") "Filter")))

(defn first-without-option [filters] 
  (first (filter (has-no-option? filters) (keys filters))))


