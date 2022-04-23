(ns re-frame-ethers-demo.utils
    (:require
     [clojure.string :as string]
     [oops.core :refer [oget oset! ocall oget+ ocall+]]
     [reagent.core :as rg]
     [re-frame.core :refer [subscribe dispatch reg-fx]]
     [camel-snake-kebab.extras :refer [transform-keys]]
     [camel-snake-kebab.core :as csk]))

(def <sub (comp deref subscribe))
(def >evt dispatch)

(defn js-event-val
  [e]
  (some-> e .-target .-value))

(def chain-id-name
  {1 "mainnet"
   3 "ropsten"
   4 "rinkeby"
   56 "bsc"
   137 "polygon"
   100 "gnosis"})

;; https://api.etherscan.io/api?module=contract&action=getabi&address=0x49cf6f5d44e70224e2e23fdcdd2c053f30ada28b

;;;;;;;;;;; directly borrowed from cljs-web3
(defn safe-case [case-f]
  (fn [x]
    (cond-> (subs (name x) 1)
      true (string/replace "_" "*")
      true case-f
      true (string/replace "*" "_")
      true (->> (str (first (name x))))
      (keyword? x) keyword)))

(def camel-case (safe-case csk/->camelCase))
(def kebab-case (safe-case csk/->kebab-case))

(def js->cljk #(js->clj % :keywordize-keys true))

(def js->cljkk
  "From JavaScript to Clojure with kekab-cased keywords."
  (comp (partial transform-keys kebab-case) js->cljk))

(def cljkk->js
  "From Clojure with kebab-cased keywords to JavaScript."
  (comp clj->js (partial transform-keys camel-case)))
;;;;;;;;;;; done

;; some modify
(defn callback-js->clj [x]
  (if (fn? x)
    (fn [err res]
      (when (and res (oget res "v"))
        (oset! res "v" (oget res "v")))                      ;; Prevent weird bug in advanced optimisations
      (x err (js->cljkk res)))
    x))

(defn args-callbacks-cljkk->js [args]
  (map (comp cljkk->js callback-js->clj) args))

(defn js-apply-with-callback
  ([this method-name]
   (js-apply-with-callback this method-name nil))
  ([this method-name args]
   (let [method-name (camel-case (name method-name))]
     (if (oget+ this method-name)
       (js->cljkk (ocall+ this method-name (args-callbacks-cljkk->js args)))
       (throw (str "Method: " method-name " was not found in object."))))))

(defn args-cljkk->js
  [args]
  (map cljkk->js args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn promise-dispatch
  [handler val]
  (when handler
    (>evt (conj handler val))))

(defn promise-action
  [event]
  (fn [v]
    (promise-dispatch event (js->cljkk v))))

(defn promise-fx-handler
  [{:keys [call on-success on-error]}]
  (-> (call)
      (.then (promise-action on-success))
      (.catch (promise-action on-error))
      (.finally true)))

(reg-fx
 ::promise promise-fx-handler)

(reg-fx
 ::promise-n
 (fn [ps]
   (doseq [p ps]
     (promise-fx-handler p))))

(reg-fx
 ::log
 (fn [msg]
   (.log js/console msg)))

(reg-fx
 ::alert
 (fn [msg]
   (js/alert msg)))


;;;; from https://ericnormand.me/guide/timeout-effect-in-re-frame
(defonce timeouts (rg/atom {}))

(reg-fx
 ::timeout
 (fn [{:keys [id event duration]}]
   (when-some [existing (get @timeouts id)]
     (js/clearTimeout existing)
     (swap! timeouts dissoc id))
   (when (some? event)
     (swap! timeouts assoc id
            (js/setTimeout (fn []
                             (dispatch event))
                           duration)))))
;;;;

;;;; from https://ericnormand.me/guide/state-in-re-frame
(comment (defonce window-size
           (let [a (rg/atom {:width  (.-innerWidth  js/window)
                            :height (.-innerHeight js/window)})]
             (.addEventListener js/window "resize"
                                (fn [] (reset! a {:width  (.-innerWidth  js/window)
                                                  :height (.-innerHeight js/window)})))
             a)))
;;;;
