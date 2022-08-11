(ns re-frame-ethers-demo.utils
    (:require
     [clojure.string :as string]
     [re-frame.core :refer [subscribe dispatch]]))

(def <sub (comp deref subscribe))
(def >evt dispatch)

(defn js-event-val
  [e]
  (some-> e .-target .-value))

(def chain-id-name
  {1 "mainnet"
   3 "ropsten"
   4 "rinkeby"
   56 "bsc"
   137 "polygon"
   100 "gnosis"
   1337 "ganache"})

(defn get-chain-name-by-id
  [chain-id]
  (get chain-id-name chain-id "chain not supported"))

;; https://api.etherscan.io/api?module=contract&action=getabi&address=0x49cf6f5d44e70224e2e23fdcdd2c053f30ada28b

;;;; from https://ericnormand.me/guide/state-in-re-frame
(comment (defonce window-size
           (let [a (rg/atom {:width  (.-innerWidth  js/window)
                            :height (.-innerHeight js/window)})]
             (.addEventListener js/window "resize"
                                (fn [] (reset! a {:width  (.-innerWidth  js/window)
                                                  :height (.-innerHeight js/window)})))
             a)))
;;;;
