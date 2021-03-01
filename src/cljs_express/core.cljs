(ns cljs-express.core
  (:require ["express" :as express]))


(defn start []
  (let [app (express)]
    (.get app "/" (fn [req res] (.send res "Hello, world")))
    (.listen app 3000 (fn [] (println "Example app listening on port 3000!")))))
