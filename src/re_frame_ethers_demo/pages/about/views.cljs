(ns re-frame-ethers-demo.pages.about.views
  (:require [re-frame.core :as rf :refer [subscribe]]))

(defn main
  []
  [:h1 "About this project."]
  [:p "A combine of re-frame and ethers, try to mimic etherscan `contract` tab "])
