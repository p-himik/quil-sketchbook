(ns quil-sketchbook.core
  (:require [accountant.core :as accountant]
            [reagent.core :as r]
            [reagent.dom]
            [clojure.string :as str]
            [quil-sketchbook.sketches :as sketches]
            [quil.core :as q]
            ["react" :as react]))

(defonce root-el (js/document.getElementById "app"))

(defn set-document-title! [title]
  (set! js/document -title title))

(defn create-ref []
  (react/createRef))

(defn ref-node [^js ref]
  (.-current ref))

(def id->sketch (into {} (map (juxt :id identity)) sketches/ordered-sketches))

(defn unhide-canvas [node]
  ;; p5.js hides a canvas that's being set up and then
  ;; for some reason makes visible all the canvases that it can find on the page.
  ;; The issue is that when we create a parent node, we don't attach it to the document,
  ;; so `document.querySelectorAll` doesn't return it. Meaning, we have to either
  ;; manually un-hide the canvas or attach the node to the document in some invisible node.
  (when-let [canvas (.querySelector node "canvas")]
    (set! (.-style canvas) -visibility "")
    (js-delete (.-dataset canvas) "hidden")))

(defonce initialized-sketches (atom {}))

(swap! initialized-sketches
       (fn [id->s]
         (if (not= (set (map :id sketches/ordered-sketches))
                   (set (keys id->s)))
           (let [id->s (select-keys id->s (map :id sketches/ordered-sketches))]
             (reduce (fn [id->s {:keys [id init]}]
                       (if (id->s id)
                         id->s
                         (let [node (.createElement js/document "div")
                               applet (init node)]
                           (q/with-sketch applet
                             (q/no-loop))
                           (unhide-canvas node)
                           (assoc id->s id {:node   node
                                            :applet applet}))))
                     id->s sketches/ordered-sketches))
           id->s)))

(defn pause-sketch! [id]
  ;; Handling the case when a sketch was completely removed
  ;; from the vector of all sketches.
  (when-some [s (@initialized-sketches id)]
    (q/with-sketch (:applet s)
      (q/no-loop))))

(defn resume-sketch! [id]
  (q/with-sketch (:applet (@initialized-sketches id))
    (q/start-loop)))

(defn detach-sketch [node id]
  ;; Handling the case when a sketch was completely removed
  ;; from the vector of all sketches.
  (when-some [s (@initialized-sketches id)]
    (.removeChild node (:node s))))

(defn attach-sketch [node id]
  (.appendChild node (:node (@initialized-sketches id))))

(defn sketch-id->path [id]
  (str "/" (name id)))

(defn path->sketch-id [t]
  (let [path (cond-> t
               (str/starts-with? t "/")
               (subs 1))]
    (reduce (fn [_ {:keys [id]}]
              (when (= path (name id))
                (reduced id)))
            nil sketches/ordered-sketches)))

(def current-sketch-id (r/atom (path->sketch-id js/window.location.pathname)))

(def navigation {:nav-handler  (fn [path]
                                 (let [id (path->sketch-id path)]
                                   (reset! current-sketch-id id)
                                   (when-some [sketch (id->sketch id)]
                                     (set-document-title! (:label sketch)))))
                 :path-exists? (let [all-ids (set (map :id sketches/ordered-sketches))]
                                 (fn [path]
                                   (let [id (cond-> path
                                              (str/starts-with? path "/")
                                              (subs 1))]
                                     (all-ids (keyword id)))))})

(defn sketch-selector-button [{:keys [id label]}]
  [:button {:on-click #(accountant/navigate! (sketch-id->path id))
            :style    (when (= id @current-sketch-id)
                        {:border-style "inset"})}
   label])

(defn sketch-selector []
  [:div {:style {:display        "flex"
                 :flex-direction "column"
                 :flex           "0 0 auto"}}
   (for [sketch sketches/ordered-sketches]
     ^{:key (:id sketch)}
     [sketch-selector-button sketch])])

(defn sketch-container [{#_#_:keys [init]} _active?]
  (let [ref (create-ref)]
    (r/create-class
      {:display-name
       "sketch"

       :component-did-mount
       (fn [this]
         (let [[_ {:keys [id]} active?] (r/argv this)]
           (attach-sketch (ref-node ref) id)
           (when active?
             (resume-sketch! id))))

       :component-will-unmount
       (fn [this]
         (let [[_ {:keys [id]} _] (r/argv this)]
           (detach-sketch (ref-node ref) id)
           (pause-sketch! id)))

       :component-did-update
       (fn [this old-argv]
         (let [[_ _ old-active?] old-argv
               [_ {:keys [id]} active?] (r/argv this)]
           (cond
             (and old-active? (not active?))
             (pause-sketch! id)

             (and (not old-active?) active?)
             (resume-sketch! id))))

       :reagent-render
       (fn [_ active?]
         [:div {:ref   ref
                :style (when-not active? {:display "none"})}])})))

(defn sketches-panel []
  [:div {:style {:flex-grow "1"}}
   (let [curr-id @current-sketch-id]
     (for [{:keys [id] :as sketch} sketches/ordered-sketches]
       ^{:key id}
       [sketch-container sketch (= id curr-id)]))])

(defn ui-root []
  [:div {:style {:display        "flex"
                 :flex-direction "row"}}
   [sketch-selector]
   [sketches-panel]])

(defn ^:dev/after-load init []
  (accountant/configure-navigation! navigation)
  (when-not @current-sketch-id
    (accountant/navigate! (sketch-id->path (:id (first sketches/ordered-sketches)))))
  (reagent.dom/render [ui-root] root-el))

(defn ^:dev/before-load stop []
  (accountant/unconfigure-navigation!))
