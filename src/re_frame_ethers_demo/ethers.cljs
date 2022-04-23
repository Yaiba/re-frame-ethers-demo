(ns re-frame-ethers-demo.ethers
  (:require ["ethers" :as ethers]
            [oops.core :refer [oget oset! ocall oget+ ocall+]]
            [re-frame-ethers-demo.utils :as utils]
            ))


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
  (ocall provider method-name event listener))

(defn signer-call
  []
  true)

(defn contract-call
  []
  true)

