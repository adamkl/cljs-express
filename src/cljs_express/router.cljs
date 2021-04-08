(ns cljs-express.router
  (:require ["express" :as express]
            [clojure.core.match :refer [match]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [applied-science.js-interop :as jsi]
            [cljs-express.middleware :refer [wrap-middleware]]))

(s/def ::router-opts map?)
(s/def ::opt-map (s/keys :req-un [::router-opts]))
(s/def ::route vector?)
(s/def ::route-args (s/or :opts-and-routes (s/cat :opts ::opt-map :routes (s/+ ::route))
                          :just-routes (s/+ ::route)))

(defn- conform-route-args [x]
  (let [conformed (s/conform ::route-args x)]
    (if (s/invalid? conformed)
      (throw (ex-info (expound/expound-str ::route-args x) {}))
      (apply hash-map conformed))))

(defn- get-router-and-routes [conformed]
  (match [conformed]
    [{:opts-and-routes r}] [(.Router express (-> r :opts :router-opts (clj->js))) (-> r :routes)]
    [{:just-routes r}] [(.Router express) r]))

(defn build-router [r]
  (let [conformed (conform-route-args r)
        [router routes] (get-router-and-routes conformed)]
    (doseq [[path method-or-nested-routes middleware] routes]
      (if (coll? method-or-nested-routes)
        (jsi/call router
                  :use
                  path
                  (build-router method-or-nested-routes))
        (jsi/call router
                  method-or-nested-routes
                  path
                  (wrap-middleware middleware))))
    router))