(ns helcb.import.state
  (:require
   [reagent.core :as r]
   [helcb.http :as http]
   [helcb.state :as state]
   [helcb.columns :as columns]))

(def initial-import {:importing false
                     :uri "journeys.csv"
                     :sep \,})

(def import-data (r/atom initial-import))

(defn reset-to-initial! []
  (reset! import-data initial-import))

(def importing (r/cursor import-data [:importing]))
(def uri (r/cursor import-data [:uri]))
(def sep (r/cursor import-data [:sep]))

(defn csv []
  (select-keys @import-data [:uri :sep]))

(defn update! [key]
  (fn [params]
    (swap! import-data assoc key params)))

(defn seq-to-text [col]
  (apply str (first col)
         (map (fn [x y] (str x y)) (repeat ", ") (rest col))))

(defn csv-import-success! [columns-key result]
  (reset-to-initial!)
  (state/set-message! (if (= 0 (:line result))
                        [:div [:p "The file did not contain any importable rows. It must contain the following columns: " 
                                       (seq-to-text (columns/for-db columns-key :label))]]
                        [:div [:p "Imported " (:imported result) " rows out of " (:line result) ". "] 
                         (if (seq (:ignored result)) 
                           [:p "The following lines were not imported:" [:br] 
                               (seq-to-text (:ignored result)) ". "]
                           [:p "No importable line was ignored. "])
                           [:p "Did you expect more lines to be imported? Duplicates and errorous entries are ignored."]])))

(defn submit-csv! [columns route]
  (swap! import-data assoc :importing true)
  (state/set-message! "The link has been sent to the server. Importing could take a while, depending on the number of entries. You will get feedback eventually as long as you remain on the site.")
  (http/post! route (csv) (fn [response] (csv-import-success! columns response))))

