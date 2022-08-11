(ns re-frame-ethers-demo.pages.contract.views
  (:require
   [cljs.spec.alpha :as s]
   [clojure.string :as string]
   [reagent.core :as rg]
   [re-frame.core :refer [subscribe dispatch]]
   [cljs.core.async :refer [<! >! put! chan go go-loop] :as a]
   [cljs.core.async.interop :refer-macros [<p!]]
   [oops.core :refer [oget]]
   [re-frame-ethers-demo.pages.contract.events]
   [re-frame-ethers-demo.pages.contract.subs]
   [re-frame-ethers-demo.db :as db]
   [re-frame-ethers-demo.utils :refer [<sub >evt chain-id-name] :as utils]))


(def first-file
  "Accepts inpOBut change events and gets the first selected file."
  (map (fn [e]
         (let [target (.-currentTarget e)
               file (-> target .-files (aget 0))]
           (set! (.-value target) "")
           file))))

(def extract-result
  "Accepts a FileReader onload event, gets the parsed result."
  (map #(-> % .-target .-result)))

(def upload-reqs (chan 1 first-file))
(def file-reads (chan 1 extract-result))

(defn put-upload! [e]
  (put! upload-reqs e))

;; handle new file
(go-loop []
       (let [reader (js/FileReader.)
             file (<! upload-reqs)]
         (>evt [:contract/update-abi-filename (.-name file)])
         (set! (.-onload reader) #(put! file-reads %))
         (.readAsText reader file))
       (recur))

;; handle new file data
(go-loop []
  (let [contract-json (<! file-reads)
        contract-info (js/JSON.parse contract-json)
        abi-json (js/JSON.stringify (oget contract-info "abi"))]
    (>evt [:contract/update-abi abi-json])
    (>evt [:contract/clear-state])
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

(defn func-input
  [read? func-idx inputs]
  (let [fname (if read?
                (str "read-" func-idx)
                (str "write-" func-idx))
        arg-num (count inputs)
        arg-idxs (into
                  []
                  (take arg-num (range)))
        fields (map
                (fn
                  [{:keys [name type]} arg-idx]
                  (let [param-id (str fname "-input-" arg-idx)]
                    ^{:key param-id}
                    [:div.func-input
                     [:label {:for param-id} name]
                     [:input {:id param-id
                              :type "text"
                              :placeholder type
                              :value (<sub [:contract/fn-input-arg read? func-idx arg-idx])
                              :on-change (fn [e]
                                           (>evt [:contract/update-fn-input-args
                                                  read?
                                                  func-idx
                                                  arg-num
                                                  arg-idx
                                                  type
                                                  (.. e -target -value)]))}]]))
                inputs arg-idxs)]
    (into [:<>] fields)))

(defn func-output
  [read? func-idx outputs]
  (let [fname (if read?
                (str "read-" func-idx)
                (str "write-" func-idx))
        output-types (map #(get % "type") outputs)
        output-idxs (into [] (take (count outputs) (range)))
        fn-outputs (<sub [:contract/fn-outputs fname output-types])]
    (into [:<>]
          (map (fn [{:keys [name type]} output-idx fn-output]
                 ^{:key (str fname "output" output-idx)}
                 [:p
                  (str "👉 " type " => "fn-output)])
               outputs output-idxs fn-outputs))))

(defn func-btn
  [read? func-idx func-name]
  (let [instance (if read?
                   (<sub [:contract/read-instance])
                   (<sub [:contract/write-instance]))
        fname (if read?
                (str "read-" func-idx)
                (str "write-" func-idx))
        tx-error (<sub [:contract/fn-call-error fname])]
    [:div
     [:input {:type "button"
              :value (if read? "query" "write")
              :on-click (fn [e]
                          (let [fn-args (<sub [:contract/fn-input-args read? func-idx])]
                            (js/console.log "call " (str func-name "(" fn-args ")"))
                            (>evt [:contract/c-call instance func-name fname fn-args])))}]
     (if (nil? tx-error)
       ""
       [:p {:style {:color "red"}} tx-error])]))

(defn contract-func
  [read? func-idx]
  (let [funcs (if read?
                (<sub [:contract/abi-read-funcs func-idx])
                (<sub [:contract/abi-write-funcs func-idx]))
        {:keys [name inputs outputs]} funcs]
    ^{:key (if read?
             (str "read-" func-idx)
             (str "write-" func-idx))}
    [:li.func
     [:div
      [:h3 name]
      [func-input read? func-idx inputs]
      [func-output read? func-idx outputs]
      [func-btn read? func-idx name]
      ]]))

(defn contract-funcs
  [read?]
  (let [func-num (if read?
                   (<sub [:contract/abi-read-func-num])
                   (<sub [:contract/abi-write-func-num]))] 
    [:div {:style {:border-style "ridge"}}
     [:h2 (if read? "read contract" "write contract")]
     (into [:ul]
           (map
            (fn
              [func-idx]
              [contract-func read? func-idx])
            (range func-num)))]))

(defn contract
  []
  [:div
   [:ul
    [:li [contract-funcs true]]
    [:li [contract-funcs false]]
    ]])

(defn contract-address
  []
  (let [addr (<sub [:contract/address])]
    [:p "contract address: "
     [:input {:type "text"
              :value addr
              :on-change (fn [e]
                         (>evt [:contract/update-address
                                (utils/js-event-val e)]))}]]))

(defn upload-abi
  []
  (let [filename (<sub [:contract/abi-filename])]
    [:div
     (if (empty? filename)
         [:label {:for "abifile"} "Load abi from local:   "]
         filename)
     [:input {:type "file"
              :name "abifile"
              :id "abifile"
              :accept ".json"
              :on-change put-upload!
              }]]))

(defn request-etherscan-abi
  []
  (let [addr (<sub [:contract/address])]
    [:div
     "OR"
     [:br]
     " 🤔🤔 provide apitoken"
     [:input
      {:type "text"
       :value (<sub [:chain/etherscan-token])
       :placeholder "API TOKEN"
       :on-change (fn [e]
                    (>evt [:web3/update-etherscan-token (.. e -target -value)]))}]
     [:button
      {:on-click (fn [e]
                   (if (s/valid? ::db/eth-addr addr)
                     (>evt [:contract/get-etherscan-abi (<sub [:chain/etherscan-token]) addr])
                     (>evt [:pop-error-msg "contract address is NOT VALID"])))}
      "load abi from etherscan"]

]))

(defn main
  []
  [:div
   [contract-address]       
   [upload-abi]
   [request-etherscan-abi]
   [:hr]
   [contract]])
