(ns ablog.build
  (:require [markdown.core :as md]
    [boot.core :as core]
    [clojure.java.io :as io]
    [clj-time.format :as clj-time-format]
    [selmer.parser :refer [render render-file]]))

(selmer.parser/set-resource-path! (System/getProperty "user.dir"))



; 默认网站参数
(def default-settings {
  :site-title "a git blog"
  :posts-dir "posts"
  :page-dir "pages"
  :public-dir "public"
})

; 获取网站参数
(defn get-settings
  "获取网站的各项设置"
  []
  (merge default-settings (read-string (slurp "settings.ini"))))


(defn get-posts-list
  "获取所有的post文件列表"
  [settings]
  (file-seq (clojure.java.io/file (:posts-dir settings))))

; 获取皮肤
(defn get-theme 
  "获取当前的皮肤"
  [settings]
  (:theme settings "default"))

; 获取对应皮肤下的模板路径
(defn get-theme-file-path
  "获取皮肤文件地址"
  [settings template-filename]
  (str "theme/" (get-theme settings) "/" template-filename))

  
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

; (deftask hello []
;   (println "Hello world!******!"))
; 用模板渲染内容

(defn parse-file
  "把所有的markdown文档找到然后编译为html"
  []
  (doseq [f (only-md-files posts-list)]
    (try (let [rdr (clojure.java.io/reader f)
        post-config (read (java.io.PushbackReader. rdr))
        post-content (line-seq (java.io.BufferedReader. rdr))
        [_ post-time-int post-filename] (re-find #"^(\d+)-(.*?)\.md$" (.getName f))
        post-filepath (str "public/" post-filename ".html")
        ;f-html-path (str "public/" (clojure.string/replace (.getName f) #"^(\d+)-(.*?)\.md$" "$2.html"))
        post-html (render-file "theme/post.html" {:post-title (:title post-config) 
            :post-content (md/md-to-html-string (clojure.string/join "\n" post-content))
            :post-time (clj-time-format/parse (clj-time-format/formatter "yyyyMMdd") "20101211")})
        ]

        (println post-config)
        (println post-filepath)
        ; (println post-time)
        (println post-filename)
        (println _)
        (println (clj-time-format/parse (clj-time-format/formatter "yyyyMMdd") "20101211"))
        (spit post-filepath post-html)
        )
    (catch Exception e (do (println (.toString f) (println e)))))
    (println f)))


