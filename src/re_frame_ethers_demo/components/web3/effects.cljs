(ns re-frame-ethers-demo.components.web3.effects
  (:require [re-frame.core :refer [reg-cofx]]
            [cljs.reader]
            [re-frame-ethers-demo.config :as conf]
            [re-frame-ethers-demo.ethereum :as ethereum]
            [re-frame-ethers-demo.cljs-ethers.core :as cljes]
            ))

(reg-cofx
 :web3/connected?
 (fn [cofx _]
   (assoc cofx :web3-connected? ethereum/is-connected)))

(reg-cofx
 :web3/injected?
 (fn [cofx _]
   (assoc cofx :web3-injected? ethereum/is-metamask-installed)))

(reg-cofx
 :web3/instance
 (fn [cofx _]
   (if ethereum/is-metamask-installed
     (let [provider (cljes/get-provider ethereum/Ethereum)
           signer (.getSigner provider)]
       (assoc cofx :provider provider
              :signer signer))
     cofx)))

(reg-cofx
 :web3/etherscan-url
 (fn [cofx _]
   (assoc cofx :etherscan-url
          (str "https://api.etherscan.io/api?module=contract&action=getabi&apikey="
               conf/etherscan-apikey
               "&address="))))
