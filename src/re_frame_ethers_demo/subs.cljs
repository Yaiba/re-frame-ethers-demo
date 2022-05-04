(ns re-frame-ethers-demo.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::eth-ready?
 (fn [db]
   (:eth-ready? db)))

(reg-sub
 ::error-msg
 (fn [db]
   (:error-msg db)))

(reg-sub
 ::mm-update
 (fn [db]
   (:mm-update db)))

(comment (reg-sub
          ::error?
          :<- [:error-msg]
          (fn [msg _]
            ((comp complement empty?) msg))))

(reg-sub
 ::ethers
 (fn [db]
   (:ethers db)))

(reg-sub
 ::ethers-provider
 :<- [::ethers]
 (fn [ethers _]
   (:provider ethers)))

(reg-sub
 ::ethers-signer
 :<- [::ethers]
 (fn [ethers _]
   (:signer ethers)))

(reg-sub
 ::chain
 (fn [db]
   (:chain db)))

(reg-sub
 ::chain-accounts
 :<- [::chain]
 (fn [chain _]
   (:accounts chain)))

(reg-sub
 ::chain-height
 :<- [::chain]
 (fn [chain _]
   (:height chain)))

(reg-sub
 ::chain-id
 :<- [::chain]
 (fn [chain _]
   (:id chain)))

(reg-sub
 ::chain-name
 :<- [::chain]
 (fn [chain _]
   (:name chain)))

(reg-sub
 ::contract
 (fn [db]
   (:contract db)))

(reg-sub
 ::contract-address
 :<- [::contract]
 (fn [contract _]
   (:address contract)))

(reg-sub
 ::contract-abi
 :<- [::contract]
 (fn [contract _]
   (:abi contract)))

(reg-sub
 ::contract-abi-filename
 :<- [::contract]
 (fn [contract _]
   (:abi-filename contract)))
