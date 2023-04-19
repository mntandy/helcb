(ns helcb.utils)

(defn lazy-flatten-map
  ([m] (lazy-flatten-map (map vector (keys m)) m))
  ([stack m]
   (lazy-seq
    (when (seq stack)
      (let [first-value (get-in m (first stack))]
        (if-not (map? first-value)
          (cons
           (first stack)
           (lazy-flatten-map (rest stack) m))
          (loop [next-paths (map #(conj (first stack) %) (keys first-value))
                 next-stack (rest stack)]
            (let [next-value (get-in m (first next-paths))]
              (if (map? next-value)
                (recur
                 (map #(conj (first next-paths) %) (keys next-value))
                 (concat (rest next-paths) next-stack))
                (cons
                 (first next-paths)
                 (lazy-flatten-map (concat (rest next-paths) next-stack) m)))))))))))


(defn all-vals [m]
  (map #(get-in m %) (lazy-flatten-map m)))
