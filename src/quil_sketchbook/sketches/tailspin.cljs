(ns quil-sketchbook.sketches.tailspin
  (:require [quil.core :as q]
            [quil.middleware :as m]))

;; From http://quil.info/sketches/show/example_tailspin.

(defn setup []
  (q/frame-rate 30)
  (let [max-r (/ (q/width) 2)
        n (int (q/map-range (q/width)
                            200 500
                            20 50))]
    {:dots (into [] (for [r (map #(* max-r %)
                                 (range 0 1 (/ n)))]
                      [r 0]))}))

(def speed 0.0003)

(defn move [dot]
  (let [[r a] dot]
    [r (+ a (* r speed))]))

(defn update-state [state]
  (update-in state [:dots] #(map move %)))

(defn dot->coord [[r a]]
  [(+ (/ (q/width) 2) (* r (q/cos a)))
   (+ (/ (q/height) 2) (* r (q/sin a)))])

(defn draw-state [state]
  (q/background 255)
  (q/fill 0)
  (let [dots (:dots state)]
    (loop [curr (first dots)
           tail (rest dots)
           prev nil]
      (let [[x y] (dot->coord curr)]
        (q/ellipse x y 5 5)
        (when prev
          (let [[x2 y2] (dot->coord prev)]
            (q/line x y x2 y2))))
      (when (seq tail)
        (recur (first tail)
               (rest tail)
               curr)))))

(defn sketch [el]
  (q/sketch :host el
            :size [500 500]
            :setup setup
            :update #'update-state
            :draw #'draw-state
            :middleware [m/fun-mode]))
