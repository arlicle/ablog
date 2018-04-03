(ns ablog.build
  (:require [markdown.core :as md]
    [boot.core :as core]
    [clojure.java.io :as io]
    [clj-time.format :as clj-time-format]
    [clj-time.core :as clj-time-core]
    [selmer.parser :refer [render render-file]]))

(selmer.parser/set-resource-path! (System/getProperty "user.dir"))



; 默认网站参数
(def default-settings {
  :site-title "a git blog"
  :posts-dir "posts"
  :page-dir "pages"
  :public-dir "public"
  :valid-filename-ext #{"md" "html" "gdb"}
  :theme "default"
  :post-date-format "yyyy-MM-dd HH:mm"
  :post-filename-date-format "yyyyMMddHHmm"
  :post-permalink ":year/:month/:day/:title/"
})

; 获取网站参数
(defn get-settings
  "获取网站的各项设置"
  []
  (merge default-settings (read-string (slurp "settings.ini"))))

(def posts-list (file-seq (clojure.java.io/file "posts")))

(defn get-file-ext 
  "获取文件后缀名"
  [filename]
  (->> filename
    (#(.lastIndexOf % "."))
    (#(inc %))
    (subs filename)))

(defn is-valid-file
  "是否是有效的模板文件"
  [settings file]
  (get (:valid-filename-ext settings) (get-file-ext (str file))))

(defn time-formater
  [format-str time-str]
  (-> (subs format-str 0 (count time-str))
      (clj-time-format/formatter)
      (#(clj-time-format/parse % time-str))))

(defn get-post-date
  "获取 post 提交时间"
  [settings post-config file]
  (if-let [write-date (:date post-config)]
    (time-formater (:post-date-format settings) write-date)
    (->> (re-find #"^(\d+)-(.*?)\.md$" (.getName file))
        (second)
        (time-formater (:post-filename-date-format settings))
    )))

(defn get-post-url
  "获取 post 的对外访问的 url"
  [settings post-filepath]
  (subs post-filepath (count (:public-dir settings))))



; 获取皮肤
(defn get-theme 
  "获取当前的皮肤"
  [settings]
  (:theme settings "default"))

; 获取对应皮肤下的模板路径
(defn get-theme-filepath
  "获取皮肤文件地址"
  [settings template-filename]
  (str "theme/" (get-theme settings) "/" template-filename))

(defn get-public-post-filepath
  "获取源文件文件对应的public文件"
  [settings file post-time]
  (str (:public-dir settings)
       "/" 
       (let [f (reduce (fn [s [key val]] (clojure.string/replace s (re-pattern (str key)) (str val))) 
        (:post-permalink settings) {:year (str (clj-time-core/year post-time)) 
          :month (str (clj-time-core/month post-time)) 
          :day (str (clj-time-core/day post-time)) 
          :title (.getName file)})]
          (subs f 0 (clojure.string/last-index-of f "."))
       )
       ".html"))



(defn parse-post
  "获取post的相关值"
  [settings file]
  (if (is-valid-file settings file)
    (let [rdr (clojure.java.io/reader file)
          post-config (read (java.io.PushbackReader. rdr))
          post-content (line-seq (java.io.BufferedReader. rdr))
          post-date (get-post-date settings post-config file)
          post-filepath (get-public-post-filepath settings file post-date)
          post-url (get-post-url settings post-filepath)
          ]
          {:content post-content :date post-date :filepath post-filepath :url post-url 
          :title (:title post-config)}
      )
))

(defn get-posts-list
  "获取所有的post文件列表，返回的是：
  文章内容
  文章标题
  文章时间
  其它变量的一个map组成的列表"
  [settings]
  ; (doall
  (->> (file-seq (clojure.java.io/file (:posts-dir settings)))
        (pmap #(parse-post settings %))
        (filter not-empty)
        (sort-by :date)
  ))
  ; )



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


(defn get-file-interpretor
  "获取文件解释器，如果是不合法的文件，返回nil"
  [file]
)

;获取上一篇文章
;获取下一篇文章
(defn generate-post
  "只为一篇当前修改的文章生成静态页面"
  [settings [prev-post post next-post]]
  (let [new-post (assoc post :prev-post prev-post :next-post next-post) 
    post-html (render-file (str "theme/" (:theme settings) "/post.html") new-post)]
    (println post-html)
    (println (:date post))
    (println (:filepath post))
    (println (:title post))
    (clojure.java.io/make-parents (:filepath post))
    (spit (:filepath post) post-html))
)

(defn generate
  "整站生成静态网站"
  []
  (let [settings (get-settings)
        posts-list (partition 3 1 (lazy-cat [nil] (get-posts-list settings) [nil]))
    ]
    ; (doall
      (pmap #(generate-post settings %) posts-list)
    ; )
    ))



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


