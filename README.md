版本标记
========

- v2.0.0-0.1.0 对应以前的 v2.0.0.B1 标记
- v2.0.0-0.2.0 对应以前的 fault-record-support 分支
- v2.0.0-0.3.0 对应以前的 csg-ehvtsq-safety-education-v0.8.0.B1 标记
- v2.0.0-0.4.0 对应以前的 before-guyong-leave 分支
- v2.0.0-0.5.0 对应以前的 coala 标记

发布
====

具体发布过程请参考 [zyeeda/origin 项目](https://bitbucket.org/zyeeda/origin)。

在执行发布的时候为了避免运行测试用例，可以添加 `-Darguments="-DskipTests"` 参数，如下：

```bash
mvn release:prepare -Darguments="-DskipTests"
mvn release:perform -Darguments="-DskipTests" -Pinternal-release
```

