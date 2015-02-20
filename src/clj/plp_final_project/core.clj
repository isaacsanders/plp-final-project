(ns plp-final-project.core
  (:use [compojure.core]
        [hiccup.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.edn :as edn]
            [ring.util.response :as resp]))

(def game-states (agent {}))

(defn render [game-state]
  (let [[x y] (game-state :reticle)
        score (game-state :score)]
  (html
    [:html
      [:head
       [:title "Twitch Darts"]
       [:link {:rel "stylesheet" :href "/css/gameStyles.css"}]]
     [:body
      [:div {:id "overview"}
       [:div {:id "board" :class "board" :style "position:relative;"}
        [:canvas {:id "surface" :class "surface" :width 400 :height 400 :style "z-index: 3; position:absolute; left:0px; top:0px;"}]
        [:canvas {:id "scoreboard" :class "scoreboard" :width 400 :height 400 :style "z-index: 2; position:absolute; left:0px; top:0px;"}]
        [:canvas {:id "background" :class "background" :width 400 :height 400 :style "z-index: 1; position:absolute; left:0px; top:0px;"}]]
       [:div {:id "content"}
        [:div {:id "menu"}
         [:p {:class "welcome"} "Welcome, " [:b]]]
        [:div {:class "chatbox" :id "chatbox"}]
        [:form {:id "commandForm" :novalidate true}
         [:input {:name "usermsg" :type "text" :id "usermsg" :size 63}]
         [:button {:type "submit" :value "Submit" :id "calc"} "Submit"]]]]
      [:script {:src "/js/final.js"}]
      [:script (str "plp_final_project.game.init([" x "," y "]," score ");")]]])))

;; defroutes macro defines a function that chains individual route
;; functions together. The request map is passed to each function in
;; turn, until a non-nil response is returned.
(defroutes app-routes
  ; to serve document root address
  (GET "/" [] (resp/redirect "/index.html"))
  ; to serve static pages saved in resources/public directory

  (route/resources "/")

  (GET "/darts/:id" [id]
       (let [game-state (@game-states id)]
         (render (if (nil? game-state)
           {:reticle [0 0] :score 0}
           game-state))))

  (GET "/darts/:id/ajax" [id]
       (let [game-state (if (nil? (@game-states id))
                          {:reticle [0 0] :score 0}
                          (@game-states id))
             response {:status 200
                       :body (str game-state)
                       :headers {"Content-type" "text/edn"}}]
         response))

  (POST "/darts/:id/ajax" {{id :id} :params
                           body :body}
        (let [game-state (if (nil? (@game-states id))
                           {:reticle [0 0] :score 0}
                           (@game-states id))
              delta (read-string (slurp body))
              reticle (map + (game-state :reticle) (delta :reticle))
              score (+ (game-state :score) (delta :score))
              nu-game-state {:reticle reticle :score score}
              response {:status 200
                        :body (str nu-game-state)
                        :headers {"Content-type" "text/edn"}}]
          (send game-states #(assoc % id nu-game-state))
          response))

  ; if page is not found
  (route/not-found "Page not found"))

;; site function creates a handler suitable for a standard website,
;; adding a bunch of standard ring middleware to app-route:
(def handler
  (handler/site app-routes))
