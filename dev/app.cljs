(ns app
  (:require [cljs-express :refer [express]]
            [cljs-express.middleware.json :refer [json]]))

(defonce app (atom nil))

(defn start! []
  (println "starting")
  (reset! app (-> (express)
      (.get "/" (fn [req res] (.send res "Hello, world")))
      (.listen 3000 (fn [] (println "Example app listening on port 3000!"))))))

(defn stop! []
  (println "stopping")
  (.close @app)
  (reset! app nil))

(defn main []
  (start!))

(comment
  (def a (-> (express)
             (json)))
  (-> a
      (.post "/" (fn [req res] (.json res (.-body req))))
      (.listen 3001))
  (.close a)
  (comment))
