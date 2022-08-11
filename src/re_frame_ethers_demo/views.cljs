(ns re-frame-ethers-demo.views
  (:require [re-frame.core :as rf :refer [subscribe]]
            [reagent.core :as rg]
            [re-frame-ethers-demo.utils :refer [<sub >evt] :as utils]
            [re-frame-ethers-demo.components.navbar.views :as nav]
            [re-frame-ethers-demo.components.footer.views :as footer]
            [re-frame-ethers-demo.pages.home.views :as home]
            [re-frame-ethers-demo.pages.about.views :as about]
            [re-frame-ethers-demo.pages.contract.views :as contract]
            [re-frame-ethers-demo.route :as route]
            [re-frame-ethers-demo.error :refer [error-boundary]]
            [re-frame-ethers-demo.components.web3.events :refer [listen-mm-events]]))

(enable-console-print!)

(defn show-panel [active-panel]
  [(case active-panel
     :home-panel home/main
     :contract-panel contract/main
     :about-panel about/main)])


(defn _main-panel
  []
  (let [active-panel (subscribe [:active-panel])
        current-route (subscribe [:current-route])]
    (fn []
      [error-boundary
       [nav/main route/router @current-route]
       (when (some? @active-panel)
         [:div
          [:main [show-panel @active-panel]]
          ;;[alert/main]
          ])
       [footer/main]]
       )))

(defn main-panel
  []
  (rg/create-class
   {:component-did-mount listen-mm-events
    :reagent-render _main-panel
    }))
