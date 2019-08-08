(ns ablog.core
  (:require [markdown.core :as md]
            [clojure.java.io :as io]
            [clj-time.format :as clj-time-format]
            [clj-time.core :as clj-time-core]
            [clj-time.coerce :as clj-time-coerce]
            [selmer.parser :refer [render render-file]]

            ))




; 默认网站参数
(def default-settings
  {
   :site-title                "a git blog"
   :posts-dir                 "posts"
   :pages-dir                 "pages"
   :public-dir                "public"
   :valid-filename-ext        #{"md" "html" "gdb"}
   :theme                     "default"
   :post-date-format          "yyyy-MM-dd HH:mm"
   :post-filename-date-format "yyyyMMddHHmm"
   :posts-out-dir             "p"
   :post-permalink            ":year/:month/:day/:title"
   :public-keep-files         ["static" "pages" "posts" "CNAME" ".git"]
   })

; 默认post参数
(def default-post-settings
  {
   :title         nil                                       ; 文章标题
   :description   ""                                        ; 文章描述 可以用于站点优化
   :keywords      ""                                        ; 文章关键词 用于站点优化
   :post-date     nil                                       ; 发布日期 或者发布日期+时间 创建日期
   :last-modified nil                                       ; 最后修改时间
   :updated       nil                                       ; 更新日期，最后一次修改时间
   :layout        "post"                                    ; 对应布局，目前有两种 post page
   :slug          nil                                       ;文章标题形成的文件名和网址，设置了，那么 post 文件名的就不管用了，方便中文的管理 "spf13-vim-3-0-release-and-new-website"
   :draft         nil                                       ; 是否是草稿，如果是，就不会生成html页面
   :categories    []                                        ; 文章分类
   :tags          []                                        ; 对应标签
   :comments      false                                     ; 是否开启文章评论功能
   :author        ""                                        ;文章作者, 一位，主作者
   :authors       []                                        ; 文章作者，多位
   ; 还可以增加自定义变量
   })


; 公共映射目录
(def *posts-out-dir (atom (:posts-out-dir default-settings)))
; post 分类
(def *post-categories (atom {}))
; post 标签
(def *post-tags (atom {}))



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


(defn is-theme-exists
  [settings f]
  (.exists (io/as-file (str "theme/" (:theme settings) "/" f)))
  )

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
              (if-not (or (= f public-folder2) (some (fn [k] (= 0 (clojure.string/index-of f k))) new-keep-files))
                (do
                  (delete-dir f)))) public-files))))

(defn copy-file
  "复制文件"
  [source-path dest-path]
  (clojure.java.io/make-parents dest-path)
  (io/copy (io/file source-path) (io/file dest-path)))


