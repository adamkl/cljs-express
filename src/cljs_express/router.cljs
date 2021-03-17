(ns cljs-express.router
  (:require ["express" :as express]
            [applied-science.js-interop :as jsi]
            [cljs-express.middleware :refer [wrap-middleware default-req-props]]))

(defn map-routes
  ([router routes]
   (map-routes router routes nil))
  ([router routes props-to-map]
   (doseq [[path method-key middleware] routes]
     (jsi/call router
               method-key
               path
               (wrap-middleware middleware
                                (concat default-req-props props-to-map))))
   router))

(defn build-router
  [routes]
  (if (map? (first routes))
    (let [{:keys [props-to-map router-opts]} (first routes)]
      (map-routes (.Router express router-opts) (rest routes) props-to-map))
    (map-routes (.Router express) routes)))