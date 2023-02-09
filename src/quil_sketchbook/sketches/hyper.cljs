(ns quil-sketchbook.sketches.hyper
  (:require [quil.core :as q]
            [quil.middleware :as m]))

;; From http://quil.info/sketches/show/example_hyper.

(defn setup []
  (q/frame-rate 100)
  (q/background 255)
  (q/rect-mode :center)
  {:r   0.0
   :col 0})

(defn tick [state]
  (update-in state [:r] + 5.0))

(defn flip [state]
  {:r   0.0
   :col (if (= 0 (:col state)) 255 0)})

(defn update-state [state]
  (if (< (:r state) 300)
    (tick state)
    (flip state)))

(defn draw-state [state]
  (q/stroke (:col state))
  (let [hw (* 0.5 (q/width))
        hh (* 0.5 (q/height))]
    (dotimes [_ (quot (q/width) 10)]
      (let [rand-ang (q/random 0 q/TWO-PI)
            r (:r state)]
        (q/line hh
                hw
                (+ hh (* (q/sin rand-ang) r))
                (+ hw (* (q/cos rand-ang) r)))))))

(defn sketch [el]
  (q/sketch :host el
            :size [500 500]
            :setup setup
            :update #'update-state
            :draw #'draw-state
            :middleware [m/fun-mode]))
