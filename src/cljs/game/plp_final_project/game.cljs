(ns plp-final-project.game
  (:require-macros [hiccups.core :as h])
  (:require [domina :as dom]
            [hiccups.runtime :as hiccupsrt]
            [domina.events :as ev]
            [clojure.string :as str]))

(def state (atom {:x 0 :y 0}))
(def twopi (* 2 (.-PI js/Math)))

(defn to-color [& rgbas]
  (let [csv (apply str (interpose ", " rgbas))]
    (str "rgb(" csv ")")))

(defn remove-help []
  (dom/destroy! (dom/by-class "help")))

(defn image [src]
  (let [img (new js/Image)]
    (set! (. img -src ) src)
    img))

(defn drawCircle [x y radius color]
  (let [target (.getElementById js/document "background")
          context (.getContext target "2d")]
       (do
            (aset context "fillStyle" (apply to-color color))
            (.beginPath context)
            (.arc context x y radius 0 twopi)
            (.closePath context)
            (.fill context))
    )
  )

(defn clear-canvas []
  (let [target (.getElementById js/document "surface")
        context (.getContext target "2d")]
    (.clearRect context 0 0 400 400)))

(defn drawTarget []
    (let [target (.getElementById js/document "background")
          context (.getContext target "2d")
          img (image "images/target.png")]
       (.drawImage context img 0 0 400 400)
    )
  )

(defn drawAim []
    (let [target (.getElementById js/document "surface")
          context (.getContext target "2d")
          img (image "images/aim.png")]
       (.drawImage context img (get @state :x) (get @state :y))
    )
  )

(defn check-command [command]
  (let [params (str/split command #" ")]
    (case (get params 0)
      "!Clear" (clear-canvas),
      "!Up" (do (swap! state assoc :y (- (get @state :y) (js/parseInt (get params 1)))) (clear-canvas) (drawAim)),
      "!Down" (do (swap! state assoc :y (+ (get @state :y) (js/parseInt (get params 1)))) (clear-canvas) (drawAim)),
      "!Left" (do (swap! state assoc :x (- (get @state :x) (js/parseInt (get params 1)))) (clear-canvas) (drawAim)),
      "!Right" (do (swap! state assoc :x (+ (get @state :x) (js/parseInt (get params 1)))) (clear-canvas) (drawAim)),
      1)))

(defn add-help []
  (let [quantity (dom/value (dom/by-id "usermsg"))]
    (do (check-command quantity) (dom/append! (dom/by-id "chatbox") (h/html [:div.help quantity])) (dom/set-value! (dom/by-id "usermsg") "")
    )))

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (do
      (ev/listen! (dom/by-id "calc") :click add-help)
      (drawTarget)
      (drawAim))
    ))
