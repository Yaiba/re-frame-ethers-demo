(ns re-frame-ethers-demo.ethereum
  (:require ["ethers" :as ethers]
            [cljs.core.async.interop :refer-macros [<p!]]
            [cljs.core.async :refer [go]]
            [camel-snake-kebab.core :as csk]))

(def Ethereum (.-ethereum js/window))
(def Contract ethers/Contract)
(def Utils (.-utils ethers))
(def Provider (.. ethers -providers -Web3Provider))
(def Signer (.-Signer ethers))

(def is-metamask-installed
  (and Ethereum (.-isMetaMask Ethereum)))

(def is-connected (.-is-connected Ethereum))

(defn to-readable-abi [json-abi]
  (-> json-abi
      (Utils.Interface.)
      (.format ethers/FormatTypes.-full)))

(defn request-accounts []
  (.request Ethereum #js {"method" "eth_requestAccounts"}))

(defn get-provider []
  (Provider. Ethereum))

(defn get-contract [addr abi provider]
  (Contract. addr (clj->js abi) provider))

(defn call [contract method args]
  (do (js/console.log "call: " method args)
        (apply (aget contract method) (clj->js args))))
