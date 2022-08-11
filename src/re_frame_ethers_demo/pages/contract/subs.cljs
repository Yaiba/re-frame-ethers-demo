(ns re-frame-ethers-demo.pages.contract.subs
  (:require
   [re-frame.core :refer [reg-sub]]
   [re-frame-ethers-demo.pages.contract.utils :refer [func-output-converter evm-abi-func-filter]]
   [re-frame-ethers-demo.cljs-ethers.core :as cljes]))

(reg-sub
 :contract
 (fn [db]
   (:contract db)))

(reg-sub
 :contract/state
 :<- [:contract]
 (fn [contract _]
   (:state contract)))

(reg-sub
 :contract/address
 :<- [:contract]
 (fn [contract _]
   (:address contract)))

(reg-sub
 :contract/abi
 :<- [:contract]
 (fn [contract _]
   (:abi contract)))

(reg-sub
 :contract/abi-funcs
 :<- [:contract/abi]
 (fn [abi [_ read?]]
   (filter (partial evm-abi-func-filter read?) abi)))

(reg-sub
 :contract/abi-read-func-num
 :<- [:contract/abi-funcs true]
 (fn [abi _]
   (count abi)))

(reg-sub
 :contract/abi-write-func-num
 :<- [:contract/abi-funcs false]
 (fn [abi _]
   (count abi)))

(reg-sub
 :contract/abi-read-funcs
 :<- [:contract/abi-funcs true]
 (fn [abi [_ idx]]
   (nth abi idx)))

(reg-sub
 :contract/abi-write-funcs
 :<- [:contract/abi-funcs false]
 (fn [abi [_ idx]]
   (nth abi idx)))

(reg-sub
 :contract/abi-filename
 :<- [:contract]
 (fn [contract _]
   (:abi-filename contract)))

(reg-sub
 :contract/read-instance
 :<- [:contract]
 :<- [:web3/provider]
 (fn [[{:keys [abi-raw address] :as contract} provider] _]
   (cljes/get-contract address (js/JSON.parse abi-raw) provider)))

(reg-sub
 :contract/write-instance
 :<- [:contract/read-instance]
 :<- [:web3/signer]
 (fn [[instance signer] _]
   (.connect instance signer)))

(reg-sub
 :contract/tx-outputs
 :<- [:contract/state]
 (fn [state _]
   (:tx-outputs state)))

(reg-sub
 :contract/fn-outputs
 :<- [:contract/tx-outputs]
 (fn [tx-outputs [_ fname output-types]]
   (let [_outputs (get tx-outputs fname)
         outputs (map (fn [output otype]
                        ((func-output-converter otype) output))
                      _outputs output-types)]
     (if (nil? outputs)
       []
       outputs))))

(reg-sub
 :contract/fn-input-args
 :<- [:contract/state]
 (fn [state [_ read? func-idx]]
   (let [k (str (if read? "read-" "write-") func-idx)]
     (get-in state [:fn-input-args k]))))

(reg-sub
 :contract/fn-input-arg
 :<- [:contract/state]
 (fn [state [_ read? func-idx arg-idx]]
   (let [k (str (if read? "read-" "write-") func-idx)
         args (get-in state [:fn-input-args k])]
     (if (nil? args)
       nil
       (nth args arg-idx)))))

(reg-sub
 :contract/fn-call-error
 :<- [:contract/state]
 (fn [state [_ fname]]
   (get-in state [:fn-call-error fname] nil)))
