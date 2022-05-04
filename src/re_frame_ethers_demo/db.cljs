(ns re-frame-ethers-demo.db
  (:require [cljs.spec.alpha :as s]
            [cljs.reader]
            [clojure.string :as string]
            [re-frame.core :refer [reg-cofx]]
            [cljs.core.async :refer [<! >! put! chan go go-loop]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [re-frame-ethers-demo.ethers :as ethers]
            [re-frame-ethers-demo.config :as conf]
            [re-frame-ethers-demo.ethereum :as ethereum]))


;;(s/def ::balance int?)
;;(s/def ::block-height int?)


(s/def ::eth-addr (s/and string? #(= 42 (count %)) #(clojure.string/starts-with? % "0x")))

(comment (s/def ::address
           (s/or
            :empty-addr (s/and string? #(= 0 (count %)))
            ::eth-addr)))

(s/def ::address string?)

(s/def ::abi-filename string?)
(s/def ::abi (s/coll-of map? :kind vector?))
(s/def ::readable-abi (s/coll-of string? :kind vector?))
;;(s/def ::contract #(instance? ethers/contract))
(s/def ::contract (s/keys :req-un [::readable-abi ::address]))

;;(s/def ::provider (s/nilable #(instance? ethers/provider %)))
;;(s/def ::signer (s/nilable #(instance? ethers/signer %)))
(s/def ::provider (complement nil?))
(s/def ::signer (complement nil?))
(s/def ::ethers (s/keys :req-un [::provider ::signer]))

(s/def ::web3-ready boolean?)


(s/def ::accounts (s/coll-of ::address :kind vector?))
(s/def ::id nat-int?)
(s/def ::name string?)
(s/def ::height nat-int?)
(s/def ::chain (s/keys
                :req-un [::accounts ::id ::name ::height]))

(s/def ::error-msg string?)


(s/def ::db (s/keys :req-un [::ethers ::contract ::chain ::error-msg]))


(def default-db
  {:ethers {:provider nil
            :signer nil}
   :contract {:abi []
              :readable-abi []
              :abi-filename ""
              :address ""}
   :chain {:accounts []
           :id 0
           :name ""
           :height 0}
   })

(def ls-chain-key "reframe-chain")

(defn chain-info->local-store
  "Puts chain-info into localStorage"
  [chain-info]
  (.setItem js/localStorage ls-chain-key (str chain-info)))     ;; sorted-map written as an EDN map

(reg-cofx
::local-store-chain-info
(fn [cofx _]
  ;; put the localstore todos into the coeffect under :local-store-todos
  (assoc cofx :local-store-chain-info
         ;; read in todos from localstore, and process into a sorted map
         (into (hash-map)
               (some->> (.getItem js/localStorage ls-chain-key)
                        (cljs.reader/read-string)    ;; EDN map -> map
                        )))))


(reg-cofx
 ::eth-injected
 (fn [cofx _]
   (assoc cofx :eth-injected ethereum/is-metamask-installed)))

(reg-cofx
 ::web3-connected
 (fn [cofx _]
   (assoc cofx :web3-connected ethereum/is-connected)))

(reg-cofx
 ::ethers-info
 (fn [{:keys [eth-injected] :as cofx} _]
   (let [provider (ethers/get-provider ethereum/Ethereum)
         signer (.getSigner provider)]
     (if eth-injected
       (assoc cofx :web3-provider provider
                   :web3-signer signer)
       cofx))))


(reg-cofx
 ::base-url
 (fn [cofx _]
   (assoc cofx :base-url (str "https://api.etherscan.io/api?module=contract&action=getabi&apikey="
   conf/etherscan-apikey
   "&address="))))
;;(def uniq-key (r/atom 0))
;;(def app-state (r/atom default-app-state))
;;(def addr (r/atom "0x..."))

