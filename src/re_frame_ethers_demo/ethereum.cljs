(ns re-frame-ethers-demo.ethereum)

(def Ethereum (.-ethereum js/window))

(def is-metamask-installed
  (and Ethereum (.-isMetaMask Ethereum)))

(def is-connected (.isConnected Ethereum))
