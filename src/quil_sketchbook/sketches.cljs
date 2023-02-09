(ns quil-sketchbook.sketches
  (:require [quil-sketchbook.sketches.hyper :as hyper]
            [quil-sketchbook.sketches.tailspin :as tailspin]
            [quil-sketchbook.sketches.ten-print :as ten-print]))

(def ordered-sketches [{:id    :tailspin
                        :label "Tailspin"
                        :init  tailspin/sketch}
                       {:id    :ten-print
                        :label "Ten Print"
                        :init  ten-print/sketch}
                       {:id    :hyper
                        :label "Hyper"
                        :init  hyper/sketch}])
