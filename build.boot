(set-env!
 :source-paths #{"src"}
 :resource-paths #{"theme"}
 :dependencies '[[adzerk/boot-cljs "2.1.4"]
                 [pandeiro/boot-http "0.8.3"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [adzerk/boot-reload "0.5.2"]
                 [markdown-clj "1.0.1"]
                 [clj-time "0.14.2"]
                 [selmer "1.11.7"]
                 [adzerk/boot-test "1.2.0"]
                 [seancorfield/boot-expectations "1.0.11"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[ablog.build :refer :all]
         '[adzerk.boot-test :refer :all]
         )

(deftask parse
  "just a test"
  []
  (generate)
  )


(deftask dev
  "run the blog server"
  []
  (comp
   (serve :dir "public")
   (watch)
   ;(hello)
   ;(cljs)
   ;(target :dir #{"public"})
   ))


