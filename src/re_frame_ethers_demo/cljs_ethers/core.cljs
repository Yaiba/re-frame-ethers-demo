(ns re-frame-ethers-demo.cljs-ethers.core
  (:require ["ethers" :as es]
            [oops.core :refer [oget oset! ocall oget+ ocall+ oapply+]]
            [re-frame-ethers-demo.cljs-ethers.utils :as utils]
            ))

(defn get-provider
  [ethereum]
  (let [provider (.. es -providers -Web3Provider)]
    (provider. ethereum)))

(defn get-contract
  "Return a contract instance from abi(#js) "
  [addr abi provider-or-signer]
  (es/Contract. addr abi provider-or-signer))

(defn get-default-provider
  ([]
   (es/getDefaultProvider "homestead" {}))
  ([network]
   (es/getDefaultProvider network {}))
  ([network options]
   (es/getDefaultProvider network options)))

(defn raw-call [contract method args]
  (if (oget+ contract method)
    (oapply+ contract method (clj->js args))
    (throw (str "Method: " method " was not found in object"))))

(defn promise-fn
  [on-success-fn on-error-fn]
  (fn [object method-name args]
    (let [method-name (utils/camel-case (name method-name))]
      (if (oget+ object method-name)
        (-> (oapply+ object method-name (utils/args-cljkk->js args))
            (.then on-success-fn)
            (.catch on-error-fn))
        (throw (str "Method: " method-name " was not found in object"))))))

(defn promise-call
  [object method-name on-success-fn on-error-fn args]
  (let [method-name (utils/camel-case (name method-name))]
    (if (oget+ object method-name)
      (-> (oapply+ object method-name (utils/args-cljkk->js args))
          (.then on-success-fn)
          (.catch on-error-fn))
      (throw (str "Method: " method-name " was not found in object.")))))

(defn rpc-handler
  [provider method-name on-success-fn on-error-fn args]
  (-> (.send provider method-name args)
      (.then on-success-fn)
      (.catch on-error-fn)
      (.finally true)))
