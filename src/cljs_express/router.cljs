(ns cljs-express.router
  (:require ["express" :as express]))

(defn- key->method [method-key]
  (fn [^js router path middleware]
    (case method-key
      :get (.get router path middleware)
      :post (.post router path middleware))))

(defn build-router
  ([routes]
   (build-router nil routes))
  ([opts routes]
   (let [router (.Router express opts)]
     (doseq [[path method-key middleware] routes]
       ((key->method method-key) router path middleware))
     router)))