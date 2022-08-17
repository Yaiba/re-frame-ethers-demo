(ns re-frame-ethers-demo.interceptors
    (:require
     [cljs.spec.alpha :as s]
     [re-frame.core :refer [debug after]]
     [re-frame-ethers-demo.db :as db]
     [re-frame-ethers-demo.config :refer [debug?]]))

(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (do
      (js/console.log "check spec: " (s/explain-str a-spec db))
      (throw (ex-info (str "spec check failed: "
                           (s/explain-str a-spec db)) {})))))

(def check-spec
  (partial check-and-throw ::db/db))

(def icheck [(when debug? debug)
             (when debug? (after check-spec))])
