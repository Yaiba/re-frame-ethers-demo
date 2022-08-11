(ns re-frame-ethers-demo.ethers-fx.utils
    (:require
     [re-frame.core :refer [dispatch]]
     [re-frame-ethers-demo.cljs-ethers.utils :refer [js->cljkk js->cljk]]))

(defn promise-dispatch
  [handler val]
  (when handler
    (dispatch (conj handler val))))

(defn promise-event
  [event]
  (fn [v]
    (promise-dispatch event (js->cljkk v))))
