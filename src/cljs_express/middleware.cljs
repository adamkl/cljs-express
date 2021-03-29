(ns cljs-express.middleware
  (:require [applied-science.js-interop :as jsi]
            [clojure.core.async :refer [go <!]]
            [clojure.set :refer [difference]]
            [cljs-express.util :refer [js->clj+ chan?]]))


(defn filter-keys
  "Filters keys that will cause req mapping to blow up"
  [keys]
  (-> (filter #(not (= \_ (get % 0))) keys)
      (set)
      (difference #{"client"
                    "route"
                    "next"
                    "socket"
                    "res"})))

(defn req->map [^js req]
  (let [keys (filter-keys (.keys js/Object req))]
    (js->clj+ req :keys keys :keywordize-keys true)))

(defn process-new-ctx [new-ctx res next]
  (if-let [{:keys [status body]} (:response new-ctx)]
    (doto res
      (.status status)
      (.send body))
    (next)))

(defn wrap-middleware [middleware]
  (fn [req res next]
    (go
      (try
        (let [ctx {:request (req->map req)}
              maybe-chan (middleware ctx)
              new-ctx-or-err (if (chan? maybe-chan)
                               (<! maybe-chan)
                               maybe-chan)]
          (if (instance? js/Error new-ctx-or-err)
            (next new-ctx-or-err)
            (process-new-ctx new-ctx-or-err res next)))
        (catch js/Error err
          (next err))))))