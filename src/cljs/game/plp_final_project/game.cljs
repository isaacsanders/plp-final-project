(ns plp-final-project.game
  (:require-macros [hiccups.core :as h])
  (:require [domina :as dom]
            [hiccups.runtime :as hiccupsrt]
            [domina.events :as ev]))


(defn add-help []
  (let [quantity (dom/value (dom/by-id "usermsg"))]
    (dom/append! (dom/by-id "chatbox")
                 (h/html [:div.help quantity]))))

(defn remove-help []
  (dom/destroy! (dom/by-class "help")))

(defn drawSquare []
    (let [target (.getElementById js/document "surface")
          context (.getContext target "2d")]
       (.fillRect context 0 0 400 400)
    )
  )

(defn image [src]
  (let [img (new js/Image)]
    (set! (. img -src ) src)
    img))

(defn drawTarget []
    (let [target (.getElementById js/document "surface")
          context (.getContext target "2d")
          img (image "images/target.png")]
       (.drawImage context img 0 0 400 400)
    )
  )

(defn drawAim []
    (let [target (.getElementById js/document "surface")
          context (.getContext target "2d")
          img (image "images/aim.png")]
       (.drawImage context img 0 0)
    )
  )

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (do
      (ev/listen! (dom/by-id "calc") :click add-help)
      (drawTarget)
      (drawAim))
    ))
