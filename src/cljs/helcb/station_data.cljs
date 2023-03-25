(ns helcb.station-data  
  (:require [reagent.core :as r]))


(def initial-settings
  {:station-id nil})

(def settings (r/atom initial-settings))

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
