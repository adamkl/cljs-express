(ns integration-test
  (:require [clojure.test :refer [use-fixtures deftest testing is async]]
            [clojure.core.async :refer [chan put!]]
            ["supertest" :as request]
            [cljs-express :refer [express]]))

(defonce app (atom nil))

(defn get-sync [ctx]
  (assoc ctx :response {:status 200
                        :body "sync"}))

(defn get-sync-unhandled-err [ctx]
  (throw (ex-info "unhandled sync error" {})))

(defn get-async [ctx]
  (let [c (chan)]
    (put! c (assoc ctx :response {:status 200
                                  :body "async"}))
    c))

(defn get-async-unhandled-err [ctx]
  (let [c (chan)]
    (put! c (ex-info "unhandled async error" {}))
    c))


(def routes [{:router-opts #js{:caseSensitive true}}
             ["/sync" :get get-sync]
             ["/sync-err" :get get-sync-unhandled-err]
             ["/async" :get get-async]
             ["/async-err" :get get-async-unhandled-err]])

(use-fixtures :once
  {:before #(reset! app (express {:routes routes}))
   :after #(reset! app nil)})

(deftest router-opts-test
  (testing "case sensitive route"
    (async done
           (-> (request @app)
               (.get "/Sync")
               (.expect #(is (= 404 (.-status %))))
               (.expect #(is (= "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n<title>Error</title>\n</head>\n<body>\n<pre>Cannot GET /Sync</pre>\n</body>\n</html>\n" (.-text %))))
               (.end done)))))

(deftest sync-test
  (testing "sync route"
    (async done
           (-> (request @app)
               (.get "/sync")
               (.expect #(is (= 200 (.-status %))))
               (.expect #(is (= "sync" (.-text %))))
               (.end done)))))

(deftest sync-unhandled-err-test
  (testing "sync unhandled err route"
    (async done
           (-> (request @app)
               (.get "/sync-err")
               (.expect #(is (= 500 (.-status %))))
               (.expect #(is (re-find #"Error\: unhandled sync error" (.-text %))))
               (.end done)))))

(deftest async-test
  (testing "async route"
    (async done
           (-> (request @app)
               (.get "/async")
               (.expect #(is (= 200 (.-status %))))
               (.expect #(is (= "async" (.-text %))))
               (.end done)))))

(deftest async-unhandled-err-test
  (testing "async unhandled err route"
    (async done
           (-> (request @app)
               (.get "/async-err")
               (.expect #(is (= 500 (.-status %))))
               (.expect #(is (re-find #"Error\: unhandled async error" (.-text %))))
               (.end done)))))