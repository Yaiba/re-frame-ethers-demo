(ns re-frame-ethers-demo.subs
  (:require
   [re-frame.core :refer [reg-sub]]
   [re-frame-ethers-demo.components.web3.subs]))

(reg-sub
 :eth-ready?
 (fn [db]
   (:eth-ready? db)))

(reg-sub
 :error-msg
 (fn [db]
   (:error-msg db)))

(reg-sub
 :mm-update
 (fn [db]
   (:mm-update db)))

(comment (reg-sub
          :error?
          :<- [:error-msg]
          (fn [msg _]
            ((comp complement empty?) msg))))

(reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(reg-sub
 :panel-params
 (fn [db _]
   (:panel-params db)))

(reg-sub
 :current-route
 (fn [db _]
   (:current-route db)))
