(set-env!
 :source-paths #{"src"}
 :dependencies '[[adzerk/boot-cljs "2.1.4"]
                 [pandeiro/boot-http "0.8.3"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 ;[adzerk/boot-reload "0.5.2"]
                 [markdown-clj "1.0.2"]
                 [clj-time "0.14.2"]
                 [selmer "1.11.7"]
                 ;[adzerk/boot-test "1.2.0"]
                 ;[seancorfield/boot-expectations "1.0.11"]
                 [metosin/bat-test "0.4.0" :scope "test"]
                 [onetom/boot-lein-generate "0.1.3" :scope "test"]
                 ])


(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[ablog.core :refer [generate get-settings]]
         ;'[adzerk.boot-reload :refer [reload]]
         ;'[adzerk.boot-test :refer :all]
         '[metosin.bat-test :refer (bat-test)])


(defn- macro-files-changed
  "获取变动的文件: 增加或修改"
  [diff]
  (->> (input-files diff)
       (by-ext ["md"])
       (map tmp-path)))


(deftask watch-generate
  []
  (let [tmp-result (tmp-dir!)
        compilers  (atom {})
        prev       (atom nil)
        prev-deps  (atom (get-env :dependencies))
        settings (get-settings)]
    (comp
      (with-pre-wrap fileset
        (let [diff          (fileset-diff @prev fileset)
              macro-changes (macro-files-changed diff)])
        (generate)
        (reset! prev fileset)
        (-> fileset commit!)))))

(deftask parse
  "generate html"
  []
(generate))

(deftask build
  "Builds an uberjar of this project that can be run with java -jar"
  []
  (comp
   (aot :namespace #{'main.entrypoint})
   (uber)
   (jar :file "project.jar" :main 'main.entrypoint)
   (sift :include #{#"project.jar"})
   (target)))



(deftask dev
  "run the blog server"
  []
  (let [settings (get-settings)
        public-dir (:public-dir settings)
        posts-dir (:posts-dir settings)]
  (set-env! :source-paths #{posts-dir})
  (comp
    (serve :dir public-dir :port 3006)
    (watch)
    ;(reload)
    (watch-generate)
    ;(cljs :compiler-options {:output-to "main.js"})
    ;(target :dir #{(str public-dir "js/")})
)))
