(ns re-frame-ethers-demo.cljs-ethers.provider
  (:require
   ["ethers" :as es]
   [oops.core :refer [ocall ocall+ oget+ oapply+]]
   [re-frame-ethers-demo.cljs-ethers.utils :as utils]))


(defn provider-event-call
  "Call provider event method.
   event are:
   - named event
   - transaction event
   - filter events
  "
  [provider method-name event listener]
  (ocall+ provider method-name event listener))


;; (defn get-network
;;   [name-or-id]
;;   (es/getNetwork name-or-id))

(defn get-balance
  ([provider address]
   (ocall provider "getBalance" address "latest"))
  ([provider address block-tag]
   (ocall provider "getBalance" address block-tag)))
