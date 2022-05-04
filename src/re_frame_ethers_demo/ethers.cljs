(ns re-frame-ethers-demo.ethers
  (:require ["ethers" :as ethers]
            [oops.core :refer [oget oset! ocall oget+ ocall+]]
            [re-frame-ethers-demo.utils :as utils]
            ))

(def Contract ethers/Contract)
(def Utils (.-utils ethers))
(def Provider (.. ethers -providers -Web3Provider))
(def Signer (.-Signer ethers))

(defn get-provider
  [ethereum]
  (Provider. ethereum))

(defn to-readable-abi [json-abi]
  (-> json-abi
      (Utils.Interface.)
      (.format ethers/FormatTypes.-full)))

(defn get-contract [addr abi provider]
  (Contract. addr (clj->js abi) provider))

(defn call [contract method args]
  (do (js/console.log "call: " method args)
        (apply (aget contract method) (clj->js args))))


(defn provider-call
  [provider method-name on-success on-error & args]
  (let [method-name (utils/camel-case (name method-name))]
    (if (oget+ provider method-name)
      (-> (ocall+ provider method-name (utils/args-cljkk->js args))
          (.then (utils/promise-action on-success))
          (.catch (utils/promise-action on-error)))
      (throw (str "Method: " method-name " was not found in object.")))))

(defn provider-event-call
  "Call provider event method.
   event are:
   - named event
   - transaction event
   - filter events
  "
  [provider method-name event listener]
  (ocall+ provider method-name event listener))

(defn signer-call
  []
  true)

(defn contract-call
  []
  true)

