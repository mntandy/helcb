(ns helcb.filters)

(defn options-for-type [type]
  (case type
    ("integer" "decimal") ["equal to" "not equal to" "greater than" "less than"] 
    "text" ["equals" "begins with" "ends with" "contains"]))

