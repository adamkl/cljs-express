(ns app
  (:require [cljs-express :refer [express start stop]]))

(defonce app (atom nil))

(def routes [["/" :get (fn [req res] (.send res "Hello everyone!"))]])

(defn start! []
  (reset! app (-> (express {:routes routes})
                  (start 3000 #(println "Example app listening on port 3000!")))))

(defn stop! []
  (stop @app #(println "Example app has been stopped"))
  (reset! app nil))

(defn main []
  (start!))

(comment
  (comment))
