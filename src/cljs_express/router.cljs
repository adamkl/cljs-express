(ns cljs-express.router
  (:require ["express" :as express]
            [clojure.core.match :refer [match]]
            [clojure.spec.alpha :as s]
            [applied-science.js-interop :as jsi]
            [cljs-express.util :refer [conform-or-throw]]
            [cljs-express.middleware :refer [wrap-middleware]]))

(s/def ::path string?)
(s/def ::method #{:all
                  :checkout
                  :copy
                  :delete
                  :get
                  :head
                  :lock
                  :merge
                  :mkactivity
                  :mkcol
                  :move
                  :m-search
                  :notify
                  :options
                  :patch
                  :post
                  :purge
                  :put
                  :report
                  :search
                  :subscribe
                  :trace
                  :unlock
                  :unsubscribe})

(s/def ::middleware (s/+ fn?))
(s/def ::router-opts map?)
(s/def ::router-middleware (s/coll-of fn? :kind vector?))
(s/def ::opts (s/keys :opt-un [::router-opts
                               ::router-middleware]))
(s/def ::route (s/or :nested-routes (s/cat :path ::path :route-args vector?)
                     :route (s/cat :path ::path :method ::method :middleware ::middleware)))
(s/def ::route-args (s/or :opts-and-routes (s/cat :opts ::opts :routes (s/+ ::route))
                          :just-routes (s/+ ::route)))

(defn- get-router-and-routes [conformed]
  (match conformed
    [:opts-and-routes r] (let [{:keys [router-opts router-middleware]} (:opts r)
                               router (jsi/call express :Router (clj->js router-opts))
                               routes (:routes r)]
                           (if router-middleware
                             (jsi/call router
                                       :use
                                       (clj->js (map wrap-middleware router-middleware))))
                           [router routes])
    [:just-routes r] [(jsi/call express :Router) r]))

(defn build-router [route-args]
  (let [conformed-args (conform-or-throw ::route-args route-args)
        [router routes] (get-router-and-routes conformed-args)]
    (doseq [route routes]
      (match route
        [:route r] (let [{:keys [method path middleware]} r]
                     (jsi/call router
                               method
                               path
                               (clj->js (map wrap-middleware middleware))))
        [:nested-routes r] (let [{:keys [path route-args]} r]
                             (jsi/call router
                                       :use
                                       path
                                       (build-router route-args)))))
    router))