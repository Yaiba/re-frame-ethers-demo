(ns re-frame-ethers-demo.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [re-frame-ethers-demo.events :as events]
   [re-frame-ethers-demo.views :as views]
   [re-frame-ethers-demo.config :as config]
   [re-frame-ethers-demo.subs]
   ))


(defn dev-setup []
  (when config/debug?
    (do
      (enable-console-print!)
      (println "dev mode"))))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
