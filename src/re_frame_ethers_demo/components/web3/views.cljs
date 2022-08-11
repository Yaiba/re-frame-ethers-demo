(ns re-frame-ethers-demo.components.web3.views
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [re-frame-ethers-demo.utils :refer [<sub >evt]]))

(defn connect-wallet-btn
  []
  (let [accounts (subscribe [:chain/accounts])]
    (fn []
      (let [connected (empty? @accounts)]
        [:button
         {:on-click  (fn [e]
                       (if connected
                         (>evt [:web3/connect-wallet])
                         (js/console.log "wallet already connected")))}
         (if connected "Connect Wallet" (first @accounts))]))))
