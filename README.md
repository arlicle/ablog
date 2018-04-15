
## ablog

ablog是用 clojure 语言实现的静态网站生成器。简单、易用、高效、容易拓展

demo: [www.debugmyself.com](http://www.debugmyself.com)

## 快速开始

1. 环境：

需要clojure1.8+ ，boot 2.7.2+


2. 获取代码:

`git clone https://github.com/arlicle/ablog.git`

3. 进入到目录，然后运行服务器

    `boot dev`

4. 访问网址

    `http://localhost:3000`

5. 可以试着在posts文件夹中增加或者修改markdown文件，然后重新访问网址，就会看到更新。运行`boot dev`，系统会自动监测markdown文件的改动，然后自动生成html文件。因此无需重新运行，直接刷新网址就可以看到改变效果。


6. 也可以手动生成静态网站。运行命令

    `boot parse`

7. 自定义配置文件，可以自定义公共目录地址，写作地址等

复制settings_sample.ini 文件为 settings.ini，然后就可以进行自定义配置了。

# 设置自己的github网站

1. 在github中创建一个仓库，仓库名为 arlicle.github.io，其中arlicle为自己的github用户名。


2. 把网站代码和ablog代码clone下来

我自己的使用中，ablog和我的github 网站在同一级目录，因此我把网站在同一级目录下clone下来

`git clone git@github.com:arlicle/arlicle.github.io.git`
`git clone https://github.com/arlicle/ablog.git`

clone下来后，目录结构为：

`find .`

```

.
./ablog
./arlicle.github.io.git

```

两个目录的功能分别为：

* ablog : 运行服务
* arlicle.github.io.git : 自己的网站目录，自己的创作空间


3. 配置自己的网站目录

进入ablog，复制settings_sample.ini文件，然后将复制出来的文件改名为 settings.ini，
`cd ablog`
`cp settings_sample.ini settings.ini`

然后打开文件settings.ini，

修改 

:posts-dir "posts/" 为 "../arlicle.github.io/posts/"

:public-dir "public/" 为 "../arlicle.github.io/"

:post-permalink ":year/:month/:day/:title/" 为  "p/:year/:month/:day/:title/"

修改完后的 settings.ini文件为

```
{
  :site-title "a git blog"
  :posts-dir "../arlicle.github.io/posts/"
  :page-dir "pages"
  :public-dir "../arlicle.github.io/"
  :valid-filename-ext #{"md" "html" "gdb"}
  :theme "default"
  :post-date-format "yyyy-MM-dd HH:mm"
  :post-filename-date-format "yyyyMMddHHmm"
  :post-permalink "p/:year/:month/:day/:title/"
  :public-keep-files ["static" "posts" "CNAME" ".git" "post.sh"]
}
```

4. 运行

在ablog目录中，执行命令

`boot dev`

然后在浏览器中访问网址 `http://localhost:3000/` ，就可以看到自己的网站了。

5. 修改，编辑文章

可以在 arlicle.github.io 文件夹中，进行自己创作，在posts文件夹中，增加自己的 markdown文件，比如文件名为：`20180415-我的第一篇日志.md` ，其中 `20180415`为创作的日期，`我的第一篇日志` 为日志自己的标题（使用中文是为了方便自己查找和维护，最终生成的文件名还可以在文章中再进行自定义，标题名称也可以自定义）。日志内容为：

```
{
    :slug "my first post"
}

哈哈，我的第一篇日志。

```

保存文件，然后刷新,就可以看到自己的日志。:slug为自定义生成文件名，还可以自定义生成文章标题:title，更多详细设置看下面的相关说明。

把文章推送到 github，过几分钟，访问网址：'http://arlicle.github.io' ，就可以看到自己的网站了。

# 相关说明

1. posts中的 markdown 文件可以任意建立子文件夹，按照自己的想法进行管理自己是源文件
2. 每一篇文章的命名规范为 20180401-hello word.md 或者 2018-04-01-hello world.md 
3. 文章头部的信息设置参数为：

`{

  :title nil ; 文章标题

  :description "" ; 文章描述 可以用于站点优化

  :keywords "" ; 文章关键词 用于站点优化

  :date nil ; 发布日期 或者发布日期+时间 创建日期

  :updated nil ; 更新日期，最后一次修改时间

  :layout "post" ; 对应布局，目前有两种 post page 

  :slug nil ;文章标题形成的文件名和网址，设置了，那么 post 文件名的就不管用了，方便中文的管理 "spf13-vim-3-0-release-and-new-website"

  :draft false ; 是否是草稿，如果是，就不会生成html页面

  :categories [] ; 文章分类

  :tags [] ; 对应标签

  :comments false ; 是否开启文章评论功能

  :authors [] ; 文章作者

  ; 还可以增加自定义变量

}`

需要哪些设置哪些即可。这里说一下slug，因为访问网址用英文或者拼音对浏览器和搜索引擎比较友好，但是我们人自己去管理文章的时候，中文又更容易识别和看到。所以我自己使用的时候，通常文件名是这样的20180401-你好世界.md 然后在文章内部定义文章生成的slug ':slug "hello world"'，这样做即方便了自己维护，也方便了浏览器

4. 可以自由设置站点参数,在settings.ini文件中设置:

`{

  :site-title "a git blog"

  :posts-dir "posts"

  :page-dir "pages"

  :public-dir "public"

  :valid-filename-ext #{"md" "html" "ipynb"}

  :theme "default"

  :post-date-format "yyyy-MM-dd HH:mm"

  :post-filename-date-format "yyyyMMddHHmm"

  :post-permalink ":year/:month/:day/:title/"

  :public-keep-files ["static"]
  
}`

5. 如果文章中需要使用到图片或者一些静态资源，放到 public 文件夹的 static文件夹中，也可以自由设置相关文件夹，然后在 settings.ini中的 :public-keep-files 中增加相关文件夹。

## 待完成功能

* 为标签，分类，作者生成相关列表页
* 可以生成整站 pdf 或者某个分类的 pdf，或者某个 tag 的 pdf 或者某个作者的 pdf 书籍
* 也可以生成电子书格式：mobi，epub 之类的
* 可以自定义404页面
* 不仅可以做 blog，还可以用来做小型企业网站之类的。
* 插件和拓展系统
* 增加一个post引用另外一篇post 或page
* 增加一个皮肤中引用一组post或page
* 增加搜索功能
* 增加文章热加载



## Tips

I'm write it on my way learning, and maby 1 - 2 hour per day, I'm not sure when I finished it.