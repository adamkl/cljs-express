(ns app
  (:require [cljs-express.core :refer [start]]))

(defonce server (atom nil))

(defn start! []
  (println "starting")
  (reset! server (start)))

(defn stop! []
  (println "stopping")
  (.close @server)
  (reset! server nil))

(defn main []
  (start!))
