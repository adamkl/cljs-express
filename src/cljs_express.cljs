(ns cljs-express
  (:require ["express" :as express-fn]
            [cljs-express.router :refer [build-router]]))

(defn start
  ([^js app]
   (.listen app))
  ([^js app port]
   (.listen app port))
  ([^js app port cb]
   (.listen app port cb)))

(defn stop
  ([^js server]
   (.close server))
  ([^js server cb]
   (.close server cb)))

(defn express [{:keys [routes]}]
  (let [router (build-router routes)]
    (doto (express-fn)
      (.use router))))
