(ns ablog.haha)


(let [[a b c & more] (range 10)]
  (println a " " b " " c )
  (println more))




(let [range-vec (vec (range 10))
      [a b c & more :as all] range-vec]
  (println a " " b " " c)
  (println more)
  (println all))


(defn xors [x-max y-max] (for [x (range x-max) y (range y-max)] [x y (bit-xor x y)]))


(def a)
(def frame (java.awt.Frame.))

(for [method (seq (.getMethods java.awt.Frame))
      :let [method-name (.getName method)]
      :when (re-find #"Vis" method-name)]
  method-name)

(.isVisible frame)
(.setVisible frame true)

(.setSize frame (java.awt.Dimension. 400 200))

(def gfx (.getGraphics frame))

(.fillRect gfx 100 100 50 75)

(.setColor gfx (java.awt.Color. 255 128 0))

(.fillRect gfx 100 150 75 50)

(doseq [[x y xor] (xors 200 200)]
  (.setColor gfx (java.awt.Color. xor xor 0))
  (.fillRect gfx x y 1 1))

(let [[a & b :as c] [1 2 3]]
  (println a ", " b ", " c))

(let [{:keys [a b & more ] :as all} {:a 1 :b 2 :c 3}]
  (println a ", " b ", " ", " all ", " more))

(for [a (range 10) b (range 10) :when (> a b)] [a b])

(let [{a 1 b 2} [1 2 3 4]]
  (println a ", " b))

(let [{:strs [a b c]} {"a" 3 "b" 4 "c" 5}]
  (println a b c))

(def m {:a 5 :b 6 :c [7 8 9] :d {:e 10 :f 11} "foo" 88 42 false})
(let [{a :a b :b} m]
  (println a "," b))

(let [{foo "foo" aaa :bbb :or {aaa 50}} m]
  (println foo aaa))

(+ 1 1)

(def s "
$q1001=11;
$v1020=12;
$q1001_1=123;
$v10432_3=4324;
")

(re-seq #"\$[qv][\d_]*" s)

(let [{f 42} m]
  (println f))
(= (last (sort (rest (reverse [2 5 4 1 3 6]))))
   (-> [2 5 4 1 3 6] (reverse) (rest) (sort) (last))
   5)

(= ((fn [v] (reduce (fn [new-v new-n] (if (= (last (last new-v)) new-n) (do (println new-v) (update-in new-v [(dec (count new-v))] #(conj % new-n))) (do (println new-v) (conj new-v [new-n])))) [] v)) [1 1 2 1 1 1 3 3]) '((1 1) (2) (1 1 1) (3 3)))

(partition-by #(= 3 %) [1 2 3 4 5])

(partition-by count ["a" "b" "ab" "ac" "c"])
`
(partition-by #(rem % 3) [1 2 3 4 5 6 9 12 7 8])

(partition-by identity [1 1 2 3 3])


(reduce * 2 [3 4 5])

(= (take 5 ((fn ) + (range))) [0 1 3 6 10])

(rem 6 3)

(= (take 5 ((fn ff ([f r coll] (lazy-seq (for [x coll] (f r coll))))
              ([f coll] (ff (first coll) (rest coll)))) + (range))) [0 1 3 6 10])

(= (take 5 ((fn ff [f coll]
              (map f coll)) + (range))) [0 1 3 6 10])


(defn fib
  ([]
   (fib 1 1))
  ([a b]
   (lazy-seq (cons a (fib b (+ a b))))))

(defn lala
  ([f coll]
    (lala f (first coll) (rest coll)))
  ([f r coll] (lazy-seq (cons (f r (first coll)) (if (seq coll) (lala (f r (first coll)) (rest coll))) '())))
  )

(seq '())
(take 5 (lala + 1 [0 1 2 3 4 5 6]))


(cons 1 [2])

(take 8 (fib))

(def aa (fn ff ([f a] (+ f a))
   ([f a b] (+ f a b))))
(aa 1 2)
(aa 1 2 3)



(= [21 6 1] (((fn [& args] (fn [& a] (map #(apply % a) args))) + max min) 2 3 5 1 6 4))


(= true ((comp zero? #(mod % 8) +) 3 5 7 9))

(= [3 2 1] (((fn [& args]
               (fn [& a2]
                 (let [fs (reverse args)
                       r (apply (first fs) a2)]
                   (loop [a r fs2 (rest fs)]
                     (if-let [f (first fs2)]
                       (recur (f a) (rest fs2)) a))))) zero? #(mod % 8) +) 3 5 7 9))


(fn myComp
  ([f] f)
  ([f1 f2]
   (fn[& args]
     (f1 (apply f2 args))))
  ([f1 f2 & fs]
   (apply myComp (myComp f1 f2) fs)))

(group-by count ["aaa" "b" "cc" "dd" "eee"])


(apply (comp +) '(1 2 3 4))

((fn leep [v]
   (for [item v]
     (if (coll? item)
       (leep item)
       (println item)))
    ) '((1 2) 3 "a" "b" [4 [5 6]]))

(mapcat #(do (println %1 %2) (list %1 %2)) [1 2 3] [:a :b :c])

(= (apply str (#(map first (partition-by identity %)) "Leeeeeerrroyyy")) "Leroy")
(= (apply str ((fn f [v] (reduce #(if (not= (last %1) %2) (conj %1 %2) %1) [] v)) "Leeeeeerrroyyy")) "Leroy")

(first ["a" "b"])
(first [])
(last [])

(= (__ [1 0 1 2 3 0 4 5]) [0 1 2 3])



(= ((fn [v] (or (last (sort-by count (filter #(> (count %) 1) (reduce #(if (= (last (last %1)) (dec %2)) (update-in %1 [(dec (count %1))] conj %2) (conj %1 [%2])) [] v)))) []))  [7 6 5 4]) [0 1 2 3])


(= ((fn [l coll] (loop [result [] c coll] (if (< (count c) l) result (recur (conj result (take l c)) (drop l c))))) 3 (range 9)) '((0 1 2) (3 4 5) (6 7 8)))


(= (#(group-by count %) [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})

(fn [coll] (into {} (map (fn [[k v]] [k (count v)]) (group-by identity coll))))

(#(let [new-map (group-by identity %)] (zipmap (keys new-map) (map count (vals new-map)))) [1 1 2 3 2 1 1])

(group-by count ["a" "as" "asd" "aa" "asdf" "qwer"])

((fn [a v] (rest (mapcat #(list a %) v)))  0 [1 2 3])

(interleave [:a :b :c] [:d :e :f] [1 2 3])

(interpose "," [:a :b :c])

(#(partition (/ (count %1) %2) (apply interleave (partition %2 %1))) (range 10) 5)

(= (#(apply map list (partition %2 %1)) [1 2 3 4 5 6] 2) '((1 3 5) (2 4 6)))

(= (#(let [l (count %2) y (rem %1 l) s (if (> y 0) y (+ l y)) v (vec %2)] (concat (subvec v s) (subvec v 0 s))) -4 '(:a :b :c)) '(3 4 5 1 2))

(take 5 (iterate #(+ 3 %) 1))

(set (#(vals (group-by type %)) [1 :a 2 :b 3 :c]))

(= 3 (((fn [f] (fn [n v] (f v n))) nth) 2 [1 2 3 4 5]))

(= (#(conj [] (subvec %2 0 %1) (subvec %2 %1)) 1 [:a :b :c :d]) [[1 2 3] [4 5 6]])

(= (#(apply * (range 1 (inc %))) 5) 120)

(list 3)

(reduce * (range 1 (inc 5)))

(reduce * (take-while pos? (iterate dec 5)))
(reduce * (take 5 (iterate inc 1)))

(not= :a :b)
(#(clojure.string/replace % #"[^A-Z]" "") "HeLlO, WoRlD!")
(clojure.string/replace )
(tree-seq seq? identity '((1 2 (3)) (4)))

(partition-all 2 3 [1 2 3 4 5 6 7 8])
(#(apply concat (partition (dec %2) %2 [] %1)) [1 2 3 4 5 6 7 8] 3)

(apply concat '((1 2) (4 5) (7 8)))

(= ((fn my-range [start end] (loop [current start result []] (if (< current end) (recur (inc current) (conj result current)) result))) -2 4) '(1 2 3))

(#(filter (complement coll?) (tree-seq seq? identity %)) '((1 2) 3 (4 (5 6 (7 8)))))


((fn myFlatten [x]
   (if (coll? x)
     (mapcat myFlatten x)
     (do
       (println x)
       [x]))) [[1 2] 3 [4 [5 6 [7 8]]]])

((fn myFlatten [x]
   (if (coll? x)
     (mapcat myFlatten x)
     [x])) [[1 2] 3 [4 [5 6 [7 8]]]])

(tree-seq map? #(do (println %) (identity %)) {:a 1 :b {:c 3 :d 4 :e {:f 6 :g 7}}})


(= (#(filter (complement coll?) (tree-seq coll? identity %)) '((1 2) 3 [4 [5 6]])) '(1 2 3 4 5 6))

(coll? [])
(+ 1 1)

(= ((fn [s]
      (loop [res [] left s]
        (if (seq left)
          (let [take-f #(= % (first left))] (recur (conj res (take-while take-f left)) (drop-while take-f left)))
          res))
      ) [:a :a :b :b :c]) '((:a :a) (:b :b) (:c)))


(fn [v] (reduce (fn [new-v new-n] (if (= (last (last new-v)) new-n) (update-in new-v [(dec (count new-v))] #(conj % new-n)) (conj new-v [new-n]))) [] v))

(take-while #(> % 3) [6 5 4 3 1 2])


(filter #((complement coll?) %) (tree-seq seq? #(do (println %) (if ((complement coll?) %) nil %)) '((1 2 (3)) (4))))

(tree-seq seq? #(do (println %) (if (coll? %) [:a] [:b])) '((1 2 (3)) (4)))

(= '(1 2) [1 2])

(= ((fn [v n] (reduce #(apply conj %1 (repeat n %2)) [] v)) [[1] [2] [3]] 3) '(1 1 2 2 3 3))


((fn [v n] (reduce #(concat %1 (repeat n %2)) [] v)) [[1] [2] [3]] 3)

((fn [v n] (apply concat (map #(repeat n %) v))) [1 2 3] 3)
((fn [v n] (mapcat #(repeat n %) v)) [[1] 2 3] 3)

(let [{r1 :x r2 :y :as randoms}
      (zipmap [:x :y :z] (repeatedly (partial rand-int 10)))]
  (println randoms)
  (assoc randoms :sum (+ r1 r2)))


(let [{v 42} m]
  (if v 1 0))

(let [{{e :e} :d} m]
  (println e))

(let [{[x y _] :c} m]
  (println x y))

(+ 1 1)


(= (take 5 ((fn fa [f a] (lazy-seq (cons a (fa f (f a))))) #(* 2 %) 1)) [1 2 4 8 16])



(= (take 5 (#(reductions (fn [i _] (%1 i)) (repeat %2)) #(* 2 %) 1)) [1 2 4 8 16])


(= ((fn [f coll] (reduce #(assoc-in %1 [(f %2)] (if ((f %2) %1) (conj ((f %2) %1) %2) [%2])) {} coll)) #(> % 5) [1 3 6 8]) {false [1 3], true [6 8]})


(= ((fn [f coll]
      (reduce #(let [key (f %2)] (if (get %1 key) (update-in %1 [key] conj %2) (assoc-in %1 [key] [%2]))) {} coll)) #(> % 5) [1 3 6 8]) {false [1 3], true [6 8]})

(re-find #"{.*?}" "{:a 1, :b 2}")
(re-matches #"^\{.*?\}$" "{:a 1, :b 2}")
(re-matches #"^\[.*?\]$" "[:a 1 :b 2]")

(str (range (rand-int 20)))
(apply hash-map {:a 1, :b 2})

(first {:a 1, :b 2})

(str '(1 2 3))

(mod 4 3)

(= ((fn [a] (let [r (range 2 (inc a))] (filter #(> % 0) (map #(if (= (mod a %) 0) % 0) r)))) 2) 2)

(take 5 (filter #((complement nil?) %) (for [a (range 2 6) :when (some #((complement nil?) %) (map #(if (and (zero? (mod a %)) (not= a %)) % nil) (range 2 (inc a))))] a)))

(mod 6 3)

(fn [n]
  )



(= ((fn [f m & args] (reduce (fn [r i] (println i)) m args )) * {:a 2, :b 3, :c 4} {:a 2} {:b 2} {:c 5})
   {:a 4, :b 6, :c 20})





(= ( * {:a 2, :b 3, :c 4} {:a 2} {:b 2} {:c 5})
   {:a 4, :b 6, :c 20})


(map (fn [[k v]] (println k v)) {:a 2, :b 3, :c 4})

(= __
   (loop [x 5
          result []]
     (if (> x 0)
       (recur (dec x) (conj result (+ 2 x)))
       result)))


(= __
   (loop [x 5
          result []]
     (if (> x 0)
       (recur (dec x) (conj result (+ 2 x)))
       result)))


(fn [n]
  (->>
    (range)
    (drop 2)
    (filter (fn [x] (every? #(< 0 (mod x %)) (range 2 x))))
    (take n)))

((fn [n]
   (->>
     (range)
     (drop 2)
     (filter (fn [x] (every? #(> (mod x %) 0) (range 2 x))))
     (take n))) 5)

(mod 5 4)

(mod 3 6)
(mod 3 2)
(mod 3 3)
(mod 3 2)
;如果要获取10个
;那么从2到无穷大循环
;判断当前数是不是质数，如果是质数，那么返回该质数，如果不是质数，返回nil，继续判断下一个数

(defn is_zhishu [n]
  (let [r (range 2 n)]
    (map #(if (zero? (mod n %)) % 0) r)))

(is_zhishu 6)
(every? #(<= % 2) (is_zhishu 3))

((fn [l] (take l (filter (fn [n]
                           (let [r (range 2 n)]
                             (every? #(< % 2) (map #(if (zero? (mod n %)) % 0) r)))) (drop 2 (range))))) 2)


((fn [n]
   (if (< n 2)
     false
     (let [r (range 2 n)]
       (every? #(< % 2) (map #(if (zero? (mod n %)) % 0) r))))) 4)

(map (fn [a] (for [i (range 2 a) :while (complement (some #(> % 0) (map #(if (and (zero? (mod a %)) (not= a %)) % 0) (range 4 a))))] a)) (range 2 11))
(range 2 1)
(range 2 0)

(seq 1)
(max 1 2)
(mod 5 10)
(mod 10 5)
(mod 4 2)
(range 1 3)

(conj #{} 1 1)
(map #(let [s (str %)] (cond
                         (re-matches #"^\{.*?\}$" s) :map
                         (re-matches #"^\(.*?\)" s) :list
                         (re-matches #"^#\{.*?\}$" s) :set
                         (re-matches #"^\[.*?\]$" s) :vector
                         )) [{} #{} [] ()])

(= :list (#(let [s (str %)] (cond
                              (re-matches #"^\{.*?\}$" s) :map
                              (re-matches #"^\(.*?\)" s) :list
                              (re-matches #"^#\{.*?\}$" s) :set
                              (re-matches #"^\[.*?\]$" s) :vector
                              )) (range (rand-int 20))))


(= :map (#(let [s (str %)] (cond
                             (re-matches #"^\{.*?\}$" s) :map
                             (re-matches #"^\(.*?\)$" s) :list
                             (re-matches #"^#\{.*?\}$" s) :set
                             (re-matches #"^\[.*?\]$" s) :vector
                             )) {:a 1, :b 2}))

(#(let [s (str %)] (cond
                   (re-matches #"^\{.*?\}$" s) :map
                   (re-matches #"^\(.*?\)$" s) :list
                   (re-matches #"^#\{.*?\}$" s) :set
                   (re-matches #"^\[.*?\]$" s) :vector
                   )))

(merge-with into [] {:aa 33} {:aa 44} {:bb 44} {:cc 44})



(= ((fn [f coll] (apply merge-with concat (map #(hash-map (f %) [%]) coll))) #(> % 5) [1 3 6 8]) {false [1 3], true [6 8]})


(concat [:a] [:b] [:c])


(assoc-in {} [:aa] 33)
(conj [1] 3)
({:aa 33} :aa)
(:aa {:aa 33})

(assoc {:aaa} :aaa 33)

(update)
(update-in {:aaa} [:aaa] conj 33)
(update)

(update-in)
(assoc-in {:aaa []} [:aaa 0] 333)



(= ((fn [a b] (apply hash-map (interleave a b))) [:a :b :c] [1 2 3]) {:a 1, :b 2, :c 3})

(apply hash-map '(1 2 3 4))

(defn haha [f coll]
  (lazy-seq (if (and (first coll) (second coll)) (cons (f (first coll) (second coll)) (haha (f (first coll) (second coll)) (rest (rest coll)))))))

(haha + [1 2 3 4 ])

(fn my-fn
  ([f coll]
   (my-fn f (first coll) (rest coll)))
  ([f r coll]
   (lazy-seq (cons r (if (and coll (first coll)) (my-fn f (f r (first coll)) (rest coll)))))))

(take 5 (fib2 + (range)))

(= (take 5 (haha + (range 10))) [0 1 3 6 10])

(cons 3 nil)

(def guys-name-map
  {:f-name "Guy" :m-name "Lewis" :l-name "Steele"})

(let [{f-name :f-name m-name :m-name l-name :l-name} guys-name-map]
  (println f-name " " m-name " " l-name))


(def guys-name-map2
  {"f-name" "Guy" "m-name" "Lewis" "l-name" "Steele"})


(let [{:strs [f-name m-name l-name]} guys-name-map2]
  (println f-name " " m-name " " l-name))

(def guys-name-map3
  {'f-name "Guy" 'm-name "Lewis" 'l-name "Steele"})

(def guys-name-map4
  {f-name "guy"})

(let [:syms [f-name m-name l-name] guys-name-map3]
  (println f-name " " m-name " " l-name))

(let [{aa 1 bb 2 :as all} [11 22 33 44]]
  (println aa " " bb)
  (println all))

(defn hello [{:keys [l-name]}]
  (println l-name))

(hello guys-name-map)