(ns re-frame-ethers-demo.events.boot
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx inject-cofx]]
            [day8.re-frame.async-flow-fx :as async-flow-fx]
            [re-frame-ethers-demo.route :as route]
            [re-frame-ethers-demo.db :as db]))

(defn boot-flow
  []
  {:id :async/demo-flow
   :db-path [:boot-flow]
   :rules [{:when :seen?
            :events [[:notify :web3/init]]
            :dispatch-n '([:web3/load-chain-info] [:web3/load-instance])}
           {:when :seen?
            :events :web3/success-load-instance
            :dispatch [:boot/finalize :web3-init-success true]
            :halt? true}
           {:when :seen?
            :events :web3/fail-load-instance
            :dispatch [:boot/finalize]
            :halt? true}]})

(reg-event-fx
 :boot/finalize
 (fn [_ [_ web3-init-success]]
   {:fx [(when web3-init-success [:log "web3 init done"])
         [:log "boot process done"]]}))

(reg-event-fx
 :boot
 [(inject-cofx :web3/injected?)]
 (fn [{:keys [web3-injected?]} _]
   {
    ;:db (assoc db/default-db :active-panel :home-panel)
    :fx [[:log (if web3-injected?
                 "start boot process w/ web3..."
                 "start boot process w/o web3...")]
         ;; NOTE: MUST start router before navbar is loaded
         [::start-router-fx]
         [:async-flow (boot-flow)] ;;kick off async boot process
         (if web3-injected?
           [:dispatch [:notify :web3/init]]
           [:alert "Plz install metamask extension"])]}))

(reg-fx
 ::start-router-fx
 (fn [_]
   (route/init-routes!)))

(reg-event-fx
 ::start-router
 (fn [_ _]
   {::start-router-fx {}}))

