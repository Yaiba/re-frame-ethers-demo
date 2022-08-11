(ns re-frame-ethers-demo.error
  (:require [reagent.core :as rg]
            [re-frame.core :refer [subscribe dispatch-sync]]
            [oops.core :refer [oget]]
            [goog.json :as json]))

(defn error-string
  [{:keys [error info]}]
  (str error
       \newline
       (or (oget info :componentStack)
           (json/serialize info))))

(defn error-panel
  [{:keys [error on-reset]}]
  (let [error-text (error-string error)]
    [:div
     [:div
      [:h1 "Something went wrong"]]
     [:p "Bla bla ..."]
     [:p error-text]
     [:div
      [:button {:on-click on-reset} "Reset"]]]
    ))

(defn error-boundary
  [& children]
  (let [caught? (rg/atom false)
        !error (rg/atom nil)]
    (rg/create-class
     {:display-name "DemoErrorBoundary"
      :get-derived-state-from-error (fn [_]
                                      (reset! caught? true)
                                      #js {})
      :component-did-catch (fn [_ error info]
                             (reset! !error {:error error :info info}))
      :render (fn [this]
                (let [clear-error! (fn []
                                     ;; Change URL to root without triggering router.
                                     (.pushState js/window.history "home" "" "/")
                                     ;; Boot and clear error.
                                     ;;(dispatch-sync [:boot])
                                     (reset! !error nil)
                                     (reset! caught? false))]
                  (if @caught?
                    (when-let [error @!error]
                      [:div
                       [:p "TODO: MAYBE A ERROR MARK/ICON"]
                       [:main
                        [:div
                         [error-panel
                          {:error error
                           :on-reset clear-error!}]]]])
                    (into [:<>] (rg/children this)))))})))
