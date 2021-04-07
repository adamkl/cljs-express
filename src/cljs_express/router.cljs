(ns cljs-express.router
  (:require ["express" :as express]
            [clojure.core.match :refer [match]]
            [clojure.spec.alpha :as s]
            [applied-science.js-interop :as jsi]
            [cljs-express.middleware :refer [wrap-middleware]]))

(s/def ::router-opts map?)
(s/def ::opt-map (s/keys :req-un [::router-opts]))
(s/def ::route keyword?)
(s/def ::route-args (s/or :opts-and-routes (s/cat :opts ::opt-map :routes (s/+ ::route))
                          :just-routes (s/+ ::route)))

(comment
  (defn conform-route-args [x]
    (let [conformed (apply hash-map (s/conform ::route-args x))]
      (match [conformed]
        [{:opts-and-routes onr}] onr
        [{:just-routes routes}] routes)))

  (conform-route-args [:a :b :c])
  (conform-route-args [{:router-opts {:1 "val"}} :a :b :c])
  (conform-route-args [{:invalid 10} :a :b :c])

  (comment))

(defn map-routes
  ([router routes]
   (doseq [[path method-key middleware] routes]
     (jsi/call router
               method-key
               path
               (wrap-middleware middleware)))
   router))

(defn build-router
  [r]
  (let [opts (first r)
        routes (if (map? opts) (rest r) r)
        router (.Router express (:router-opts opts))]
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