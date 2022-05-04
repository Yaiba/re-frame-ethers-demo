(ns re-frame-ethers-demo.ethereum)

(def Ethereum (.-ethereum js/window))

(def is-metamask-installed
  (and Ethereum (.-isMetaMask Ethereum)))

(def is-connected (.-is-connected Ethereum))

(defn request-accounts []
  (.request Ethereum #js {"method" "eth_requestAccounts"}))
