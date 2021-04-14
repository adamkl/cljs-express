(ns user
  (:require [cljs-express :refer [express start stop]]
            ["supertest" :as request]))

(defonce app (atom nil))

(defn hello [ctx]
  (assoc ctx :response {:status 200
                        :body "Hello everyone!"}))

(def routes [["/hello" :get hello]])

(defn start! []
  (print "creating app")
  (reset! app (-> (express {:routes routes})
                  #_(start 3000 #(println "Example app listening on port 3000!")))))

(defn stop! []
  (print "destroying app")
  #_(stop @app #(println "Example app has been stopped"))
  (reset! app nil))

(defn main []
  (start!))

(comment
  (start!)
  (stop!)
  (-> (request @app)
      (.get "/hello?message=hello")
      (.expect #(assert (= 201 (.-status %))))
      (.end #(identity %)))

  (comment))

