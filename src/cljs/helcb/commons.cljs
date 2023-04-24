(ns helcb.commons)

(defn button [value on-click]
  [:button.button {:on-click on-click} value])


(defn input
  ([type name value on-change on-enter style]
   (input type name value on-change on-enter style false))
  ([type name value on-change on-enter style disabled]
   [:label.label {:for name} name]
   [:input {:type type
            :name name
            :value value
            :disabled disabled
            :style style
            :on-key-up (fn [event] (when (= (. event -key) "Enter") (on-enter)))
            :on-change #(on-change (-> % .-target .-value))}]))

(defn text-input
  ([name value on-change on-enter style]
   [input :text name value on-change on-enter style])
  ([name value on-change on-enter style disabled]
   [input :text name value on-change on-enter style disabled]))

