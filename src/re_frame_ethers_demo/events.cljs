(ns re-frame-ethers-demo.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path after]]
   [cljs.spec.alpha :as s]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [re-frame-ethers-demo.db :as db]
   [re-frame-ethers-demo.ethereum :as ethereum]
   [re-frame-ethers-demo.ethers-fx]
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

(def chain-interceptor [(path :chain)
                             (after db/chain-info->local-store)])

(def contract-interceptor [(path :contract)])

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
                                          :on-success [::update-chain-network "load-network"]
                                          :on-error [::log-error "Load network failed: "]}]}
                      :efx/watch-blocks {:provider web3-provider
                                         :event "block"
                                         :listener (fn [block-num] (utils/>evt [::update-chain-height (js->clj block-num)]))})
       (assoc effects ::utils/alert "Plz install metamask extension")))))

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
 chain-interceptor
 (fn [chain [_ from accounts]]
   (if (= accounts (:accounts chain))
     chain
     (assoc chain :accounts accounts))))

(reg-event-db
 ::update-chain-network
 chain-interceptor
 (fn [{:keys [id] :as chain} [_ from {:keys [name chain-id] :as network-info}]]
   (if (= id chain-id)
     chain
     (assoc chain :id chain-id
                  :name name))))

(reg-event-db
 ::update-chain-id
 chain-interceptor
 (fn [chain [_ chain-id]]
   (if (not= chain-id (:id chain))
     (assoc chain :id chain-id)
     chain)))

(reg-event-db
 ::update-chain-height
 chain-interceptor
 (fn [chain [_ height]]
   (assoc chain :height height)))

(reg-event-db
 ::update-contract-address
 contract-interceptor
 (fn [contract [_ address]]
   (assoc contract :address address)))

(reg-event-db
 ::update-contract-abi
 contract-interceptor
 (fn [contract [_ abi]]
   (assoc contract :abi abi)))

(reg-event-db
 ::update-contract-abi-filename
 contract-interceptor
 (fn [contract [_ name]]
   (assoc contract :abi-filename name)))

(reg-event-fx
 ::request-abi
 [(inject-cofx ::db/base-url)]
 (fn [{:keys [base-url]} [_ address]]
   {:http-xhrio {:method          :get
                 :uri             (str base-url address)
                 :timeout         8000 ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true}) ;; IMPORTANT!: You must provide this.
                 :on-success      [::request-abi-success]
                 :on-failure      [::request-abi-fail]}}))

(reg-event-db 
 ::request-abi-success
 (fn [db [_ {:keys [result] :as res}]]
   (assoc-in db [:contract :abi] (js->clj (js/JSON.parse result)))))

;; TODO: a general fx?
(reg-event-fx
 ::request-abi-fail
 (fn [cofx [_ {:keys [failure status] :as error}]]
   {:db (assoc (:db cofx) :error-msg failure)
    ::utils/timeout {:id :request-abi-error-msg
                     :event [::remote-error-msg]
                     :duration timeout-duration}}))

(reg-event-fx
 ::pop-error-msg
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
