(ns cljs-express.middleware.json
  (:require [clojure.spec.alpha :as s]
            [cljs-express.util :refer [conform-or-throw]]
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
  ([]
   (.json express))
  ([opts]
   (let [conformed (conform-or-throw ::opts opts)]
     (.json express (clj->js conformed)))))
