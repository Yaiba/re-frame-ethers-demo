(ns re-frame-ethers-demo.components.footer.views
  (:require
   [re-frame-ethers-demo.utils :refer [<sub >evt get-chain-name-by-id]]))

(defn chain-height
  []
  (let [chain-height (<sub [:chain/height])]
    [:p {:style {:color "pink"}}
     "Block height: " chain-height]))

(defn chain-id
  []
  (let [chain-id (<sub [:chain/id])]
    [:p {:style {:color "pink"}}
     (str (get-chain-name-by-id chain-id) " Chain(" chain-id ")")]))

(defn main
  []
  [:div {:style {:text-align "right"}}
   [chain-height]
   [chain-id]])
