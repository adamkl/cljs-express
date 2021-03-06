(ns integration-test
  (:require [clojure.test :refer [use-fixtures deftest testing is async]]
            [applied-science.js-interop :as jsi]
            ["supertest" :as request]
            [cljs-express :refer [express]]))

(defonce app (atom nil))


(defn hello [ctx]
  (assoc ctx :response {:status 200
                        :body "Hello everyone!"}))

(defn missing [ctx]
  ctx)

(def routes [["/hello" :get hello]
             ["/missing" :get missing]])

(use-fixtures :once
  {:before #(reset! app (express {:routes routes}))
   :after #(reset! app nil)})

(deftest hello-test
  (testing "hello route"
    (async done
           (-> (request @app)
               (.get "/hello")
               (.expect #(is (= 200 (.-status %))))
               (.expect #(is (= "Hello everyone!" (.-text %))))
               (.end done)))))

(deftest missing-test
  (testing "missing route"
    (async done
           (-> (request @app)
               (.get "/missing")
               (.expect #(is (= 404 (.-status %))))
               (.expect #(is (= "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n<title>Error</title>\n</head>\n<body>\n<pre>Cannot GET /missing</pre>\n</body>\n</html>\n" (.-text %))))
               (.end done)))))
