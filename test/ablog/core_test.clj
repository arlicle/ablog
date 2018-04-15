(ns ablog.core-test
    (:require [clojure.test :refer :all]
              [ablog.core :refer :all]
              [clj-time.core :as clj-time-core]
              [clj-time.local :as l]))

(def settings (get-settings))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest is-valid-file-test
  (testing "有效文件判断失败"
    (is (= "md" (is-valid-file settings "hello.md")))
    (is (= "html" (is-valid-file settings "hello.html")))
    (is (= nil (is-valid-file settings "hello.doc")))
    ))

(deftest get-file-ext-test
  (testing "获取文件后缀名失败"
    (is (= "html" (get-file-ext "aaa.html")))
    (is (= "md" (get-file-ext "aaa.md")))
    ))

(comment
(deftest time-formater-test
  (testing "时间日期格式化失败"
    (is (= (clj-time-core/date-time 2017 10 15) (time-formater "20171015")))
    (is (= (clj-time-core/date-time 2018 4 4 13 16) (time-formater "20180404 13:16")))
    (is (= (clj-time-core/date-time 2017 10 15) (time-formater "2017-10-15")))
    (is (= (clj-time-core/date-time 2017 3 3) (time-formater "2017-3-3")))
    (is (= (clj-time-core/date-time 2018 4 4 13 16) (time-formater "2018-04-04 13:16")))
    ))
    )
    