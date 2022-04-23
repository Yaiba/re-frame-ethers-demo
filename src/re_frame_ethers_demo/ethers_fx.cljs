(ns re-frame-ethers-demo.ethers-fx
  (:require
   ["ethers" :as ethers]
   [cljs.spec.alpha :as s]
   [re-frame-ethers-demo.ethers :as es]
   [re-frame.core :refer [reg-fx]]
   [re-frame-ethers-demo.utils :as utils]))

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

(defn rpc-handler
  [provider method-name on-success on-error args]
  (-> (.send provider method-name args)
      (.then (fn [val]
               (utils/promise-dispatch on-success (utils/js->cljkk val))))
      (.catch (fn [err]
                (utils/promise-dispatch on-error err)))
      (.finally true)))

(reg-fx
 :efx/request-accounts
 (fn [{:keys [provider on-success on-error args]}]
   (rpc-handler provider "eth_requestAccounts" on-success on-error args)))

(reg-fx
 :efx/load-accounts
 (fn [{:keys [provider on-success on-error args]}]
   (rpc-handler provider "eth_accounts" on-success on-error args)))


(defn provider-handler
  [{:keys [provider fns]} params]
  (s/assert ::provider-call params)
  (doseq [{:keys [fn args on-success on-error]} (remove nil? fns)]
    (es/provider-call provider fn on-success on-error args)))

;; provider method fx
(reg-fx
 :efx/p-call provider-handler)


(defn- on-event-handler
  [{:keys [provider event listener]}]
  (es/provider-event-call provider "on" event listener))

(reg-fx
 :efx/p-on-event on-event-handler)

(defn- off-event-handler
  [{:keys [provider event listener]}]
  (es/provider-event-call provider "off" event listener))

(reg-fx
 :efx/p-off-event off-event-handler)

(defn- once-event-handler
  [{:keys [provider event listener]}]
  (es/provider-event-call provider "once" event listener))

(reg-fx
 :efx/p-once-event once-event-handler)

(reg-fx
 :efx/watch-blocks on-event-handler)
