
(ns cirru-editor.comp.editor
  (:require [respo.alias :refer [create-comp div]]))

(defn render [] (fn [state mutate!] (div {})))

(def comp-editor (create-comp :editor render))