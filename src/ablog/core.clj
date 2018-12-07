(ns ablog.core
  (:require [markdown.core :as md]
            [clojure.java.io :as io]
            [clj-time.format :as clj-time-format]
            [clj-time.core :as clj-time-core]
            [selmer.parser :refer [render render-file]]
            ))




; 默认网站参数
(def default-settings
  {
   :site-title                "a git blog"
   :posts-dir                 "posts"
   :page-dir                  "pages"
   :public-dir                "public"
   :valid-filename-ext        #{"md" "html" "gdb"}
   :theme                     "default"
   :post-date-format          "yyyy-MM-dd HH:mm"
   :post-filename-date-format "yyyyMMddHHmm"
   :posts-out-dir             "p"
   :post-permalink            ":year/:month/:day/:title"
   :public-keep-files         ["static"]
   })

; 默认post参数
(def default-post-settings
  {
   :title       nil ; 文章标题
   :description "" ; 文章描述 可以用于站点优化
   :keywords    "" ; 文章关键词 用于站点优化
   :post-date   nil ; 发布日期 或者发布日期+时间 创建日期
   :last-modified nil                                       ; 最后修改时间
   :updated     nil ; 更新日期，最后一次修改时间
   :layout      "post" ; 对应布局，目前有两种 post page
   :slug        nil ;文章标题形成的文件名和网址，设置了，那么 post 文件名的就不管用了，方便中文的管理 "spf13-vim-3-0-release-and-new-website"
   :draft       nil ; 是否是草稿，如果是，就不会生成html页面
   :categories  [] ; 文章分类
   :tags        [] ; 对应标签
   :comments    false ; 是否开启文章评论功能
   :author      ""                                                ;文章作者, 一位，主作者
   :authors     [] ; 文章作者，多位
   ; 还可以增加自定义变量
   })

; 获取网站参数
(defn get-settings
  "获取网站的各项设置
  如果没有获得的"
  []
  (merge default-settings (try
    (read-string (slurp "settings.ini"))
    (catch Exception e nil))))

(defn rtrim
  "如果只有一个 s 参数，那么只是清空空格
  如果有两个参数，则去掉后一个参数"
  ([s] (clojure.string/trimr s))
  ([s k] (if-let [i (clojure.string/last-index-of s k)]
           (if (= (subs s i) k)
             (subs s 0 i)
             s) s)))


(defn ltrim
  "如果只有一个 s 参数，那么只是清空空格
  如果有两个参数，则从左边开始去掉后一个参数"
  ([s] (clojure.string/triml s))
  ([s k] (let [i (clojure.string/index-of s k)] (if (and i (= 0 i)) (subs s (count k)) s))))


(defn trim
  "如果只有一个 s 参数，那么只是清空空格
  如果有两个参数，则去掉后一个参数"
  ([s] (clojure.string/trim s))
  ([s k] (ltrim (rtrim s k) k)))


(defn delete-dir
  "Delete a directory tree."
  [root]
  (let [root-file (io/file root)]
    (if (.isDirectory root-file)
    (doseq [path (reverse (file-seq root-file))]
      (.delete path))
    (.delete root-file))))

