(ns helcb.commons)

(defn button [value on-click]
  [:input.button
   {:type :submit
    :value value
    :on-click on-click}])


(defn input
  [type name value on-change on-enter style]
   [:label.label {:for name} name]
   [:input {:type type
            :name name
            :value value
            :style style
            :on-key-up (fn [event] (when (= (. event -key) "Enter") (on-enter)))
            :on-change #(on-change (-> % .-target .-value))}])

(defn text-input 
  [name value on-change on-enter style] 
   [input :text name value on-change on-enter style])
