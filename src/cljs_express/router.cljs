(ns cljs-express.router
  (:require ["express" :as express]
            [clojure.core.match :refer [match]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [applied-science.js-interop :as jsi]
            [cljs-express.middleware :refer [wrap-middleware]]))

(s/def ::router-opts map?)
(s/def ::opt-map (s/keys :req-un [::router-opts]))
(s/def ::path string?)
(s/def ::method keyword?)
(s/def ::middleware (s/+ fn?))
(s/def ::route (s/or :nested-routes (s/cat :path ::path :route-args vector?)
                     :route (s/cat :path ::path :method ::method :middleware ::middleware)))
(s/def ::route-args (s/or :opts-and-routes (s/cat :opts ::opt-map :routes (s/+ ::route))
                          :just-routes (s/+ ::route)))

(defn- conform-route-args [x]
  (let [conformed (s/conform ::route-args x)]
    (if (s/invalid? conformed)
      (throw (ex-info (expound/expound-str ::route-args x) {}))
      (apply hash-map conformed))))

(defn- get-router-and-routes [conformed]
  (match conformed
    {:opts-and-routes r} [(.Router express (-> r :opts :router-opts (clj->js))) (-> r :routes)]
    {:just-routes r} [(.Router express) r]))

(defn build-router [route-args]
  (let [conformed-args (conform-route-args route-args)
        [router routes] (get-router-and-routes conformed-args)]
    (doseq [route routes]
      (match route
        [:route r] (let [{:keys [method path middleware]} r]
                     (jsi/call router
                               method
                               path
                               (clj->js (for [mw middleware] (wrap-middleware mw)))))
        [:nested-routes r] (let [{:keys [path route-args]} r]
                             (jsi/call router
                                       :use
                                       path
                                       (build-router route-args)))))
    router))