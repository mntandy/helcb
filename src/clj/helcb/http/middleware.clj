(ns helcb.http.middleware
  (:require
   [muuntaja.middleware :refer [wrap-format wrap-params]]))

(defn wrap-formats [handler]
  (-> 
   handler
   wrap-params
   (wrap-format)))

(defn wrap-nocache [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Pragma"] "no-cache"))))