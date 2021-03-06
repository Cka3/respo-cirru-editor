
(ns cirru-editor.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo.alias :refer [create-comp div span]]
            [cirru-editor.comp.editor :refer [comp-editor]]))

(defn on-command [snapshot dispatch! e] (println "command" e))

(defn on-update! [snapshot dispatch!] (dispatch! :save snapshot))

(defn render [store]
  (fn [state mutate!]
    (div
     {:style {:position "absolute",
              :width "100%",
              :height "100%",
              :display "flex",
              :flex-direction "column",
              :background-color (hsl 0 0 0)}}
     (comp-editor store on-update! on-command))))

(def comp-container (create-comp :container render))
