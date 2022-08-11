(ns re-frame-ethers-demo.effects
  (:require
   [reagent.core :as rg]
   [re-frame.core :refer [reg-fx reg-cofx dispatch]]
   [oops.core :refer [oget+ oapply+]]
   [re-frame-ethers-demo.utils :as utils]
   [re-frame-ethers-demo.cljs-ethers.utils :as ceutils]
   [re-frame-ethers-demo.components.web3.effects]))

(defn local-store-cofx-key
  [key]
  (keyword (str "local-store-" (name key))))

(def local-store-key-prefix "local-store-")

(defn local-store-key
  [key]
  (str local-store-key-prefix (name key)))

(defn >local-store
  "Puts data into localStorage under key"
  [key]
  (fn
    [data]
    (.setItem js/localStorage (local-store-key key) (str data))))

(reg-cofx
 :local-store
 (fn [cofx _key]
   (let [cofx-key (local-store-cofx-key _key)
         key (local-store-key _key)]
     ;; put the localstore data into the coeffect under :local-store-
     (assoc cofx cofx-key
            ;; read in from localstore, and process into a sorted map
            (into (hash-map)
                  (some->> (.getItem js/localStorage key)
                           (cljs.reader/read-string) ;; EDN map -> map
                           ))))))

(reg-fx
 :persist
 (fn [[key value]]
   (let [key (local-store-key key)]
     (if (some? value)
       (.setItem js/localStorage key (str value))
       ;; Specifying `nil` as value removes the key instead.
       (.removeItem js/localStorage key)))))

(reg-fx
 :log
 (fn [msg]
   (.log js/console msg)))

(reg-fx
 :alert
 (fn [msg]
   (js/alert msg)))

;;;; from https://ericnormand.me/guide/timeout-effect-in-re-frame
(defonce timeouts (rg/atom {}))

(reg-fx
 :timeout
 (fn [{:keys [id event duration]}]
   (when-some [existing (get @timeouts id)]
     (js/clearTimeout existing)
     (swap! timeouts dissoc id))
   (when (some? event)
     (swap! timeouts assoc id
            (js/setTimeout (fn []
                             (dispatch event))
                           duration)))))

