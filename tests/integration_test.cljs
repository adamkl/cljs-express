(ns integration-test
  (:require [clojure.test :refer [use-fixtures deftest testing is async]]
            ["supertest" :as request]
            [cljs-express :refer [express]]))

(defonce app (atom nil))

(def routes [["/" :get (fn [req res]
                         (.send res "Hello everyone!"))]])

(use-fixtures :once
  {:before #(reset! app (express {:routes routes}))
   :after #(reset! app nil)})

(deftest hello-test
  (async done
         (-> (request @app)
             (.get "/")
             (.expect #(is (= 200 (.-status %))))
             (.expect #(is (= "Hello everyone!" (.-text %))))
             (.end done))))
