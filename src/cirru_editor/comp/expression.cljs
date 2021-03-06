
(ns cirru-editor.comp.expression
  (:require [hsl.core :refer [hsl]]
            [respo.alias :refer [create-comp div]]
            [respo.comp.text :refer [comp-text]]
            [respo.comp.space :refer [comp-space]]
            [respo.comp.debug :refer [comp-debug]]
            [cirru-editor.comp.token :refer [comp-token]]
            [cirru-editor.util.detect :refer [coord-contains? shallow? deep?]]
            [cirru-editor.util.keycode :as keycode]))

(declare render)

(declare comp-expression)

(defn update-state [state] (not state))

(def style-folded
  {:display "inline-block",
   :color (hsl 180 80 60),
   :font-family "Source Code Pro,Menlo,monospace",
   :font-size "15px",
   :outline "none",
   :border-width "1px",
   :border-style "solid",
   :border-color (hsl 0 0 100 0.5),
   :padding-left 16,
   :padding-right 16,
   :vertical-align "top",
   :line-height "27px",
   :border-radius "16px",
   :cursor "pointer",
   :margin-bottom "4px"})

(defn on-click [modify! coord focus]
  (fn [e dispatch!] (if (not= coord focus) (modify! :focus-to coord dispatch!))))

(defn on-unfold [mutate!] (fn [e dispatch!] (mutate!)))

(defn init-state [& args] false)

(defn on-keydown [modify! coord on-command mutate!]
  (fn [e dispatch!]
    (let [code (:key-code e)
          event (:original-event e)
          shift? (.-shiftKey event)
          command? (or (.-metaKey event) (.-ctrlKey event))]
      (cond
        (= code keycode/space)
          (do
           (.preventDefault event)
           (if shift?
             (modify! :before-token coord dispatch!)
             (modify! :after-token coord dispatch!)))
        (= code keycode/tab)
          (do
           (.preventDefault event)
           (if shift?
             (modify! :unfold-expression coord dispatch!)
             (modify! :fold-node coord dispatch! dispatch!)))
        (= code keycode/enter)
          (if command?
            (if shift?
              (modify! :append-expression coord dispatch!)
              (modify! :prepend-expression coord dispatch!))
            (if shift?
              (modify! :before-expression coord dispatch!)
              (modify! :after-expression coord dispatch!)))
        (= code keycode/backspace)
          (do (.preventDefault event) (modify! :remove-node coord dispatch!))
        (= code keycode/left)
          (do (.preventDefault event) (modify! :node-left coord dispatch!))
        (= code keycode/right)
          (do (.preventDefault event) (modify! :node-right coord dispatch!))
        (= code keycode/up) (do (.preventDefault event) (modify! :node-up coord dispatch!))
        (= code keycode/down)
          (do (.preventDefault event) (modify! :expression-down coord dispatch!))
        (and command? (= code keycode/key-b))
          (do (.preventDefault event) (modify! :duplicate-expression coord dispatch!))
        (and command? (= code keycode/key-c)) (modify! :command-copy coord dispatch!)
        (and command? (= code keycode/key-x)) (modify! :command-cut coord dispatch!)
        (and command? (= code keycode/key-v)) (modify! :command-paste coord dispatch!)
        (and command? shift? (= code keycode/key-f)) (mutate!)
        :else (if command? (on-command e dispatch!) nil)))))

(defn render [expression modify! coord level tail? focus on-command head? after-expression?]
  (fn [state mutate!]
    (let [exp-size (count expression)]
      (if state
        (div
         {:style style-folded,
          :event {:click (on-unfold mutate!),
                  :keydown (on-keydown modify! coord on-command mutate!)}}
         (comp-text (first expression) nil))
        (div
         {:style (merge
                  {}
                  (if (and (shallow? expression)
                           (not after-expression?)
                           (not tail?)
                           (pos? level)
                           (< (count expression) 5))
                    {:display "inline-block",
                     :border-width "0 0 1px 0",
                     :padding-left 17,
                     :padding-right 15,
                     :padding-bottom 2,
                     :margin-left 4,
                     :margin-right 4,
                     :text-align "center",
                     :background-color (hsl 200 80 80 0)})
                  (if (and tail? (not head?) (pos? level))
                    {:display "inline-block",
                     :border-width "0 0 0 1px",
                     :background-color (hsl 0 80 80 0)})
                  (if (= coord focus) {:border-color (hsl 0 0 100 0.6)})),
          :attrs (merge
                  {:tab-index 0, :class-name "cirru-expression"}
                  (if (= coord focus) {:id "editor-focused"})),
          :event {:click (on-click modify! coord focus),
                  :keydown (on-keydown modify! coord on-command mutate!)}}
         (loop [acc [], idx 0, expr expression, child-after-expression? false]
           (if (empty? expr)
             acc
             (let [item (first expr)
                   pair [idx
                         (let [child-coord (conj coord idx)
                               child-focus (if (coord-contains? focus child-coord)
                                             focus
                                             nil)
                               child-head? (zero? idx)]
                           (if (string? item)
                             (comp-token
                              item
                              modify!
                              child-coord
                              child-focus
                              on-command
                              child-head?)
                             (comp-expression
                              item
                              modify!
                              child-coord
                              (inc level)
                              (and (or after-expression? (not tail?))
                                   (= (dec exp-size) idx))
                              child-focus
                              on-command
                              child-head?
                              child-after-expression?)))]
                   next-acc (conj acc pair)]
               (recur next-acc (inc idx) (rest expr) (vector? item))))))))))

(def comp-expression (create-comp :expression init-state update-state render))

(def style-expression
  {:border-style "solid",
   :border-color (hsl 0 0 32 0.9),
   :outline "none",
   :padding-left 8,
   :padding-right 0,
   :padding-top 2,
   :padding-bottom 0,
   :margin-left 12,
   :margin-right 0,
   :margin-top 0,
   :margin-bottom 4,
   :border-width "0 0 0 1px",
   :min-height "26px",
   :min-width "16px",
   :vertical-align "top",
   :box-sizing "border-box"})
