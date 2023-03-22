(ns helcb.core
  (:require [helcb.http.core :as http]
            [mount.core :as mount]))

(defn -main
  [& args]
  (mount/start))