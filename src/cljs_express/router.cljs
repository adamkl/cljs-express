(ns cljs-express.router
  (:require ["express" :as express]
            [applied-science.js-interop :as jsi]
            [cljs-express.middleware :refer [wrap-middleware]]))

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