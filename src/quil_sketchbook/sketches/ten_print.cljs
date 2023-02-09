(ns quil-sketchbook.sketches.ten-print
  (:require [quil.core :as q]
            [quil.middleware :as m]))

;; From http://quil.info/sketches/show/example_ten-print.

(def scale 20)

(defn setup []
  (q/background 0)
  (q/frame-rate 20)
  (q/color-mode :hsb)
  {:x 0
   :y 0})

(defn update-state [{:keys [x y]}]
  (let [new-x (if (>= x (q/width)) 0 (+ x scale))
        new-y (if (zero? new-x) (+ y scale) y)]
    {:x new-x
     :y (if (>= new-y (q/height)) 0 new-y)}))

(defn draw-state [{:keys [x y]}]
  (when (and (zero? x) (zero? y))
    (q/background 0))
  (q/stroke (rand-int 255) (rand-int 255) 255)
  (if (> (rand) 0.5)
    (q/line x y (+ x scale) (+ y scale))
    (q/line x (+ y scale) (+ x scale) y)))

(defn sketch [el]
  (q/sketch :host el
            :size [500 500]
            :setup setup
            :update #'update-state
            :draw #'draw-state
            :middleware [m/fun-mode]))
