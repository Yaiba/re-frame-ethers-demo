(ns re-frame-ethers-demo.db
  (:require [cljs.spec.alpha :as s]))


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
  {:web3 {:provider nil
          :signer nil
          :chain {:accounts []
                  :id 0
                  :name ""
                  :height 0}}
   :contract {:abi []
              :abi-raw []
              :readable-abi []
              :abi-filename ""
              :address ""
              :state {}}
   :current-route nil
   ;:active-panel :nil
   ;:panel-params nil
   })


;;(def uniq-key (r/atom 0))
;;(def app-state (r/atom default-app-state))
;;(def addr (r/atom "0x..."))

