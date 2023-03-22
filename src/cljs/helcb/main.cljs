(ns helcb.main
  (:require [reagent.dom :as dom]))

(defn main []
  (fn []
    [:div
     [:div.columns.is-centered.m-6 "Okey!"]
     ]))

(dom/render
 [main]
 (.getElementById js/document "app"))