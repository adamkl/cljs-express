(ns cljs-express.middleware
  (:require [applied-science.js-interop :as jsi]
            [cljs-express.util :refer [js->clj+]]))

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

(defn wrap-middleware [middleware props-to-map]
  (fn [req res next]
    (let [{:keys [response] :as ctx} (middleware {:request (req->map req props-to-map)
                                                  :_req req})]
      (if-let [{:keys [status body]} response]
        (doto res
          (.status status)
          (.send body))
        (next)))))