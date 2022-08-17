(ns re-frame-ethers-demo.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path after]]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [re-frame-ethers-demo.effects]
   [re-frame-ethers-demo.db :as db]
   [re-frame-ethers-demo.route :as route]
   [re-frame-ethers-demo.cofx :as cofx]
   [re-frame-ethers-demo.ethereum :as ethereum]
   [re-frame-ethers-demo.ethers-fx.core]
   [re-frame-ethers-demo.utils :as utils]
   [re-frame-ethers-demo.cljs-ethers.utils :as eutils]
   [re-frame-ethers-demo.events.boot]
   [re-frame-ethers-demo.components.web3.events]
   [re-frame-ethers-demo.components.navbar.events]
   [re-frame-ethers-demo.pages.contract.events]
   [re-frame-ethers-demo.effects]
))

;; misc 
(reg-event-fx
 :notify
 (fn [cofx [_ args]]
   ;;no op handler
   ))

(reg-event-fx
 :log-error
 (fn [cofx [_ desc err]]
   (let [msg (str desc (.-message err))]
     {:log msg})))

(reg-event-fx
 :log
 (fn [cofx [_ msg]]
   {:log msg}))

(reg-event-fx
 :alert
 (fn [cofx [_ msg]]
   {:alert msg}))


(def msg-timeout-duration 3000)
(reg-event-fx
 :pop-error-msg
 (fn [cofx [_ msg]]
   {:db (assoc (:db cofx) :error-msg msg)
    :timeout {:id :error-msg
              :event [:remove-error-msg]
              :duration msg-timeout-duration}}))

(reg-event-db
 :remove-error-msg
 (fn [db _]
   (dissoc db :error-msg)))
