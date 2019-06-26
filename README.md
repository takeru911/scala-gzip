## scala-gzip

### 概要

gzipファイルを読み込んで、圧縮したり解凍したりするやつ。

### 実装したこと

* ファイルのbyteから
  * gzip formatであるか読む
  * zlibを使って圧縮/解凍する
  * CRC32より展開した内容とgzipファイルのチェックサムを確認する

### 実装したいこと

* 圧縮・解凍の実装をzlibでなく手前実装にする

### 動かし方

* sbt, javaが入っていること前提

* 圧縮

```
$ cat compress.txt
abcdefghijk

$ make compress FILE=compress.txt
sbt "run -c compress.txt"
[info] Loading project definition from /mnt/c/Users/taker/IdeaProjects/gzip/project
[info] Loading settings for project gzip from build.sbt ...
[info] Set current project to gzip (in build file:/mnt/c/Users/taker/IdeaProjects/gzip/)
[info] Running Gzip -c compress.txt
[success] Total time: 1 s, completed 2019/06/26 17:23:42$ make compress FILE=compress.txt

$ zcat compress.txt.gz
abcdefghijk
```

* 解凍

```
$ zcat test.txt.gz
abcdefg

$ make decompress FILE=test.txt.gz
sbt "run -d test.txt.gz"
[info] Loading project definition from /mnt/c/Users/taker/IdeaProjects/gzip/project
[info] Loading settings for project gzip from build.sbt ...
[info] Set current project to gzip (in build file:/mnt/c/Users/taker/IdeaProjects/gzip/)
[info] Running Gzip -d test.txt.gz
abcdefg
[success] Total time: 2 s, completed 2019/06/26 17:25:38
```

### 参考にしたもの

* https://www.futomi.com/lecture/japanese/rfc1952.html
