(ns cljs-express.router
  (:require ["express" :as express]
            [applied-science.js-interop :as jsi]
            [cljs-express.middleware :refer [wrap-middleware default-req-props]]))

(defn build-router
  ([routes]
   (build-router nil routes))
  ([opts routes]
   (let [{:keys [props-to-map
                 router-opts]} opts
         router (.Router express router-opts)]
     (doseq [[path method-key middleware] routes]
       (jsi/call router
                 method-key
                 path
                 (wrap-middleware middleware
                                  (concat default-req-props props-to-map))))
     router)))