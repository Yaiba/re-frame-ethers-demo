(ns re-frame-ethers-demo.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path after]]
   [re-frame-ethers-demo.db :as db]
   [re-frame-ethers-demo.ethereum :as ethereum]
   [re-frame-ethers-demo.ethers-fx]
   [cljs.spec.alpha :as s]
   [re-frame-ethers-demo.utils :as utils]
))

(def timeout-duration 3000)

(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (do
      (js/console.log (s/explain-str a-spec db))
      (throw (ex-info (str "spec check failed: "
                           (s/explain-str a-spec db)) {})))))

(def check-spec-interceptor
  (after (partial check-and-throw :re-frame-ethers-demo.db/db)))

(def ethers-interceptors [check-spec-interceptor])

(def chain-info-interceptor [(path :chain)
                             (after db/chain-info->local-store)])

(reg-event-fx
 ::initialize-db
 [(inject-cofx ::db/eth-injected)
  (inject-cofx ::db/ethers-info)
  (inject-cofx ::db/local-store-chain-info)]
 (fn [{:keys [eth-injected
              web3-provider web3-signer
              local-store-chain-info]} _]
   (let [ethers-info {:provider web3-provider
                      :signer web3-signer}
         chain-info (merge (:chain db/default-db) local-store-chain-info)
         effects {:db (assoc db/default-db :ethers ethers-info
                                           :chain chain-info)}]
     (if eth-injected
       (assoc effects :efx/load-accounts {:provider web3-provider
                                                       :on-success [::update-chain-accounts "load-accounts"]
                                                       :on-error [::log-error "Load accounts failed: "]}
                      :efx/p-call {:provider web3-provider
                                   :fns [{:fn :get-network
                                          ;;:args []
                                          :on-success [::update-chain-network "load-network"]
                                          :on-error [::log-error "Load network failed: "]}]}
                      :efx/watch-blocks {:provider web3-provider
                                         :event "block"
                                         :listener (fn [block-num] (js/console.log "got new block" block-num) (utils/>evt [::update-chain-height (js->clj block-num)]))})
       (assoc effects ::utils/alert "Plz install metamask extension")))))

(comment
  :efx/watch-blocks {:provider web3-provider
                     :event "block"
                     :listener #(utils/>evt [::update-chain-height (utils/js->cljkk %)])})
(reg-event-fx
 ::connect-wallet
 [(inject-cofx ::db/eth-injected)]
 (fn [{:keys [db eth-injected]} _]
   (if eth-injected
     {:efx/request-accounts {:provider (get-in db [:ethers :provider])
                                :on-success [::update-chain-accounts "request-accounts"]
                                :on-error [::log-error "Request accounts failed: "]}}
     {::utils/alert "Plz install metamask extension"})))

(reg-event-db
 ::update-chain-accounts
 chain-info-interceptor
 (fn [chain [_ from accounts]]
   (if (= accounts (:accounts chain))
     chain
     (assoc chain :accounts accounts))))

(reg-event-db
 ::update-chain-network
 chain-info-interceptor
 (fn [{:keys [id] :as chain} [_ from {:keys [name chain-id] :as network-info}]]
   (if (= id chain-id)
     chain
     (assoc chain :id chain-id
                  :name name))))

(reg-event-db
 ::update-chain-id
 chain-info-interceptor
 (fn [chain [_ chain-id]]
   (if (not= chain-id (:id chain))
     (assoc chain :id chain-id)
     chain)))

(reg-event-db
 ::update-chain-height
 chain-info-interceptor
 (fn [chain [_ height]]
   (assoc chain :height height)))

(reg-event-fx
 ::set-error-msg
 (fn [cofx [_ msg]]
   {:db (assoc (:db cofx) :error-msg msg)
    ::utils/timeout {:id :error-msg
                     :event [::remove-error-msg]
                     :duration timeout-duration}}))

(reg-event-db
 ::remove-error-msg
 (fn [db _]
   (dissoc db :error-msg)))

(reg-event-fx
 ::log-error
 (fn [cofx [_ desc err]]
   (let [msg (str desc (.-message err))]
     {::utils/log msg})))

(reg-event-fx
 ::log
 (fn [cofx [_ msg]]
   {::utils/log msg}))

(reg-event-fx
 ::alert
 (fn [cofx [_ msg]]
   {::utils/alert msg}))

(reg-event-fx
 ::web3-updated
 (fn [{:keys [db]} [_ reason]]
   {:db (update-in db [:web3-updated] (fnil inc 0))
    ::utils/log (str "web3 updated: " reason)}))

(comment (reg-event-fx
          ::watch-block
          (fn [{:keys [db]} _]
            {:efx/p-on-event {:provider (get-in db [:ethers :provider])
                              :event "block"
                              :listener #(utils/>evt [::update-chain-height (utils/js->cljkk %)])}})))



(.on ethereum/Ethereum "chainChanged"
     (fn [chain-id]
       (let [chain-id (int chain-id)]
         (if (contains? utils/chain-id-name chain-id)
           (utils/>evt [::update-chain-id chain-id])
           (utils/>evt [::alert "Network not support"]))
         (.reload (.. js/window -location)))))

(.on ethereum/Ethereum "connect"
     (fn [&args] (utils/>evt [::web3-updated "connected"])))

(.on ethereum/Ethereum "disconnect"
     (fn [&args] (utils/>evt [::web3-updated "disconnected"])))

(.on ethereum/Ethereum "accountsChanged"
     (fn [accounts]
       (if (empty? accounts )
         (utils/>evt [::alert "Plz connect to metamask."])
         (utils/>evt [::update-chain-accounts "change-accunts" accounts]))))
