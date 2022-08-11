(ns re-frame-ethers-demo.config)


(goog-define debug? false)

(when debug?
  (enable-console-print!))

(goog-define etherscan-apikey "")
