(ns re-frame-ethers-demo.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [oops.core :refer [ocall]]
   [re-frame-ethers-demo.subs]
   [re-frame-ethers-demo.events]
   [re-frame-ethers-demo.views :as views]
   [re-frame-ethers-demo.config :as config]
   ))

(defn dev-setup []
  (when config/debug?
    (do
      (enable-console-print!)
      (println "dev mode"))))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (ocall js/document "getElementById" "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/clear-subscription-cache!)
  (re-frame/dispatch-sync [:boot])
  (dev-setup)
  (mount-root))
