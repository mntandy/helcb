(ns helcb.core
  (:require [helcb.http.core :as http]
            [helcb.db.core :as db]
            [mount.core :as mount])
  (:gen-class))

(defn -main
  [& args]
  (mount/start))