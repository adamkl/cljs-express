(ns cljs-express.util
  (:require [clojure.core.async.impl.channels :refer [ManyToManyChannel]]
            [expound.alpha :as expound]
            [clojure.spec.alpha :as s]))

;; I should model this more closely after the standard js->clj?
;; Doesn't properly handle arrays with non-Object values
(defn js->clj+
  [obj & {:keys [keys keywordize-keys]}]
  (let [keyfn (if keywordize-keys keyword str)]
    (if (goog/isObject obj)
      (-> (fn [result key]
            (let [v (.get goog/object obj key)
                  t (goog/typeOf v)]
              (case t
                "function" result
                "array" (assoc result (keyfn key) (js->clj+ v :keywordize-keys keywordize-keys))
                (assoc result (keyfn key) (js->clj+ v :keywordize-keys keywordize-keys)))))
          (reduce {} (if keys
                       keys
                       (.getKeys ^js goog/object obj))))
      obj)))

(defn chan? [c]
  (instance? ManyToManyChannel c))

(defn conform-or-throw [spec x]
  (let [conformed (s/conform spec x)]
    (if (s/invalid? conformed)
      (throw (ex-info (expound/expound-str spec x) {}))
      conformed)))