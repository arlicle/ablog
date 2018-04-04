(ns ablog.build-test
    (:require [clojure.test :refer :all]
              [ablog.build :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))


(deftest get-file-ext-test
  (testing "获取文件后缀名失败"
    (is (= "html" (get-file-ext "aaa.html")))))