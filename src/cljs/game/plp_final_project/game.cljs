(ns plp-final-project.game
  (:require-macros [hiccups.core :as h])
  (:require [domina :as dom]
            [ajax.core :refer [GET POST]]
            [hiccups.runtime :as hiccupsrt]
            [domina.events :as ev]
            [clojure.string :as str]))

(def twopi (* 2 (.-PI js/Math)))

(defn to-color [& rgbas]
  (let [csv (apply str (interpose ", " rgbas))]
    (str "rgb(" csv ")")))

(defn exp [x n]
  (reduce * (repeat n x)))

(defn inCircle [x y radius]
  (<= (+
        (exp (- x 200) 2)
        (exp (- y 200) 2))
      (exp radius 2)))

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
    (aset context "fillStyle" (apply to-color color))
    (.beginPath context)
    (.arc context x y radius 0 twopi)
    (.closePath context)
    (.fill context)))

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
        img (image "/images/target.png")]
    (.drawImage context img 0 0 400 400)))

(defn drawCircles []
  (drawCircle 200 200 200 [10 10  10])
  (drawCircle 200 200 150 [9  79  11])
  (drawCircle 200 200 100 [9  13  79])
  (drawCircle 200 200 50  [83 20  150])
  (drawCircle 200 200 25  [71 125 124]))

(defn drawAim [reticle]
  (let [target (.getElementById js/document "surface")
        context (.getContext target "2d")
        img (image "/images/reticle.png")]
    (.drawImage context img (get reticle "x") (get reticle "y"))))

(defn drawText [score]
  (let [target (.getElementById js/document "scoreboard")
        context (.getContext target "2d")]
    (set! (.-font context) "30px Arial")
    (.fillText context score 370 35)))

(defn render [response]
  (.log js/console response))

(defn check-command [reticle score command]
  (let [params (str/split command #" ")
        value (get params 1)
        [x y] (map #(get reticle %) ["x" "y"])
        [nu-reticle nu-score] (case (get params 0)
                          "!Reset" [[0 0] 0]
                          "!Up" [[x (+ y value)] score]
                          "!Down" [[x (- y value)] score]
                          "!Right" [[(+ x value) y] score]
                          "!Left" [[(- x value) y] score]
                          "!Shoot" [[0 0] (+ score value)])]
    (POST (str (.-location js/window) "/ajax") {:params {:reticle nu-reticle :score nu-score} :format :json})
    (GET (.-location js/window) {:handler render :response-format :json})
    ;(case (get params 0)
    ;  "!Reset" (do
    ;  "!Up" (do
    ;          (swap! state assoc :y (- (get @state :y) (js/parseInt (get params 1))))
    ;          (clear-canvas)
    ;          (drawAim)),
    ;  "!Down" (do (swap! state assoc :y (+ (get @state :y) (js/parseInt (get params 1))))
    ;            (clear-canvas)
    ;            (drawAim)),
    ;  "!Left" (do
    ;            (swap! state assoc :x (- (get @state :x) (js/parseInt (get params 1))))
    ;            (clear-canvas)
    ;            (drawAim)),
    ;  "!Right" (do
    ;             (swap! state assoc :x (+ (get @state :x) (js/parseInt (get params 1))))
    ;             (clear-canvas)
    ;             (drawAim)),
    ;  "!Shoot" (do
    ;             (clear-text)
    ;             (drawText (+ score (calc-score (get @state :x) (get @state :y)))))
    ;  nil)))

(defn add-help [reticle score & event]
  (let [command (dom/value (dom/by-id "usermsg"))]
    (check-command reticle score command)
    (dom/append! (dom/by-id "chatbox") (h/html [:div.help quantity]))
    (dom/set-value! (dom/by-id "usermsg") "")))

(defn ^:export init [reticle score]
  (when (and js/document
             (aget js/document "getElementById"))
    (do
      (ev/listen! (dom/by-id "calc") :click (partial add-help reticle score))
      (drawCircles)
      (drawAim reticle)
      (drawText score))
    ))
