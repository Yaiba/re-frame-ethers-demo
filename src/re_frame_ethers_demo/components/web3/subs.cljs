(ns re-frame-ethers-demo.components.web3.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :web3
 (fn [db]
   (:web3 db)))

(reg-sub
 :web3/provider
 :<- [:web3]
 (fn [web3 _]
   (:provider web3)))

(reg-sub
 :web3/signer
 :<- [:web3]
 (fn [web3 _]
   (:signer web3)))

(reg-sub
 :web3/chain
 :<- [:web3]
 (fn [web3 _]
   (:chain web3)))

(reg-sub
 :chain/accounts
 :<- [:web3/chain]
 (fn [chain _]
   (:accounts chain)))

(reg-sub
 :chain/height
 :<- [:web3/chain]
 (fn [chain _]
   (:height chain)))

(reg-sub
 :chain/id
 :<- [:web3/chain]
 (fn [chain _]
   (:id chain)))

(reg-sub
 :chain/name
 :<- [:web3/chain]
 (fn [chain _]
   (:name chain)))

(reg-sub
 :chain/etherscan-token
 :<- [:web3/chain]
 (fn [chain _]
   (:token chain)))
