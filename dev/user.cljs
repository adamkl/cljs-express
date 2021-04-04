(ns cljs.user
  (:require ["express" :as express]))

(def json (.json express))

(defn handler [ctx]
  nil)

(.arguments json)
(type->str handler)

(defn mc [x]
  (meta x))

(.-length json)
(.-length handler)
(mc handler)