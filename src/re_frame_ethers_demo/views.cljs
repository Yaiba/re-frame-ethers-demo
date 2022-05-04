(ns re-frame-ethers-demo.views
  (:require
   [cljs.spec.alpha :as s]
   [clojure.string :as string]
   [reagent.core :as rg]
   [re-frame.core :refer [subscribe dispatch]]
   [re-frame-ethers-demo.subs :as subs]
   [cljs.core.async :refer [<! >! put! chan go go-loop] :as a]
   [cljs.core.async.interop :refer-macros [<p!]]
   [re-frame-ethers-demo.events :as events]
   [re-frame-ethers-demo.db :as db]
   [re-frame-ethers-demo.utils :refer [<sub >evt chain-id-name] :as utils]
   ))



(defn connect-wallet-btn
  []
  (let [accounts (subscribe [::subs/chain-accounts])]
    (fn []
      (let [connected (empty? @accounts)]
        [:button
         {:on-click  (fn [e]
                       (if connected
                         (>evt [::events/connect-wallet])
                         (js/console.log "wallet already connected")))}
         (if connected "Connect Wallet" (first @accounts))]))))

(defn chain-height
  []
  (let [chain-height (<sub [::subs/chain-height])]
    [:p {:style {:color "pink"}}
     "Block height: " chain-height]))

(defn chain-id
  []
  (let [chain-id (<sub [::subs/chain-id])]
    [:p {:style {:color "pink"}}
     (str (get chain-id-name chain-id "not support") " Chain(" chain-id ")")]))

(defn header
  []
  [:div
   [connect-wallet-btn]
   [chain-height]
   [chain-id]
   ])

(defn err-box
  []
  (let [msg (<sub [::subs/error-msg])
        show (boolean msg)]
    [:p {:style {:display (if show "block" "none")
                 :color "red"}}
     msg]))


;; (defn gen-key! []
;;   (let [next (swap! uniq-key inc)]
;;     next))


(def first-file
  "Accepts inpOBut change events and gets the first selected file."
  (map (fn [e]
         (let [target (.-currentTarget e)
               file (-> target .-files (aget 0))]
           (set! (.-value target) "")
           file))))

