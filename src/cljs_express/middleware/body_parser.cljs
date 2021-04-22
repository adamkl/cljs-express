(ns cljs-express.middleware.body-parser
  (:require [clojure.spec.alpha :as s]
            [cljs-express.util :refer [conform-or-throw]]
            ["express" :as express]))

(s/def ::defaultCharset string?)
(s/def ::inflate boolean?)
(s/def ::limit
  (s/or :string string?
        :number number?))
(s/def ::reviver fn?)
(s/def ::strict boolean?)
(s/def ::type
  (s/or :string string?
        :collection (s/coll-of string?)
        :function fn?))
(s/def ::verify fn?)

(s/def ::json-opts
  (s/keys :opt-un [::inflate
                   ::limit
                   ::reviver
                   ::strict
                   ::type
                   ::verify]))

(s/fdef json
  :args ::json-opts
  :ret fn?)
(defn json
  ([]
   (.json express))
  ([opts]
   (let [conformed (conform-or-throw ::json-opts opts)]
     (.json express (clj->js conformed)))))


(s/def ::raw-opts
  (s/keys :opt-un [::inflate
                   ::limit
                   ::type
                   ::verify]))
(defn raw
  ([]
   (.raw express))
  ([opts]
   (let [conformed (conform-or-throw ::raw-opts opts)]
     (.raw express (clj->js conformed)))))

(s/def ::text-opts
  (s/keys :opt-un [::defaultCharset
                   ::inflate
                   ::limit
                   ::type
                   ::verify]))

(defn text
  ([]
   (.text express))
  ([opts]
   (let [conformed (conform-or-throw ::text-opts opts)]
     (.text express (clj->js conformed)))))