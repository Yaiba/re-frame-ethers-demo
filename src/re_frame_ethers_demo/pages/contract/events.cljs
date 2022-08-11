(ns re-frame-ethers-demo.pages.contract.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path]]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [re-frame-ethers-demo.db :as db]
   [re-frame-ethers-demo.ethereum :as ethereum]
   [re-frame-ethers-demo.ethers-fx.core]
   [re-frame-ethers-demo.pages.contract.utils :refer [func-input-converter]]
   [re-frame-ethers-demo.cljs-ethers.utils :as ceutils]))

(def timeout-duration 3000)
(def contract-interceptor [(path :contract)])
(def state-interceptor [(path [:contract :state])])

(reg-event-db
 :contract/clear-state
 contract-interceptor
 (fn [contract [_]]
   (assoc contract :state {})))

(reg-event-db
 :contract/update-address
 contract-interceptor
 (fn [contract [_ address]]
   (assoc contract :address address)))

(reg-event-db
 :contract/update-abi
 contract-interceptor
 (fn [contract [_ abi]]
   (-> contract
       (assoc :abi-raw abi)
       (assoc :abi (ceutils/js->cljkk (js/JSON.parse abi))))))

(reg-event-db
 :contract/update-abi-filename
 contract-interceptor
 (fn [contract [_ name]]
   (assoc contract :abi-filename name)))

(defn get-etherscan-url
  [token address]
  (str "https://api.etherscan.io/api?module=contract&action=getabi&apikey=" token
       "&address=" address))

(reg-event-fx
 :contract/get-etherscan-abi
 ;[(inject-cofx :web3/etherscan-url)]
 (fn [_ [_ token address]]
   {:http-xhrio {:method          :get
                 ;:uri             (str etherscan-url address)
                 :uri             (get-etherscan-url token address)
                 :timeout         8000 ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true}) ;; IMPORTANT!: You must provide this.
                 :on-success      [:contract/get-etherscan-abi-success]
                 :on-failure      [:contract/get-etherscan-abi-fail]}}))

(reg-event-fx 
 :contract/get-etherscan-abi-success
 (fn [cofx [_ {:keys [status result] :as res}]]
   (if (= status "0")
     ;; not ok
     {:dispatch [:contract/get-etherscan-abi-notok res]}
     {:db (-> (:db cofx)
              (assoc-in [:contract :abi-raw] result)
              (assoc-in [:contract :abi] (ceutils/js->cljkk (js/JSON.parse result))))
      :dispatch [:contract/clear-state]})))

(reg-event-fx
 :contract/get-etherscan-abi-notok
 (fn [cofx [_ {:keys [message result] :as res}]]
   {:db (assoc (:db cofx) :error-msg result)
    :timeout {:id :etherscan-abi-notok-msg
              :event [:remove-error-msg]
              :duration timeout-duration}}))

;; TODO: a general fx?
(reg-event-fx
 :contract/get-etherscan-abi-fail
 (fn [cofx [_ {:keys [failure status] :as error}]]
   {:db (assoc (:db cofx) :error-msg failure)
    :timeout {:id :etherscan-abi-fail-msg
              :event [:remove-error-msg]
              :duration timeout-duration}}))

(reg-event-fx
 :contract/c-call
 (fn [cofx [_ instance func-name fname tx-inputs]]
   {:efx/c-call {:instance instance
                 :fns [{:fn func-name
                        :args tx-inputs
                        :on-success [:contract/success-c-call func-name fname]
                        :on-error [:contract/fail-c-call func-name fname]}]}}))

(reg-event-fx
 :contract/success-c-call
 (fn [cofx [_ func-name fname result]]
   (let [res (if (seq? result)
               result
               [result])]
     {:db (assoc-in (:db cofx) [:contract :state :tx-outputs fname] res)})))

(reg-event-fx
 :contract/fail-c-call
 (fn [cofx [_ func-name fname {message :message data :data :as error}]]
   (let [msg (if (contains? data :message)
               (:message data)
               message)]
     {:db (assoc (:db cofx) :error-msg msg) 
      :timeout {:id :c-call-error-msg
                :event [:remove-error-msg]
                :duration timeout-duration}
      :dispatch [:contract/update-fn-call-error fname msg]})))

(reg-event-db
 :contract/remove-fn-call-error
 state-interceptor
 (fn [state [_ fname]]
   (update-in state [:fn-call-error] dissoc fname)))

(reg-event-fx
 :contract/update-fn-call-error
 (fn [cofx [_ fname error]]
   {:db (assoc-in (:db cofx) [:contract :state :fn-call-error fname] error)
    :timeout {:id :fn-call-error
              :event [:contract/remove-fn-call-error fname]
              :duration timeout-duration}}))

(reg-event-db
 :contract/update-fn-input-args
 state-interceptor
 (fn [state [_ read? func-idx arg-num arg-idx type value]]
   (let [k (str (if read? "read-" "write-") func-idx)
         v ((func-input-converter type) value)
         _args (get-in state [:fn-input-args k] (into [] (take arg-num (repeat nil))))
         args (assoc _args arg-idx v)]
     (assoc-in state [:fn-input-args k] args))))
