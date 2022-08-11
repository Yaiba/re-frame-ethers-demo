(ns re-frame-ethers-demo.pages.contract.utils)

(defn func-input-converter [itype]
  (case itype
    ;; TODO more case
    "uint" js/parseInt
    "uint256" js/parseInt
    "uint128" js/parseInt
    "bool" #(if (= % "true") true false)
    identity))

(defn func-output-converter [otype]
  (case otype
    ;; TODO more case
    "uint" #(.toString %)
    "uint256" #(.toString %)
    "unit128" #(.toString %)
    "bool" #(if (= % "true") true false)
    identity))

(defn evm-abi-func-filter
  ;; Return a filter fn.
  ;; Thid will get only read or write functions according to read?
  [read? {:keys [type state-mutability] :as abi}]
  (if read?
    (and (= type "function") (contains? #{"view" "pure"} state-mutability))
    (and (= type "function") (not (contains? #{"view" "pure"} state-mutability)))))

(defn func-field-uniq-key
  "Generate a uniq key from 'function name, arg name, arg type, arg nth'"
  [fname aname atype ath]
  (str fname "-" aname "(" atype ")" "-" ath))

;; abi related
(defn evm-abi-func-signature
  [fname inputs]
  (str fname (map :name inputs)))

