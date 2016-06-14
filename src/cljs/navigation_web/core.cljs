(ns navigation-web.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; -------------------------
;; Views

(defn first-scene-navbar [opacity]
  [:div {:style {:position "absolute"
                 :height "20px"
                 :top 0
                 :left 0
                 :right 0
                 :opacity opacity
                 :background-color "blue"}}])

(defn first-scene-content [left]
  [:div {:style {:display "absolute"
                 :top 0
                 :left left
                 :right 0
                 :bottom 0
                 :padding-bottom "30px"
                 :justify-content "center"
                 :background-color "white"
                 :align-items "center"}}
   [:span {:style {:flex 1}} "HDGJHGDJ"]])

(defn first-scene-tabbar [height bottom]
  [:div {:style {:position "absolute"
                 :height (str height "px")
                 :bottom (str "-" bottom "px")
                 :left 0
                 :right 0
                 :border "1px solid black"
                 :background-color "red"}}])

(defn second-scene-navbar [opacity]
  [:div {:style {:position "absolute"
                 :height "20px"
                 :top 0
                 :left 0
                 :right 0
                 :opacity opacity
                 :background-color "yellow"}}])

(defn second-scene-content [left]
  [:div {:style {:display "absolute"
                 :top 0
                 :left left
                 :right 0
                 :bottom 0
                 :padding-bottom "30px"
                 :justify-content "center"
                 :background-color "white"
                 :align-items "center"}}
   [:span {:style {:flex 1}} "sdfsdfsf"]])

(defn second-scene-tabbar [height bottom]
  [:div {:style {:position "absolute"
                 :height (str height "px")
                 :bottom (str "-" bottom "px")
                 :left 0
                 :right 0
                 :border "1px solid black"
                 :background-color "pink"}}])

(defn second-scene [{:keys [width left]}]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :position "relative"
                 :height "300px"
                 :width "150px"
                 :background-color "white"}}
   [second-scene-navbar {:left 0 :width 150}]
   [second-scene-content]
   [second-scene-tabbar]])

(def first-s {:navbar first-scene-navbar
              :content first-scene-content
              :tabbar first-scene-tabbar})

(def second-s {:navbar second-scene-navbar
               :content second-scene-content
               :tabbar second-scene-tabbar})

(defn navbar-transition [first-navbar second-navbar [width height progress]]
  (if (progress < 0.5)
    [first-navbar (- 1 (* progress 2))]
    [second-navbar (- (* progress 2) 1)]))

(defn content-transition [first-content second-content [width height progress]]
  (cond
    (= 0 progress) [first-content 0]
    (= 1 progress) [second-content 0]
    :else [:div
           [first-content 0]
           [second-content (* (- 1 progress) width)]]))

(defn tabbar-transition [first-tabbar second-tabbar [width height progress]]
  (let [tabbar-height 30]
    (if (progress < 0.5)
      [first-tabbar
       tabbar-height
       (- tabbar-height (* (- 1 (* progress 2)) tabbar-height))]
      [second-tabbar
       tabbar-height
       (- tabbar-height (* (- (* progress 2) 1) tabbar-height))])))

(def first-to-second {:navbar navbar-transition
                      :content content-transition
                      :tabbar tabbar-transition})

(defn home-page []
  (let [width 150
        height 300
        progress (reagent/atom 0)]
    (fn []
      [:div {:style {:position "relative"}}
       [first-scene {:width width :height height :progress @progress}]
       [second-scene {:width width :height height :progress @progress}]])))

(defn about-page []
  [:div [:h2 "About navigation-web"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
