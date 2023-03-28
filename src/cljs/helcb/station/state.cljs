(ns helcb.station.state
  (:require [reagent.core :as r]))

(def initial-settings
  {:station-id nil :parent nil :row nil})

(def settings (r/atom initial-settings))

(def parent (r/cursor settings [:parent]))

(def row (r/cursor settings [:row]))

(def station-example {:name "Hanasaari"
                      :namn "Hanaholmen"
                      :nimi "Hanasaari"
                      :osotie "Hanasaarenranta 1"
                      :adress "Hanaholmsstranden 1"
                      :kaupunki "Espoo"
                      :stad "Esbo"
                      :x "24.840319"
                      :y "60.16582"})

(def info (r/atom station-example))

(defn initialise! [parent row]
  (swap! settings assoc
         :parent parent
         :row row))
