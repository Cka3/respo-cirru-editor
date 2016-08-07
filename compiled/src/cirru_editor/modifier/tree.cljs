
(ns cirru-editor.modifier.tree)

(defn update-token [snapshot op-data]
  (let [[coord new-token] op-data]
    (-> snapshot (assoc-in (cons :tree coord) new-token))))

(defn after-token [snapshot op-data]
  (let [coord op-data]
    (if (> (count coord) 0)
      (-> snapshot
       (update-in
         (cons :tree (butlast coord))
         (fn [expression]
           (if (= (last coord) (dec (count expression)))
             (conj expression "")
             (into
               []
               (concat
                 (subvec expression 0 (inc (last coord)))
                 [""]
                 (subvec expression (inc (last coord))))))))
       (update
         :focus
         (fn [coord]
           (conj (into [] (butlast coord)) (inc (last coord))))))
      (if (= (:tree snapshot) [])
        (-> snapshot (assoc :focus [0]) (assoc :tree [""]))
        snapshot))))

(defn fold-node [snapshot op-data]
  (let [coord op-data]
    (-> snapshot
     (update-in (cons :tree coord) (fn [node] [node]))
     (update :focus (fn [coord] (conj coord 0))))))

(defn unfold-expression [snapshot op-data]
  (let [coord op-data]
    (cond
      (> (count coord) 1) (-> snapshot
                           (update
                             :tree
                             (fn [tree]
                               (let 
                                 [expression (get-in tree coord)
                                  position (last coord)]
                                 (update-in
                                   tree
                                   (butlast coord)
                                   (fn 
                                     [parent]
                                     (into
                                       []
                                       (cond
                                         (zero? position)
                                         (concat
                                           expression
                                           (rest parent))
                                         (=
                                           position
                                           (dec (count parent)))
                                         (concat
                                           (butlast parent)
                                           expression)
                                         :else
                                         (concat
                                           (subvec parent 0 position)
                                           expression
                                           (subvec
                                             parent
                                             (inc position)))))))))))
      (= 1 (count coord)) (-> snapshot
                           (update
                             :tree
                             (fn [parent]
                               (let 
                                 [expression (get-in parent coord)
                                  position (last coord)]
                                 (into
                                   []
                                   (cond
                                     (zero? position)
                                     (concat expression (rest parent))
                                     (= position (dec (count parent)))
                                     (concat
                                       (butlast parent)
                                       expression)
                                     :else
                                     (concat
                                       (subvec parent 0 position)
                                       expression
                                       (subvec
                                         parent
                                         (inc position)))))))))
      :else snapshot)))

(defn before-token [snapshot op-data]
  (let [coord op-data]
    (-> snapshot
     (update-in
       (cons :tree (butlast coord))
       (fn [parent]
         (into
           []
           (let [position (last coord)]
             (cond
               (zero? position) (cons "" parent)
               :else (concat
                       (subvec parent 0 position)
                       [""]
                       (subvec parent position))))))))))

(defn before-expression [snapshot op-data]
  (let [coord op-data]
    (-> snapshot
     (update-in
       (cons :tree (butlast coord))
       (fn [parent]
         (let [position (last coord)]
           (into
             []
             (cond
               (zero? position) (cons [""] parent)
               :else (concat
                       (subvec parent 0 position)
                       [[""]]
                       (subvec parent position)))))))
     (update :focus (fn [focus] (conj focus 0))))))

(defn after-expression [snapshot op-data]
  (let [coord op-data]
    (if (pos? (count coord))
      (-> snapshot
       (update-in
         (cons :tree (butlast coord))
         (fn [parent]
           (let [position (last coord)]
             (into
               []
               (cond
                 (= position (dec (count parent))) (conj parent [""])
                 :else (concat
                         (subvec parent 0 (inc position))
                         [[""]]
                         (subvec parent (inc position))))))))
       (update
         :focus
         (fn [focus]
           (conj (into [] (butlast focus)) (inc (last focus)) 0))))
      (if (= (:tree snapshot) [])
        (-> snapshot (assoc :focus [0]) (assoc :tree [""]))
        snapshot))))

(defn prepend-expression [snapshot op-data]
  (let [coord op-data]
    (-> snapshot
     (update-in
       (cons :tree coord)
       (fn [parent] (into [] (cons "" parent))))
     (update :focus (fn [focus] (conj focus 0))))))

(defn append-expression [snapshot op-data]
  (let [coord op-data expression (get-in snapshot (cons :tree coord))]
    (-> snapshot
     (update-in (cons :tree coord) (fn [parent] (conj parent "")))
     (update :focus (fn [focus] (conj focus (count expression)))))))

(defn remove-node [snapshot op-data]
  (let [coord op-data]
    (if (pos? (count coord))
      (-> snapshot
       (update-in
         (cons :tree (butlast coord))
         (fn [parent]
           (let [position (last coord)]
             (into
               []
               (cond
                 (= 1 (count parent)) []
                 (zero? position) (rest parent)
                 (= position (dec (count parent))) (butlast parent)
                 :else (concat
                         (subvec parent 0 position)
                         (subvec parent (inc position))))))))
       (update
         :focus
         (fn [focus]
           (into
             []
             (let [position (last focus)]
               (if (zero? position)
                 (butlast focus)
                 (concat (butlast focus) [(dec position)])))))))
      snapshot)))

(defn tree-reset [snapshot op-data]
  (let [tree op-data]
    (-> snapshot (assoc :tree tree) (assoc :focus []))))