(defn wipe-public-folder [public-folder keep-files]
  ; 晴空目录
  (let [public-files (reverse (file-seq (io/file public-folder))) public-folder2 (rtrim public-folder "/") new-keep-files (map #(str public-folder2 "/" %) keep-files)]
    (doall
      (map #(let [f (str %)]
        (if-not (or (= f public-folder2) (some (fn [k] (= 0 (clojure.string/index-of f k))) new-keep-files) )
          (do
            ; (println "delete : " f)
            (delete-dir f)) )) public-files))))

(defn copy-file
  "复制文件"
  [source-path dest-path]
  (clojure.java.io/make-parents dest-path)
  (io/copy (io/file source-path) (io/file dest-path)))


(defn copy-dir
  "把文件夹拷贝到对应的目录"
  [src target ignored-files]
  (println "src:" src)
  (doall
    (pmap #(let [filepath-str (str %)
                 file-rel-path (subs filepath-str (count src))
                 file-target-path (str target file-rel-path)]
             (println "filepath-str:" filepath-str)
             (copy-file filepath-str file-target-path)) (filter #(.isFile %) (file-seq (clojure.java.io/file src))))))

(defn copy-resources-from-theme
  "复制模板的资源到对外的目录"
  [settings]
  (doall
    (map #(copy-dir (str "theme/" (:theme settings) "/" %) (str (:public-dir settings) "/" %) nil) ["css" "images" "img" "js"])))

(defn get-file-ext
  "获取文件后缀名"
  [filename]
  (->> filename
       (#(.lastIndexOf % "."))
       (#(inc %))
       (subs filename)))



(defn is-valid-file
  "是否是有效的模板文件，返回模板后缀名"
  [settings file]
  (get (:valid-filename-ext settings) (get-file-ext (str file))))



; 时间输入格式
(def multi-parser (clj-time-format/formatter (clj-time-core/default-time-zone) "yyyy-MM-dd" "yyyy/MM/dd" "dd/MM/yyyy" "yyyy-MM-dd HH:mm" "yyyy/MM/dd HH:mm" "dd/MM/yyyy HH:mm" "yyyyMMdd HH:mm" "yyyyMMddHHmm" "yyyyMMdd" "yyyyMMddHH"))



(defn time-formater
  [time-str]
  (clj-time-format/parse multi-parser time-str))

(defn get-filename [file]
  (rtrim (clojure.string/replace (.getName file) #"^[\d\-]+-" "") ".md"))

(defn get-post-date
  "获取 post 提交时间"
  [settings post-config file]
  (if-let [write-date (:post-date post-config)]
    (time-formater write-date)
    (->> (re-find #"^([\d\-]+)-(.*?)\.md$" (.getName file))
         (second)
         (time-formater))))



(defn get-post-title
  "获取post标题"
  [post-config file]
  (if (and (= (type post-config) clojure.lang.PersistentArrayMap) (:title post-config))
    (:title post-config)
    (get-filename file)))



(defn get-post-authors
  "获取 post 作者"
  [settings post-config]
  (let [authors1 (:authors post-config)
        authors2 (:authors settings)
        authors
        (cond
          (and authors1 (seq authors1))
          authors1

          (and authors2 (seq authors2))
          authors2
          )]
    (cond
      (nil? authors)
      nil

      (string? authors)
      [authors]

      :else
      authors
      )))


(defn get-post-url
  "获取 post 的对外访问的 url"
  [settings post-filepath]
  (-> post-filepath
      (ltrim (:public-dir settings))
      (rtrim "index.html")
      (ltrim "/")
      (#(str "/" %))))


(defn get-post-filename
  "获取post文件名"
  [slug filename]
  (let [new-slug (if (and slug (re-find #"[\w\d]+" slug)) slug)
        new-filename (rtrim filename ".md")]
    (-> (if slug slug new-filename)
        (clojure.string/trim)
        (clojure.string/replace #"\s+" "-"))))


;(defn get-public-post-filepath
;  "获取源文件文件对应的public文件"
;  [settings post-config file post-time]
;  (let [a
;        (->
;          (str (rtrim (:public-dir settings) "/")
;               "/"
;               (let [f (reduce (fn [s [key val]] (let [k (clojure.string/replace s (re-pattern (str key)) (str val))]
;                                                   (println "ssss:" k)
;                                                   k
;
;                                                   ))
;                               (:post-permalink settings)
;                               {:year  (str (clj-time-core/year post-time))
;                                :month (str (clj-time-core/month post-time))
;                                :day   (str (clj-time-core/day post-time))
;                                :title (get-post-filename (:slug post-config) (get-filename file))})]
;                 (rtrim f "/"))
;               "/index.html"))]
;    (println "a:" a)
;    a
;    ))


(defn get-public-post-filepath
  "获取源文件文件对应的public文件"
  [settings post-config file post-time]

  (str (rtrim (:public-dir settings) "/")
       "/"
       (trim (:posts-out-dir settings) "/")
       "/"
       (let [f (reduce (fn [s [key val]] (clojure.string/replace s (re-pattern (str key)) (str val)))
                       (:post-permalink settings)
                       {:year  (str (clj-time-core/year post-time))
                        :month (str (clj-time-core/month post-time))
                        :day   (str (clj-time-core/day post-time))
                        :title (get-post-filename (:slug post-config) (get-filename file))})]
         (trim f "/"))
       "/index.html")
  )





(defn parse-post
  "获取post的相关值"
  [settings file]
  (if (is-valid-file settings file)
    (let [last-modified (.lastModified (clojure.java.io/file file))
          rdr (clojure.java.io/reader file)
          post-config (try (read (java.io.PushbackReader. rdr))
                           (catch Exception e nil)
                           )
          post-content (md/md-to-html-string
                         (if (= (type post-config) clojure.lang.PersistentArrayMap)
                          (slurp rdr)
                          (slurp file)))
          post-title (get-post-title post-config file)
          post-date (get-post-date settings post-config file)
          post-filepath (get-public-post-filepath settings post-config file post-date)
          post-url (get-post-url settings post-filepath)
          post-authors (get-post-authors settings post-config)
          post-author (first post-authors)
          ]
      (if (not (:draft post-config))
        {:content post-content :post-date post-date :last_modified last-modified :filepath post-filepath :url post-url :title post-title :author post-author :authors post-authors}))))



(defn get-posts-list
  "获取所有的post文件列表，返回的是：
  文章内容
  文章标题
  文章时间
  其它变量的一个map组成的列表"
  [settings]
  (->> (file-seq (clojure.java.io/file (:posts-dir settings)))
       (pmap #(parse-post settings %))
       (filter not-empty)
       (sort #(compare (:post-date %2) (:post-date %1)))))




(defn generate-post-list
  "生成post列表页"
  [settings [current-page posts] pages template_filename]
  (let [prev-page (if (= current-page 1) current-page (dec current-page))
        next-page (if (= current-page (count pages)) current-page (inc current-page))
        list-html (render-file template_filename {:posts posts :prev-page prev-page :current-page current-page :pages pages :next-page next-page :total-page-count (count pages) :site-title (:site-title settings)})
        page-link (if (> current-page 1) ; 如果第一页，那么就直接/list/
                    (str "/list/page/" current-page)
                    "/list/"
                    )
        list-filepath (str (rtrim (:public-dir settings) "/") "/" (:posts-out-dir settings) page-link "/index.html")
        ]
    (clojure.java.io/make-parents list-filepath)
    (spit list-filepath list-html)
    ))




(defn generate-post
  "为相应的模板生成页面"
  [settings [prev-post post next-post] template_filename]
  (let [new-post (assoc post :prev-post prev-post :next-post next-post :site-title (:site-title settings))
        post-html (render-file template_filename new-post)]
    (clojure.java.io/make-parents (:filepath post))
    (println "post-path:" (:filepath post))
    (spit (:filepath post) post-html)))



(defn generate-homepage
  "生成首页"
  [settings [prev-post post next-post]]
  (generate-post settings [prev-post (assoc post :filepath (str (rtrim (:public-dir settings) "/") "/index.html")) next-post] "post.html"))



(defn generate
  "整站生成静态网站"
  []
  (let [settings (get-settings)
        _ (selmer.parser/set-resource-path! (str (System/getProperty "user.dir") "/theme/" (:theme settings)))
        posts (get-posts-list settings)
        ; 博客一页一页的
        post-part-list (partition 3 1 (lazy-cat [nil] posts [nil]))
        ; 列表分页
        post-list-list (map #(vector (inc %1) %2) (range) (partition-all 15 posts))
        ; 页数
        page-count (count post-list-list)
        pages (range 1 (inc page-count))
        ]
    ; 清空目录
    (wipe-public-folder (:public-dir settings) (:public-keep-files settings))
    ; 生成首页
    (generate-homepage settings (first post-part-list))
    ; 复制皮肤相关资源到目标目录
    (copy-resources-from-theme settings)

    ; 生成所有文章列表页
    (doall
      (pmap #(generate-post-list settings % pages "list.html") post-list-list))


    (doall
      ; 生成每一个post
      (pmap #(generate-post settings % "post.html") post-part-list))))





