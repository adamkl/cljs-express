(ns app
  (:require [cljs-express :refer [express start stop]]))

(defonce app (atom nil))


(defn hello [ctx]
  (assoc ctx :response {:status 200
                        :body "Hello everyone!"}))

(defn missing [ctx]
  ctx)

(def routes [["/hello" :get hello]
             ["/missing" :get missing]])

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
