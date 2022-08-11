(ns re-frame-ethers-demo.ethers-fx.core
    (:require
     [re-frame.core :refer [subscribe dispatch reg-fx]]
     [cljs.spec.alpha :as s]
     [re-frame-ethers-demo.cljs-ethers.core :as ce]
     [re-frame-ethers-demo.cljs-ethers.provider :as cep]
     [re-frame-ethers-demo.cljs-ethers.contract :as cec]
     [re-frame.core :refer [reg-fx]]
     [re-frame-ethers-demo.ethers-fx.utils :as utils]))
 

(s/def ::id any?)
(s/def ::instance (complement nil?))
(s/def ::dispatch vector?)
(s/def ::contract-fn-arg any?)
(s/def ::address string?)
(s/def ::watch? (s/nilable boolean?))
;;(s/def ::block-filter-opts block-filter-opts?)
(s/def ::provider (complement nil?))
(s/def ::event-ids sequential?)
(s/def ::fn #(or (fn? %) (keyword? %) (string? %)))
(s/def ::args (s/coll-of ::contract-fn-arg))
(s/def ::on-success ::dispatch)
(s/def ::on-error ::dispatch)
(s/def ::on-tx-hash ::dispatch)
(s/def ::on-tx-hash-error ::dispatch)
(s/def ::on-tx-receipt ::dispatch)
(s/def ::on-tx-success ::dispatch)
(s/def ::on-tx-error ::dispatch)
(s/def ::tx-opts map?)
(s/def ::event keyword?)
(s/def ::event-id any?)
(s/def ::event-filter-opts (s/nilable map?))
;;(s/def ::block-filter-opts block-filter-opts?)
(s/def ::tx-hashes (s/coll-of string?))

(s/def ::provider-fn (s/nilable (s/keys :opt-un [::args
                                                 ::on-success
                                                 ::on-error
                                                 ::fn])))
(s/def ::provider-fns (s/coll-of ::provider-fns))
(s/def ::provider-call (s/keys :req-un [::provider ::provider-fns]))

(comment (s/def ::signer-fn (s/nilable (s/keys :opt-un [::signer
                                                        ::args
                                                        ::tx-opts
                                                        ::on-success
                                                        ::on-error
                                                        ::on-tx-hash
                                                        ::on-tx-hash-error
                                                        ::on-tx-receipt
                                                        ::on-tx-success
                                                        ::on-tx-error
                                                        ::fn
                                                        ::instance]))))

(defn promise-handler
  [{:keys [object fns]} params]
  (doseq [{:keys [fn args on-success on-error]} (remove nil? fns)]
    (ce/promise-call object fn
                     (utils/promise-event on-success)
                     (utils/promise-event on-error)
                     args)))

;; general promise fx
(reg-fx
 :efx/promise promise-handler)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(reg-fx
 :efx/request-accounts
 (fn [{:keys [provider on-success on-error args]}]
   (ce/rpc-handler provider "eth_requestAccounts"
                   (utils/promise-event on-success)
                   (utils/promise-event on-error) args)))

(reg-fx
 :efx/load-accounts
 (fn [{:keys [provider on-success on-error args]}]
   (ce/rpc-handler provider "eth_accounts"
                   (utils/promise-event on-success)
                   (utils/promise-event on-error) args)))

;; provider method fx
(defn provider-handler
  [{:keys [provider fns]} params]
  (s/assert ::provider-call params)
  (doseq [{:keys [fn args on-success on-error]} (remove nil? fns)]
    (ce/promise-call provider fn
                     (utils/promise-event on-success)
                     (utils/promise-event on-error) args)))

(reg-fx
 :efx/p-call provider-handler)

(defn- on-event-handler
  [{:keys [provider event listener]}]
  (cep/provider-event-call provider "on" event listener))

(defn- on-block-handler
  [params]
  (on-event-handler (assoc params :event "block")))

(reg-fx
 :efx/p-on-event on-event-handler)

(reg-fx
 :efx/watch-blocks on-block-handler)

(defn- off-event-handler
  [{:keys [provider event listener]}]
  (cep/provider-event-call provider "off" event listener))

(reg-fx
 :efx/p-off-event off-event-handler)

(defn- once-event-handler
  [{:keys [provider event listener]}]
  (cep/provider-event-call provider "once" event listener))

(reg-fx
 :efx/p-once-event once-event-handler)


;; signer method fx

;; contract method fx
(defn contract-handler
  [{:keys [instance fns]} params]
  ;;(s/assert ::provider-call params)
  (doseq [{:keys [fn args on-success on-error]} (remove nil? fns)]
    (ce/promise-call instance fn
                     (utils/promise-event on-success)
                     (utils/promise-event on-error)
                     args)))

(reg-fx
 :efx/c-call contract-handler)
