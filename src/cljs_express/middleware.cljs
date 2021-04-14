(ns cljs-express.middleware
  (:require [applied-science.js-interop :as jsi]
            [clojure.core.async :refer [go <!]]
            [clojure.set :refer [difference]]
            [cljs-express.util :refer [js->clj+ chan?]]))


(defn filter-keys
  "Filters keys that will cause req mapping to blow up. 
   Keeps only keys to support basic functionality for now"
  [keys]
  (-> (filter #(not (= \_ (get % 0))) keys)
      (set)
      (difference #{"client"
                    "route"
                    "next"
                    "socket"
                    "res"
                    "rawTrailers"
                    "httpVersionMajor"
                    "httpVersionMinor"
                    "upgrade"
                    "rawHeaders"})))

(defn req->map [^js req]
  (let [keys (filter-keys (.keys js/Object req))]
    (js->clj+ req :keys keys :keywordize-keys true)))

(defn process-new-ctx [new-ctx req res next]
  (if-let [{:keys [status body]} (:response new-ctx)]
    (doto res
      (.status status)
      (.send body))
    (do
      (doseq [key (keys (:request new-ctx))]
        (jsi/assoc! req key (clj->js (get-in new-ctx [:request key]))))
      (next))))

(defn wrap-middleware [middleware]
  ; cljs-express middleware only uses one arg
  ; js middleware uses 2+
  (if (= 1 (jsi/get middleware :length))
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
              (process-new-ctx new-ctx-or-err req res next)))
          (catch js/Error err
            (next err)))))
    middleware))