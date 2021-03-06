(ns cljs-express.middleware
  (:require [applied-science.js-interop :as jsi]))

(defn req->map [^js req]
  {:_js-req req
   :aborted (jsi/get req :aborted)
   :base-url (jsi/get req :baseUrl)
   :body (jsi/get req :body)
   :complete (jsi/get req :complete)
   :cookies (js->clj (jsi/get req :cookies))
   :fresh (jsi/get req :fresh)
   :headers (js->clj (jsi/get req :headers))
   :hostname (jsi/get req :hostname)
   :http-version (jsi/get req :httpVersion)
   :ip (jsi/get req :ip)
   :ips (js->clj (jsi/get req :ips))
   :method (jsi/get req :method)
   :original-url (jsi/get req :method)
   :params (js->clj (jsi/get req :params))
   :path (jsi/get req :path)
   :protocol (jsi/get req :protocol)
   :query (js->clj (jsi/get req :query))
   :raw-headers (js->clj (jsi/get req :rawHeaders))
   :raw-trailers (js->clj (jsi/get req :rawTrailers))
   :route (jsi/get req :route)
   :secure (jsi/get req :secure)
   :signed-cookies (js->clj (jsi/get req :signedCookies))
   :stale (jsi/get req :stale)
   :subdomains (js->clj (jsi/get req :subdomains))
   :xhr (jsi/get req :xhr)
   :trailers (js->clj (jsi/get req :trailers))
   :url (jsi/get req :url)})

(defn wrap-middleware [middleware]
  (fn [req res next]
    (let [{:keys [response] :as ctx} (middleware {:request (req->map req)})]
      (if-let [{:keys [status body]} response]
        (doto res
          (.status status)
          (.send body))
        (next)))))