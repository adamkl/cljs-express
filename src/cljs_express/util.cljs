(ns cljs-express.util
  (:require [clojure.core.async.impl.channels :refer [ManyToManyChannel]]))

;; I should model this more closely after the standard js->clj?
;; Doesn't properly handle arrays with non-Object values
(defn js->clj+
  [obj & [{:keys [keywordize-keys]}]]
  (let [keyfn (if keywordize-keys keyword str)]
    (if (goog/isObject obj)
      (-> (fn [result key]
            (let [v (.get goog/object obj key)
                  t (goog/typeOf v)]
              (case t
                "function" result
                "array" (assoc result (keyfn key) (js->clj v))
                (assoc result (keyfn key) (js->clj+ v)))))
          (reduce {} (.getKeys ^js goog/object obj)))
      obj)))

(defn chan? [c]
  (instance? ManyToManyChannel c))