(defn copy-dir
  "把文件夹拷贝到对应的目录"
  [src target ignored-files]
  (doall
    (pmap #(let [filepath-str (str %)
                 file-rel-path (subs filepath-str (count src))
                 file-target-path (str target file-rel-path)]
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
  (try
    (clj-time-format/parse multi-parser time-str)
    (catch Exception e nil)))

(defn get-filename [file]
  (rtrim (clojure.string/replace (.getName file) #"^[\d\-]+-" "") ".md"))

(defn get-post-date
  "获取 post 提交时间"
  [settings post-config file default-time]
  (let [write-date (:post-date post-config)
        file-date (second (re-find #"^([\d\-]+)-(.*?)\.md$" (.getName file)))
        d (time-formater (if write-date write-date file-date))]
    (if d d default-time)))



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


(defn get-category-url
  [settings category-name]
  (str "/" @*posts-out-dir "/cat/" category-name)
  )


(defn get-tag-url
  [settings tag-name]
  (str "/" @*posts-out-dir "/tag/" tag-name)
  )

(defn get-post-filename
  "获取post文件名"
  [slug filename]
  (let [new-slug (if (and slug (re-find #"[\w\d]+" slug)) slug)
        new-filename (rtrim filename ".md")]
    (-> (if slug slug new-filename)
        (clojure.string/trim)
        (clojure.string/replace #"\s+" "-"))))




(defn get-public-post-filepath
  "获取源文件post文件对应的public文件"
  [settings post-config file post-time]
  (str (rtrim (:public-dir settings) "/")
       "/"
       (trim @*posts-out-dir "/")
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


(defn get-public-page-filepath
  "获取源文件page文件对应的public文件"
  [settings post-config file post-time]

  (str (rtrim (:public-dir settings) "/")
       "/"
       (trim (:page-out-dir settings) "/")
       "/"
       (let [f (reduce (fn [s [key val]] (clojure.string/replace s (re-pattern (str key)) (str val)))
                       (:page-permalink settings)
                       {:year  (str (clj-time-core/year post-time))
                        :month (str (clj-time-core/month post-time))
                        :day   (str (clj-time-core/day post-time))
                        :title (get-post-filename (:slug post-config) (get-filename file))})]
         (trim f "/"))
       "/index.html")
  )

(defn get-last-modified
  [file]
  (clj-time-coerce/from-long (.lastModified (clojure.java.io/file file)))
  )


(defn parse-post
  "获取post的相关值"
  ([settings file] (parse-post settings file "post"))
  ([settings file ftype]
   (if (is-valid-file settings file)
     (let [last-modified (get-last-modified file)
           rdr (clojure.java.io/reader file)
           post-config (try (read (java.io.PushbackReader. rdr))
                            (catch Exception e nil)
                            )
           categories (:categories post-config)

           categories2 (for [x categories] {:name x :url (get-category-url settings x)})
           tags (:tags post-config)
           tags2 (for [x tags] {:name x :url (get-tag-url settings x)})
           post-content (md/md-to-html-string
                          (if (= (type post-config) clojure.lang.PersistentArrayMap)
                            (slurp rdr)
                            (slurp file))
                          )
           post-filename (get-filename file)
           post-title (get-post-title post-config file)
           post-date (get-post-date settings post-config file last-modified)
           _ (println "post-date:" post-date " t:" post-title last-modified ":" (type last-modified))
           post-filepath (if (= ftype "page") (get-public-page-filepath settings post-config file post-date) (get-public-post-filepath settings post-config file post-date))
           post-url (get-post-url settings post-filepath)
           post-authors (get-post-authors settings post-config)
           post-author (first post-authors)

           post {:content post-content :post-date post-date :last_modified last-modified :filename post-filename :filepath post-filepath :url post-url :title post-title :author post-author :authors post-authors :categories categories2 :tags tags2}
           ]

       (doseq [x categories]
         (if x
           (swap! *post-categories update-in [x] conj post)
           )
         )

       (doseq [x tags]
         (if x
           (swap! *post-tags update-in [x] conj post)
           )
         )


       (if (not (:draft post-config))
         post
         )))))



(defn get-posts-list
  "获取所有的post文件列表，返回的是：
  文章内容
  文章标题
  文章时间
  其它变量的一个map组成的列表"
  [settings folder-key]
  (->> (file-seq (clojure.java.io/file (folder-key settings)))
       (map #(parse-post settings %))
       (filter not-empty)
       ))



(defn get-page-list
  "获取所有的page文件列表，返回的是：
  文章内容
  文章标题
  文章时间
  其它变量的一个map组成的列表"
  [settings]
  (if (.exists (io/as-file (:pages-dir settings)))
    (->> (file-seq (clojure.java.io/file (:pages-dir settings)))
         (pmap #(parse-post settings % "page"))
         (filter not-empty)
         (sort #(compare (:post-date %2) (:post-date %1))))))


(defn generate-post-list
  "生成post列表页"
  [settings [current-page posts] page-numbers template_filename]
  (if (is-theme-exists settings template_filename)
    (let [prev-page (if (= current-page 1) current-page (dec current-page))
          next-page (if (= current-page (count page-numbers)) current-page (inc current-page))
          list-html (render-file template_filename {:posts posts :prev-page prev-page :current-page current-page :page-numbers page-numbers :next-page next-page :total-page-count (count page-numbers) :site-title (:site-title settings) :posts-out-dir @*posts-out-dir})
          page-link (if (> current-page 1)                  ; 如果第一页，那么就直接/list/
                      (str "/list/page/" current-page)
                      "/list"
                      )
          list-filepath (str (rtrim (:public-dir settings) "/") "/" @*posts-out-dir page-link "/index.html")
          ]
      (clojure.java.io/make-parents list-filepath)
      (spit list-filepath list-html)
      )))

(defn generate-cat-post-list
  "生成category列表页"
  [settings [current-page posts] page-numbers template_filename]
  (if (is-theme-exists settings template_filename)
    (let [prev-page (if (= current-page 1) current-page (dec current-page))
          next-page (if (= current-page (count page-numbers)) current-page (inc current-page))
          list-html (render-file template_filename {:posts posts :prev-page prev-page :current-page current-page :page-numbers page-numbers :next-page next-page :total-page-count (count page-numbers) :site-title (:site-title settings) :posts-out-dir @*posts-out-dir})
          page-link (if (> current-page 1)                  ; 如果第一页，那么就直接/list/
                      (str "/cat/" current-page)
                      "/cat"
                      )
          list-filepath (str (rtrim (:public-dir settings) "/") "/" @*posts-out-dir page-link "/index.html")
          ]
      (clojure.java.io/make-parents list-filepath)
      (spit list-filepath list-html)
      )))


(defn generate-readme
  "生成README.md"
  [settings posts template_filename]
  (if (is-theme-exists settings template_filename)
    (let [html-data (render-file template_filename {:posts posts :total-post-count (count posts) :site-title (:site-title settings) :posts-out-dir @*posts-out-dir})
          list-filepath (str (rtrim (:public-dir settings) "/") "/" template_filename)
          ]
      (clojure.java.io/make-parents list-filepath)
      (spit list-filepath html-data)
      )))



(defn generate-page
  "生成page页"
  [settings [prev-page page next-page] pages template_filename]
  (if (is-theme-exists settings template_filename)
    (let [
          html-data (render-file template_filename (assoc page :prev-page prev-page :next-page next-page :site-title (:site-title settings) :total-page-count (count pages) :posts-out-dir @*posts-out-dir))
          filepath (str (rtrim (:public-dir settings) "/") "/" @*posts-out-dir "/" (:filename page) "/index.html")
          ]
      (clojure.java.io/make-parents filepath)
      (println "page-path:" filepath)
      (spit filepath html-data)
      )))


(defn generate-post
  "为相应的模板生成页面"
  [settings [prev-post post next-post] template_filename]
  (let [new-post (assoc post :prev-post prev-post :next-post next-post :site-title (:site-title settings) :posts-out-dir @*posts-out-dir :all-tags @*post-tags :all-categories @*post-categories)
        post-html (render-file template_filename new-post)]
    (clojure.java.io/make-parents (:filepath post))
    (println "post-path:" (:filepath post))
    (spit (:filepath post) post-html)))



(defn generate-homepage
  "生成首页"
  [settings [prev-post post next-post]]
  (generate-post settings [prev-post (assoc post :filepath (str (rtrim (:public-dir settings) "/") "/index.html")) next-post] "post.html"))


(def is-local-run 1)


(defn generate
  "整站生成静态网站
  gtype 表示是本地生成还是服务器端生成
  dev 如果本地生成，那么就可以显示private
  pub 如果要生成给服务器，那么就不显示private
  "
  [gtype]
  (let [settings (get-settings)
        _
        (if (= gtype "dev")
          (reset! *posts-out-dir (:private-posts-out-dir settings))
          (reset! *posts-out-dir (:posts-out-dir settings))
          )

        _ (selmer.parser/set-resource-path! (str (System/getProperty "user.dir") "/theme/" (:theme settings)))
        public-posts (get-posts-list settings :posts-dir)
        private-posts
        (if (= gtype "dev")
          (get-posts-list settings :private-posts-dir))
        pages (get-page-list settings)

        posts (sort #(compare (:post-date %2) (:post-date %1)) (concat public-posts private-posts))

        ; 博客一页一页的
        post-part-list (partition 3 1 (lazy-cat [nil] posts [nil]))
        page-part-list (partition 3 1 (lazy-cat [nil] pages [nil]))
        ; 列表分页
        post-list-list (map #(vector (inc %1) %2) (range) (partition-all 15 posts))

        ; 页数
        page-count (count post-list-list)
        page-numbers (range 1 (inc page-count))
        ]

    ; 清空目录
    (wipe-public-folder (:public-dir settings) (:public-keep-files settings))
    ; 生成首页
    (generate-homepage settings (first post-part-list))
    ; 复制皮肤相关资源到目标目录
    (copy-resources-from-theme settings)

    (doall
      ; 生成每一个post
      (map #(generate-post settings % "post.html") post-part-list))

    (if (seq page-part-list)
      (doall
        ; 生成pages
        (map #(generate-page settings % pages "page.html") page-part-list)))

    (doall
      ; 生成所有文章列表页
      (map #(generate-post-list settings % page-numbers "list.html") post-list-list))

    ; 生成readme
    (generate-readme settings posts "README.MD")


    ; 生成分类页
    (doseq [[cat_name posts] @*post-categories]
      (let [cat-part-posts (map #(vector (inc %1) %2) (range) (partition-all 15 posts))]
        (doall
          ; 生成所有分类列表页
          (pmap #(generate-cat-post-list settings % page-numbers "list.html") post-list-list))
        )
      )


    ; 生成标签页
    )
  )





