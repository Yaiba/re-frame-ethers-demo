(ns re-frame-ethers-demo.route
  (:require [re-frame.core :refer [subscribe dispatch reg-sub reg-event-db reg-event-fx reg-fx]]
            [oops.core :refer [ocall]]
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.coercion.spec :as rcs]
            [reitit.frontend.easy :as rfe]))

;; events ;;
(reg-event-fx
 ::do-active-panel
 (fn [{db :db} [_ active-panel panel-params]]
   (let [nothing-goes-wrong? true]
     (if nothing-goes-wrong?
       {:db (assoc db
                   :active-panel active-panel
                   :panel-params panel-params)}
       {:dispatch-n [[::navigate ::home]
                     ;;[] error box msg
                     ]}))))

(reg-event-fx
 ::set-active-panel
 (fn [{db :db} [_ active-panel panel-params]]
   (let [event [::do-active-panel active-panel panel-params]]
     (cond
       (:do-something? db)
       {:db db
        :dispatch []}
       :else
       {:dispatch event}))))

(reg-event-db
 ::navigated
 (fn [db [_ new-match]]
   (let [old-match (:current-route db)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (assoc db :current-route (assoc new-match :controllers controllers)))))

(reg-event-fx
 ::navigate
 (fn [{db :db} [_ route & [params query]]]
   {::navigate! {:name route
                 :query query
                 :params params}}))

(reg-event-fx
 ::go-back
 (fn [{db :db} [_]]
   (::go-back! {})))

;; subscriptions ;;
(reg-sub
 ::current-route
 (fn [db]
   (:current-route db)))

;; effects ;;
(reg-fx
 ::navigate!
 (fn [{:keys [name params query]}]
   (rfe/push-state name params query)))

(reg-fx
 ::go-back!
 (fn [_]
   (.back js/window.history)))

(def routes
  [["/"
    {:name ::home
     :link-text "Home"
     :controllers
     [{ ;; Do whatever initialization needed for home page
       ;; I.e (re-frame/dispatch [::events/load-something-with-ajax])
       :start (fn []
                (js/console.log "Entering home page")
                (dispatch [::set-active-panel :home-panel nil]))
       ;; Teardown can be done here.
       :stop  (fn [& params] (js/console.log "Leaving home page"))}]}]
   ["/contract"
    {:name ::contract
     :link-text "Contract"
     :controllers
     [{:start (fn []
                (dispatch [::set-active-panel :contract-panel nil]))}]}]
   ["/about"
    {:name ::about
     :link-text "About"
     :controllers
     [{:start (fn []
                (dispatch [::set-active-panel :about-panel nil]))}]}]])

(def router
  (rf/router
   routes
   {:data {:coercion rcs/coercion}}))

(defn on-navigate
  [new-match]
  ;; - Put side-effects you want to run on every page load/change here!
  ;; Make sure there are no hanging popovers.
  ;;(ocall (js/$ ".popover") "remove")
  (if new-match
    (dispatch [::navigated new-match])
    ;; empty or invalid path
    (let [pathname (.. js/window -location -pathname)
          paths (clojure.string/split pathname #"/")]
      (js/console.log "Fallback to home page as the path "
                      pathname
                      " could not be resolved"))))

(defn href
  "Return relative url for given route. Url can be used in HTML links."
  ([name]
   (href name nil nil))
  ([name params]
   (href name params nil))
  ([name params query]
   (rfe/href name params query)))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   {:use-fragment true}))
