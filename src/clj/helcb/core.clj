(ns helcb.core
  (:require [helcb.http.core :as http]
            [helcb.db.core :as db]
            [mount.core :as mount]
            [helcb.columns :as columns]))

(defn -main
  [& args]
  (mount/start))