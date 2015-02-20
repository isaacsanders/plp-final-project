(ns plp-final-project.game
  (:use [cljs.reader :only [read-string]])
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
      5
    (if (inCircle x y 50)
      4
      (if (inCircle x y 100)
        3
        (if (inCircle x y 150)
          2
          (if (inCircle x y 200)
            1
            0)))))))

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

(defn drawAim [reticle]
    (let [[x y] reticle
          target (.getElementById js/document "surface")
                  context (.getContext target "2d")
                  img (image "/images/reticle.png")]
          (.drawImage context img x y)))


(defn drawText [score]
    (let [target (.getElementById js/document "scoreboard")
          context (.getContext target "2d")]
       (do (set! (.-font context) "30px Arial")
           (.fillText context (str score) 370 35))))

(defn next-state [command value state]
  (let [[reticle score] state
        [x y] reticle]
    (.log js/console value)
    (case command
      "!Reset" [[(- x) (- y)] (- score)]
      "!Up"    [[0 (- value)] 0]
      "!Down"  [[0 value] 0]
      "!Right" [[value 0] 0]
      "!Left"  [[(- value) 0] 0]
      "!Shoot" [[(- x) (- y)] (calc-score x y)]
      [[0 0] 0])))

(declare process-response)

(defn check-command [reticle score command]
  (let [params (str/split command #" ")
        command (get params 0)
        value (int (get params 1))
        [nu-reticle nu-score] (next-state command value [reticle score])
        xhr (js/XMLHttpRequest.)]
    (.open xhr "POST" (-> js/window .-location .-pathname (str "/ajax")) true)
    (aset xhr "onreadystatechange" (fn []
                                     (case (.-readyState xhr)
                                       4 (process-response (.-response xhr))
                                       nil)))
    (.setRequestHeader xhr "Content-Type" "text/edn")
    (.send xhr {:reticle nu-reticle :score nu-score})))

(defn add-help [reticle score event]
  (ev/prevent-default event)
    (let [command (.-value (dom/by-id "usermsg"))]
      (check-command reticle score command)
      (dom/append! (dom/by-id "chatbox") (h/html [:div.help command]))
      (dom/set-value! (dom/by-id "usermsg") "")))

(defn process-response [res]
  (let [state (read-string res)
        commands (:commands state)
        reticle (:reticle state)
        score (:score state)]
    (ev/unlisten! (dom/by-id "calc"))
    (ev/listen! (dom/by-id "commandForm") :submit (partial add-help reticle score))
    (drawCircles)
    (clear-canvas)
    (drawAim reticle)
    (clear-text)
    (drawText score)))

(defn ^:export init [reticle score]
  (when (and js/document
             (aget js/document "getElementById"))
    (.setInterval js/window
                  (fn []
                    (let [xhr (js/XMLHttpRequest.)]
                      (.open xhr "GET" (str (-> js/window .-location .-pathname) "/ajax") true)
                      (aset xhr "onreadystatechange"
                            (fn []
                              (case (.-readyState xhr)
                                4 (process-response (.-response xhr))
                                nil)))
                      (.setRequestHeader xhr "content-type" "text/edn")
                      (.send xhr )))
                  500)))
