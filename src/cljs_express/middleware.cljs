(ns cljs-express.middleware
  (:require [applied-science.js-interop :as jsi]
            [clojure.core.async :refer [go <!]]
            [cljs-express.util :refer [js->clj+ chan?]]))

(def default-req-props
  [:aborted
   :baseUrl
   :body
   :complete
   :cookies
   :fresh
   :headers
   :hostname
   :httpVersion
   :ip
   :ips
   :method
   :params
   :path
   :protocol
   :query
   :rawHeaders
   :rawTrailers
   :route
   :secure
   :signedCookies
   :stale
   :subdomains
   :trailers
   :xhr
   :url])

(defn req->map [^js req props-to-map]
  (-> (fn [result key]
        (assoc result key (js->clj+ (jsi/get req key))))
      (reduce {} props-to-map)))

(defn process-new-ctx [new-ctx res next]
  (if-let [{:keys [status body]} (:response new-ctx)]
    (doto res
      (.status status)
      (.send body))
    (next)))

(defn wrap-middleware [middleware props-to-map]
  (fn [req res next]
    (go
      (try
        (let [ctx {:request (req->map req props-to-map)}
              maybe-chan (middleware ctx)
              new-ctx-or-err (if (chan? maybe-chan)
                               (<! maybe-chan)
                               maybe-chan)]
          (if (instance? js/Error new-ctx-or-err)
            (next new-ctx-or-err)
            (process-new-ctx new-ctx-or-err res next)))
        (catch js/Error err
          (next err))))))