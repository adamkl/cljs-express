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


(def routes [["/sync" :get get-sync]
             ["/sync-err" :get get-sync-unhandled-err]
             ["/async" :get get-async]
             ["/async-err" :get get-async-unhandled-err]])

(use-fixtures :once
  {:before #(reset! app (express {:routes routes}))
   :after #(reset! app nil)})

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