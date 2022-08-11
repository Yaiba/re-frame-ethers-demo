(ns re-frame-ethers-demo.components.navbar.views
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [re-frame-ethers-demo.utils :refer [<sub >evt chain-id-name] :as utils]
   [reitit.core :as rt]
   [re-frame-ethers-demo.route :as route]
   [re-frame-ethers-demo.error :refer [error-boundary]]
   [re-frame-ethers-demo.components.web3.views :refer [connect-wallet-btn]]))


(defn simple-logo
  []
  [:svg {:height 90 :width 200}
   [:a {:href (route/href ::route/home)}
    [:text {:x 10 :y 10}
     [:tspan {:x 10 :y 35 :style {:fill "green"}} "re-frame"]
     [:tspan {:x 0 :y 60 :style {:fill "salmon" :font-weight "bold"}} "ethers-demo"]]]])


(defn navs
  [router current-route]
  [:<>
   (for [route-name (rt/route-names router)
         :let       [_route (rt/match-by-name route/router route-name)
                     text (-> _route :data :link-text)]
         :when      (not= text "Home")]
     [:div {:key route-name :style {:margin "auto 20px"}}
      (when (= route-name
               (-> current-route :data :name)) "> ")
      [:a {:href (route/href route-name)} text]])])

(defn err-box
  []
  (let [msg (<sub [:error-msg])
        show (boolean msg)]
    [:p {:style {:display (if show "block" "none")
                 :color "red"}}
     msg]))

(defn main
  [router current-route]
  [error-boundary
   [:div
    [err-box]
    [:div {:style {:background-color "#95A5A6" :display "flex" :justify-content "space-between"}}
     [:div [simple-logo]]   
     [:div {:style {:display "flex" :flex-direction "row" :justify-content "flex-end"}}
      [navs router current-route]
      [:div {:style {:margin "auto"}} [connect-wallet-btn]]]
     ]]])

;; (defn gen-key! []
;;   (let [next (swap! uniq-key inc)]
;;     next))
