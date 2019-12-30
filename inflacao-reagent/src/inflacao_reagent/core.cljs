(ns inflacao-reagent.core
    (:require
      [reagent.core :as r]
      [cljsjs.semantic-ui-react :as ui]))

(def inflacao-selecionada (r/atom ""))

;; -------------------------
;; Views

(defn home-page []
  [:main
   [:div
    [:h1 "Calculadora de Inflação"]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