(def extract-result
  "Accepts a FileReader onload event, gets the parsed result."
  (map #(-> % .-target .-result js/JSON.parse js->clj)))

(def upload-reqs (chan 1 first-file))
(def file-reads (chan 1 extract-result))

(defn put-upload! [e]
  (put! upload-reqs e))

;; handle new file
(go-loop []
       (let [reader (js/FileReader.)
             file (<! upload-reqs)]
         ;;(swap! app-state assoc :abi-file-name (.-name file))
         (>evt [::events/update-contract-abi-filename (.-name file)])
         (set! (.-onload reader) #(put! file-reads %))
         (.readAsText reader file))
       (recur))

;; handle new file data
(go-loop []
  (let [abi-data (.get (<! file-reads) "abi")
        ;;contract-addr (:contract-addr @app-state)
        ;;provider (:provider @w3)
        ;;contract (ethers/get-contract contract-addr abi-data provider)
        ]
    ;;(swap! app-state assoc :abi-data abi-data :contract contract)
    (>evt [::events/update-contract-abi abi-data])
    (recur)))

(comment (defn upload-btn [abi-file-name]
           [:div
            (or abi-file-name "upload abi : ")
            [:input {:type "file"
                     :name "inputfile"
                     :id "inputfile"
                     :accept ".json"
                     :on-change put-upload!
                     }]
            (when abi-file-name
              [:input {:type "button"
                       :style {:background-color "pink"}
                       :on-click #((reset! app-state default-app-state)
                                   (reset! uniq-key 0))
                       :value "reset"}])]))

;; (defn read-func? [meta]
;;   (let [{ftype "type"
;;          visible "stateMutability"} meta]
;;     (and (= ftype "function")
;;          (contains? #{"view" "pure"} visible))))

;; (defn write-func? [meta]
;;   (let [{ftype "type"
;;          visible "stateMutability"} meta]
;;     (and (= ftype "function")
;;          (not (contains? #{"view" "pure"} visible)))))

;; (defn input-converter [itype]
;;   (case itype
;;     "address" identity
;;     "uint256" js/parseInt
;;     "uint128" js/parseInt
;;     "bool" #(if (= % "true") true false)
;;     str))

;; (defn output-converter [itype]
;;   (case itype
;;     "address" identity
;;     "uint256" #(.toString %)
;;     "unit128" #(.toString %)
;;     "bool" #(if (= % "true") true false)
;;     str))

;; (defn tx-input-component
;;   [fname inputs outputs]
;;   [:<>
;;    (let [tx-inputs (:tx-inputs @app-state)]
;;      (doall
;;       (for [input inputs
;;             :let [{iname "name"
;;                    itype "type"} input
;;                   ;;_ (js/console.log "input field: " iname "/" itype "/")
;;                   ifield (str iname "(" itype ")")]]
;;         [:span {:key ifield}
;;          [:label {:for (str fname ifield "text")} ifield]
;;          [:input {:id (str fname ifield "text")
;;                   :type "text"
;;                   :placeholder ifield
;;                   :value (get-in tx-inputs [fname iname])
;;                   :on-change (fn [e]
;;                                (let [cvt (input-converter itype)
;;                                      new-value (cvt (.. e -target -value))]
;;                                  (swap! app-state assoc-in [:tx-inputs fname iname] new-value)))}]])))
;;    (doall
;;     (for [output outputs]
;;       (let [{otype "type"} output
;;             ;;_ (js/console.log "output field: " output)
;;             ;;_ (js/console.log "output type: " otype)
;;             ]
;;         [:p {:key (gen-key!)}
;;          (str "|_ " otype)])))])

;; (defn tx-btn-component
;;   [btn-name view? fname inputs outputs]
;;   (let [tx-inputs (:tx-inputs @app-state)]
;;     [:input {:type "button"
;;              :value btn-name
;;              :on-click (fn [e]
;;                          (go
;;                            (let [_contract (:contract @app-state)
;;                                  contract (if view?
;;                                             _contract
;;                                             (.connect _contract (:signer @w3)))
;;                                  tx-input-map (get tx-inputs fname)
;;                                  input-args-order (map #(get % "name") inputs)
;;                                  tx-input (if (zero? (count input-args-order))
;;                                             [nil]
;;                                             (map #(get tx-input-map %) input-args-order))
;;                                  tx-result (<p! (ethers/call contract fname tx-input))
;;                                  tx-results (if (seq? tx-result) tx-result [tx-result])
;;                                  output-types (map #(get % "type") outputs)
;;                                  cvt-result (#(for [otype %1
;;                                                     output %2]
;;                                                 ((output-converter otype) output)) output-types tx-results)]
;;                              (js/console.log "input args order : " input-args-order)
;;                              (js/console.log "input args : " tx-input-map)
;;                              (js/console.log "converted input : " tx-input)
;;                              (js/console.log "tx results : " tx-results)
;;                              (js/console.log "converted result : " cvt-result)
;;                              (swap! app-state assoc-in [:tx-result fname] tx-results)
;;                              (swap! app-state assoc-in [:cvt-result fname] cvt-result)
;;                              )))}]))

;; (defn tx-output-component
;;   [fname outputs]
;;   [:ul
;;    (let [tx-inputs (:tx-inputs @app-state)
;;          tx-result (get-in @app-state [:tx-result fname])
;;          cvt-result (get-in @app-state [:cvt-result fname])
;;          ]
;;      (if (empty? outputs)
;;        [:samp (str (js->clj tx-result))]
;;        (for [res cvt-result
;;              {otype "type"} outputs]
;;          ^{:key (gen-key!)}
;;          [:li
;;           [:p (str otype ": " res)]])))])


;; (defn function [meta view?]
;;   (let [{fname "name"
;;          inputs "inputs"
;;          outputs "outputs"} meta
;;         ;;_ (js/console.log "render func: " fname "/" inputs)
;;         btn-name (if view? "query" "write")]
;;     [:li
;;      [:p [:b fname]]
;;      [tx-input-component fname inputs outputs]
;;      [tx-output-component fname outputs]
;;      [tx-btn-component btn-name view? fname inputs outputs]]))

;; (defn contract-funcs [funcs view?]
;;   [:div {:style {:border-style "ridge"}}
;;    [:b (if view? "read contract" "write contract")]
;;    [:ul
;;       (for [func funcs]
;;         ^{:key (gen-key!)}
;;         [function func view?])]])

;; (defn result [metas]
;;   (let [read-funcs (filter read-func? metas)
;;         write-funcs (filter write-func? metas)]
;;     [:<>
;;      [contract-funcs read-funcs true]
;;      [:br]
;;      [contract-funcs write-funcs false]]))

(defn contract-address
  []
  (let [addr (<sub [::subs/contract-address])]
    [:p "address: "
     [:input {:type "text"
              :value addr
              :on-change (fn [e]
                         (>evt [::events/update-contract-address
                                (utils/js-event-val e)]))}]]))

(defn upload-abi []
  (let [filename (<sub [::subs/contract-abi-filename])]
    [:div
     (if (empty? filename)
         [:label {:for "abifile"} "Upload abi: "]
         filename)
     [:input {:type "file"
              :name "abifile"
              :id "abifile"
              :accept ".json"
              :on-change put-upload!
              }]]))

(comment (when filename
                [:input {:type "button"
                         :style {:background-color "pink"}
                         :on-click #((reset! state {})
                                     ;;(reset! uniq-key 0)
                                     )
                         :value "reset"}]))

(defn request-abi
  []
  (let [addr (<sub [::subs/contract-address])]
    [:button
     {:on-click (fn [e]
                  (if (s/valid? ::db/eth-addr addr)
                    (>evt [::events/request-abi addr])
                    (>evt [::events/pop-error-msg "contract address is not set"])))}
     "load abi from etherscan"]))

(defn contract-area
  []
  (let [local-state (rg/atom {})
        provider (subscribe [::subs/ethers-provider])
        signer (subscribe [::subs/ethers-signer])]
    (fn []
      [:div
       [contract-address]       
       [upload-abi]
       [request-abi]
       
       ;;[result (:abi-data @app-state)]   ])
       ])))


(defn main-panel
  []
  [:div
   [err-box]
   [header]
   [:h2 "re-frame & ethers"]

   [contract-area]
   ])
