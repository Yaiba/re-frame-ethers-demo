(ns re-frame-ethers-demo.db
  (:require [cljs.spec.alpha :as s]
            [re-frame-ethers-demo.cljs-ethers.core :as es]))

(defn eth-addr?
  [x]
  (and
   (string? x)
   (= 42 (count x))
   (clojure.string/starts-with? x "0x")))

(defn empty-addr?
  [x]
  (and
   (string? x)
   (empty? x)))

(defn address?
  [x]
  (or
   (eth-addr? x)
   (empty-addr? x)
   ))

(s/def ::eth-addr eth-addr?)
(s/def ::empty-addr empty-addr?)

(s/def ::address (s/or :eth-addr eth-addr?
                       :empty-addr empty-addr?))

(s/def ::abi-raw string?)
(s/def ::abi-filename string?)
(s/def ::abi (s/coll-of map? :kind vector?))
(s/def ::readable-abi (s/coll-of string? :kind vector?))
(s/def ::contract (s/keys :req-un [::abi ::address ::abi-raw]
                          :opt-un [::abi-filename]))

(s/def ::accounts (s/nilable array?))

(s/def ::id nat-int?)
(s/def ::name string?)
(s/def ::height nat-int?)
(s/def ::chain (s/keys
                :req-un [::accounts ::id ::name ::height]))

(s/def ::provider (s/nilable #(instance? es/Provider %)))
(s/def ::signer (s/nilable #(instance? es/Signer %)))

(s/def ::web3 (s/keys :req-un [::chain ::provider ::signer]))

(s/def ::db (s/keys :req-un [::web3 ::contract]))


(def default-db
  {:web3 {:provider nil
          :signer nil
          :chain {:accounts []
                  :id 0
                  :name ""
                  :height 0}}
   :contract {:abi []
              :abi-raw ""
              :readable-abi []
              :abi-filename ""
              :address ""
              :state {}}
   :current-route nil
   })

