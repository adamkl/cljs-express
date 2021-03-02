(ns cljs-express.middleware.json
  (:require [clojure.spec.alpha :as s]
            ["express" :as express]))

(s/def ::inflate boolean?)
(s/def ::limit (s/or :string string?
                     :number number?))
(s/def ::reviver fn?)
(s/def ::strict boolean?)
(s/def ::type (s/or :string string?
                    :collection (s/coll-of string?)
                    :function fn?))
(s/def ::verify fn?)
(s/def ::opts (s/keys :opt-un [::inflate
                               ::limit
                               ::reviver
                               ::strict
                               ::type
                               ::verify]))
(defn json
  ([app]
   (json app "/" {}))
  ([app path]
   (json app path {}))
  ([app path opts]
   (.use app path (.json express opts))))
