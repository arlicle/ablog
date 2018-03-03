(ns ablog.build
  (:require [markdown.core :as md]))


(def posts-list (file-seq (clojure.java.io/file "posts")))

(defn only-md-files
  [file-s]
  ;(println file-s)
  (filter #(and (.isFile %) (.endsWith (.toString %) "md")) file-s))

(defn get_md
  [f]
  (let [f-path (.toString f)]
    (println f-path)
    [f-path (clojure.string/replace f-path, #"\.md", ".html")]
  )
)

(defn parse-file
  "把所有的markdown文档找到然后编译为html"
  []
  (doseq [f (only-md-files posts-list)]
    (try (let [rdr (clojure.java.io/reader f)
        post-config (read (java.io.PushbackReader. rdr))
        post-content (line-seq (java.io.BufferedReader. rdr))
        f-html-path (str "public/" (clojure.string/replace (.getName f) #"^(\d+)-(.*?)\.md$" "$2.html"))
        f-post-path (clojure.string/replace (.toString f), #"\.md", ".html")]
        (println post-config)
        (println f-html-path)
        (spit f-html-path (md/md-to-html-string (clojure.string/join "\n" post-content)))
        )
    (catch Exception e (println e)))
    (println f)))


