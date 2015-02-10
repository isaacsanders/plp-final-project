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

(defn exp [x n]
  (reduce * (repeat n x)))

(defn inCircle [x y radius]
  (<= (+ (exp (- x 200) 2) (exp (- y 200) 2)) (exp radius 2)))

(defn calc-score [a b]
  (let [x (+ a 25)
        y (+ b 25)]
    (if (inCircle x y 25)
    "5"
    (if (inCircle x y 50)
      "4"
      (if (inCircle x y 100)
        "3"
        (if (inCircle x y 150)
          "2"
          (if (inCircle x y 200)
            "1"
            "0")))))))

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

(defn clear-text []
  (let [target (.getElementById js/document "scoreboard")
        context (.getContext target "2d")]
    (.clearRect context 0 0 400 400)))

(defn drawTarget []
    (let [target (.getElementById js/document "background")
          context (.getContext target "2d")
          img (image "images/target.png")]
       (.drawImage context img 0 0 400 400)
    )
  )

(defn drawCircles []
  (do (drawCircle 200 200 200 [10 10 10])
      (drawCircle 200 200 150 [9 79 11])
      (drawCircle 200 200 100 [9 13 79])
      (drawCircle 200 200 50 [83 20 150])
      (drawCircle 200 200 25 [71 125 124])))

(defn drawAim []
    (let [target (.getElementById js/document "surface")
          context (.getContext target "2d")
          img (image "images/reticle.png")]
       (.drawImage context img (get @state :x) (get @state :y))
    )
  )

(defn drawText [score]
    (let [target (.getElementById js/document "scoreboard")
          context (.getContext target "2d")]
       (do (set! (.-font context) "30px Arial")
           (.fillText context score 370 35))
    )
  )

(defn check-command [command]
  (let [params (str/split command #" ")]
    (case (get params 0)
      "!Reset" (do (swap! state assoc :y 0) (swap! state assoc :x 0) (clear-canvas) (drawAim) (clear-text) (drawText "0")),
      "!Up" (do (swap! state assoc :y (- (get @state :y) (js/parseInt (get params 1)))) (clear-canvas) (drawAim)),
      "!Down" (do (swap! state assoc :y (+ (get @state :y) (js/parseInt (get params 1)))) (clear-canvas) (drawAim)),
      "!Left" (do (swap! state assoc :x (- (get @state :x) (js/parseInt (get params 1)))) (clear-canvas) (drawAim)),
      "!Right" (do (swap! state assoc :x (+ (get @state :x) (js/parseInt (get params 1)))) (clear-canvas) (drawAim)),
      "!Shoot" (do (clear-text) (drawText (calc-score (get @state :x) (get @state :y))))
      nil)))

(defn add-help []
  (let [quantity (dom/value (dom/by-id "usermsg"))]
    (do (check-command quantity) (dom/append! (dom/by-id "chatbox") (h/html [:div.help quantity])) (dom/set-value! (dom/by-id "usermsg") "")
    )))

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (do
      (ev/listen! (dom/by-id "calc") :click add-help)
      (drawCircles)
      (drawAim)
      (drawText "0"))
    ))
