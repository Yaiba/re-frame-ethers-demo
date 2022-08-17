(ns re-frame-ethers-demo.components.web3.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx path after inject-cofx]]
            [re-frame-ethers-demo.db :as db]
            [re-frame-ethers-demo.effects :refer [>local-store]]
            [re-frame-ethers-demo.ethereum :as ethereum]
            [re-frame-ethers-demo.interceptors :refer [icheck]]
            [re-frame-ethers-demo.utils :refer [>evt chain-id-name]]))

(def chain-interceptor [icheck
                        (path [:web3 :chain])
                        (after (>local-store :chain))])

(reg-event-db
 :web3/update-chain-accounts
 chain-interceptor
 (fn [chain [_ from accounts]]
   (if (= accounts (:accounts chain))
     chain
     (assoc chain :accounts accounts))))

(reg-event-db
 :web3/update-chain-network
 chain-interceptor
 (fn [{:keys [id] :as chain} [_ from {:keys [name chain-id] :as network-info}]]
   (if (= id chain-id)
     chain
     (assoc chain :id chain-id
                  :name name))))

(reg-event-db
 :web3/update-chain-id
 chain-interceptor
 (fn [chain [_ chain-id]]
   (if (not= chain-id (:id chain))
     (assoc chain :id chain-id)
     chain)))

(reg-event-db
 :web3/update-chain-height
 chain-interceptor
 (fn [chain [_ height]]
   (assoc chain :height height)))

(reg-event-db
 :web3/update-etherscan-token
 chain-interceptor
 (fn [chain [_ token]]
   (assoc chain :token token)))

(reg-event-fx
 :web3/updated
 (fn [{:keys [db]} [_ reason]]
   {:db (update-in db [:web3-updated] (fnil inc 0))
    :log (str "web3 updated: " reason)}))

(defn listen-mm-events
  []
  (.on ethereum/Ethereum "chainChanged"
       (fn [chain-id]
         (let [chain-id (int chain-id)]
           (>evt [:web3/updated "chain changed"])
           (if (contains? chain-id-name chain-id)
             (>evt [:web3/update-chain-id chain-id])
             (>evt [:alert "Network not support"]))
           (.reload (.. js/window -location)))))
  (.on ethereum/Ethereum "connect"
       (fn [&args] (>evt [:web3/updated "connect"])))
  (.on ethereum/Ethereum "disconnect"
       (fn [&args] (>evt [:web3/updated "disconnected"])))
  (.on ethereum/Ethereum "accountsChanged"
       (fn [accounts]
         (>evt [:web3/updated "account changed"])
         (if (empty? accounts )
           (>evt [:alert "Plz connect to metamask."])
           (>evt [:web3/update-chain-accounts "change-accounts" accounts])))))

(reg-event-fx
 :web3/connect-wallet
 [(inject-cofx :web3/injected?)]
 (fn [{:keys [db web3-injected?]} _]
   (if web3-injected?
     ;; TODO pure
     {:efx/request-accounts {:provider (get-in db [:web3 :provider])
                             :on-success [:web3/update-chain-accounts "request-accounts"]
                             :on-error [:log-error "Request accounts failed: "]}}
     {:alert "Plz install metamask extension"})))

(reg-event-fx
 :web3/load-chain-info
 [(inject-cofx :local-store :chain)]
 (fn [{:keys [local-store-chain] :as cofx} _]
   (let [chain-info (merge
                     (get-in db/default-db [:web3 :chain])
                     local-store-chain)
         db (-> db/default-db
                (assoc-in [:web3 :chain] chain-info))]
     {:db db})))

(reg-event-fx
 :web3/load-instance
 [(inject-cofx :web3/instance)]
 (fn [{:keys [provider signer db]} _]
   (merge
    {:db db}
    (if provider
      {:db (-> db
               (assoc-in [:web3 :provider] provider)
               (assoc-in [:web3 :signer] signer))
       :fx [[:efx/load-accounts {:provider provider
                                :on-success [:web3/update-chain-accounts "load-accounts"]
                                :on-error [:log-error "Load web3 accounts failed: "]}]
            [:efx/p-call {:provider provider
                          :fns [{:fn :get-network
                                 :on-success [:web3/update-chain-network "load-network"]
                                 :on-error [:log-error "Load web3 network failed: "]}]}]
            ;; :efx/watch-blocks {:provider web3-provider
            ;;                    ;;:event "block"
            ;;                    :listener (fn [block-num] (utils/>evt [::update-chain-height (js->clj block-num)]))}
            [:dispatch [:notify :web3/success-load-instance]]]}
      {:dispatch [:notify :web3/fail-load-instance]}))))
