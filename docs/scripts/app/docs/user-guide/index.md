![中昱达](assets/images/logo.png "中昱达")

Zyeeda Framework 2.0 用户手册
=================================

Zyeeda Framework 2.0 是公司全新研发的技术平台与开发框架。在设计与研发过程中，始终秉承如下一些理念：

- *Don't make me think.* 这是本框架的核心开发理念，在任何情况下使用该框架，都不希望框架本身为开发人员带来过多思考过程。
- *One way to do it.* 在本框架的约束下，实现任何功能，都有且只有唯一的一种方式和途径。
- *Minimize your work.* 框架的封装尽量使得开发人员使用最少的代码编写最多的功能。

和 1.x 版本相比，新框架进行了重大的革新与调整，主要体现在如下几个方面：

- 集成框架抛弃 T5 IoC，而使用最新版本的 Spring Framework
- 引入服务器端 JavaScript（Server-Side JavaScript, SSJS）脚本编程环境，大部分代码修改后无需编译和重启
- 服务器端 JavaScript 使得前后端开发语言得到统一。同一种编程思想，同一套组件类库，一次学习可以前后端通用
- 微内核框架。除了必要核心代码使用 Java 编写以外，整个框架都是基于 JavaScript 开发而成
- 突出面向实体编程的思想，开发人员定义实体和 ORM 映射关系，并始终围绕实体数据进行编程与开发
- 支持动态脚手架功能，根据实体、流程配置动态生成持久化、控制与展现逻辑
- 引入约定优于配置的思想，提供了默认的代码布局风格，按照此风格摆放的代码可以自动装载和运行

服务器端 JavaScript 是本系统的最大特色之一，也是颠覆传统 Spring Struts 和 Hibernate（SSH）开发模式的主要所在，能够更多的了解与 SSJS 有关的内容，就能够更全面的理解本框架的设计思想。这里主要引入了三个服务器端 JavaScript 框架和组件：

- [Rhino](http://www.mozilla.org/rhino/ "Rhino") 是一个完全由 Java 语言写成的符合 ECMAScirpt 和 JavaScript 规范的 JavaScript 解析引擎，能够与 Java 程序无缝集成。
- [Ringo](http://ringojs.org/ "Ringo") 是一个基于 Rhino 封装的符合 [CommonJS](http://commonjs.org "CommonJS") 规范的 JavaScript 引擎。
- [Stick](https://github.com/hns/stick "Stick") 是一个基于 Ringo 的模块化 [JSGI](http://wiki.commonjs.org/wiki/JSGI/Level0/A/Draft2 "JavaScript Gate Interface") 中间件组装层和应用程序框架。


1. 准备环境
-----------

### JDK

本框架需要使用 JDK 1.6 及以上版本进行编译和运行，请确认系统中正确配置了 Java 开发和运行环境。

### Maven

[Apache Maven](http://maven.apache.org/ "Maven") 是一款基于项目对象模型（Project Object Model, POM）的项目管理工具，本框架的编译需要使用 Maven。

有关 Maven 的配置与运行，请参考[官方文档](http://maven.apache.org/download.html "Download and Install Maven")。

### Mercurial

[Mercurial](http://mercurial.selenic.com/ "Mercurial") 是一款分布式版本管理软件。如果需要获取和查看本项目的源代码，就需要了解 Mercurial 的基本使用方法。想了解有关内容请参考官方[用户手册](http://mercurial.selenic.com/guide/ "Learning Mercurial in Workflows")。

### 获取源代码

框架源代码托管于 [Bitbucket](https://bitbucket.org "Atlassian Bitbucket") 服务器，使用如下命令获取源代码（注：需要向管理员所要访问权限）：

```
hg clone https://bitbucket.org/zyeeda/zyeeda-framework-2.0
```


2. 预备知识
-----------

在开始详细介绍框架功能之前，首先需要了解一下框架引入的新技术和新概念。如果对这些内容已经掌握，可以跳过本章节。

### JavaScript

本框架和基于本框架开发的系统会大量使用 JavaScript 语言，如果不熟悉该语言，请参考如下一些学习资源：

- [JavaScript Tutorial from w3schools.com](http://www.w3schools.com/js/default.asp)
- [Learn JavaScript from Mozilla Developer Network](https://developer.mozilla.org/en-US/learn/javascript)

### exports 和 require

exports 和 require 来源于 CommonJS 规范，为 JavaScript 提供了模块化思想。服务器端 JavaScript 不同于客户端 JavaScript 的一大区别在于其每一个单独的 JavaScript 文件会形成一个模块（module），在不做任何额外操作的情况下，各模块之间是无法相互贯通的。也就是说模块定义了一个程序边界，变量与方法只能在模块内部相互访问，想要在模块之间实现相互引用，就必须进行所谓的“导出”与“导入”操作，在 CommonJS 中的术语称为 exports 和 require。

**exports** 顾名思义就是将模块外可访问的内容进行“导出"声明，以标识其被模块公开，可以跨越模块边界被其他模块访问。

```javascript
// path/to/demo.js
exports.app = function (req) {
    return {
        status: 200,
        headers: {'Content-Type': 'text/plain'},
        body: ['Hello World!']
    };
};
```

可以看出 exports 类似于一个 JavaScript 对象，所有该对象的属性和方法都会被公开出来。因此在一个 JavaScript 文件内（或者说模块内），可以多次使用 exports 来声明导出若干属性和方法。

**require** 用来请求被其它模块 exports 出来的内容。require 是一个方法，接收要请求的模块路径作为参数。不同于 Java 的类加载机制，由于 JavaScript 是解析执行的，直到文件被首次 require 的时候，引擎才会解析其内容，解析后的结果会被缓存起来，再次访问的时候就不用重新解析。当文件发生变化的时候，require 会重新解析该文件并缓存，从而达到动态语言的效果。

```javascript
var app = require('path/to/demo'); // 模块路径可以省略 .js 扩展名
```

### 解构赋值

```javascript
var user = {
    firstName: 'Tom',
    lastName: 'Smith'
};
var firstName = user.firstName;
var lastName = user.lastName;
```

上面的代码的功能是将 user 对象的两个属性 firstName 和 lastName 分别赋值给两个的变量。设想一种情况，假如 user 具有非常多的属性，想要进行类似的赋值，就必须写很多行赋值语句，而且每一行都要包含相同的 user 引用。针对这种情况，JavaScript 1.7 版本以后，开始引入一种新的概念，称为“解构赋值”。关于解构赋值的更多信息，请参考[这里](https://developer.mozilla.org/en/New_in_JavaScript_1.7, "New in JavaScript 1.7")。

```javascript
var {firstName, lastName} = user;

// 以上写法等同于
var firstName = user.firstName;
var lastName = user.lastName;
```

3. 框架详细介绍
---------------

为了更好的理解和使用本框架，与框架代码一起发布的还有一个 Drivebox 试驾系统。该系统是一个可以独立运行的 Web 应用程序，构建于本框架的基础结构之上。一方面可以用来演示框架在实际生产中是如何被使用的，另外一方面该系统整合了常用的业务功能（例如账户管理、组织机构管理、认证管理和授权管理等），以避免这些功能在项目过程中被重复开发。可以使用如下方法获取试驾系统的源代码（注：需要向管理员所要访问权限）：

**试驾系统的定位有待明确！**

```
hg clone https://bitbucket.org/zyeeda/zyeeda-drivebox-2.0
```

如何部署和运行该系统，请参考试驾系统用户手册。

### 工作区目录结构

基于本框架构建的项目，目录结构要遵守一定的规范。以试驾系统为例：

   ![](assets/images/user-guide/project.png)

这是一个典型的 Maven 项目，在 src/main 目录下有六个子目录，各自用途如下：

- java \- Java 文件目录。所有由 Java 语言编写的程序要放到此目录下，比如领域实体等。
- javascript \- JavaScript 文件目录。服务器端 JavaScript 程序要放到此目录下，基于本框架开发的大部分代码应该集中在此。
- resources \- 配置文件目录。Spring、JPA 等配置文件都存放在此目录下。
- rules \- 工作流及规则文件目录。（**规则和流程应该分开**）
- sql \- 系统初始化及升级时使用的 SQL 语句。
- webapp \- Web 应用程序目录。

### 实体设计

框架提供了三个基础实体类，如下表：

<table class="table table-striped table-bordered">
    <thead>
        <tr>
            <th>实体类型</th>
            <th>实现接口</th>
            <th>继承父类</th>
            <th>包含属性</th>
            <th>属性类型</th>
            <th>数据库字段名</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>DomainEntity</td>
            <td>Serializable</td>
            <td>Object</td>
            <td>id</td>
            <td>String</td>
            <td>F\_ID</td>
        </tr>
        <tr>
            <td>SimpleDomainEntity</td>
            <td>Serializable</td>
            <td>DomainEntity</td>
            <td>name<br />description</td>
            <td>String<br />String</td>
            <td>F\_NAME<br />F\_DESCRIPTION</td>
        </tr>
        <tr>
            <td>RevisionDomainEntity</td>
            <td>Serializable</td>
            <td>SimpleDomainEntity</td>
            <td>creator<br />createdTime<br />lastModifier<br />lastModifiedTime</td>
            <td>String<br />Date<br />String<br />Date</td>
            <td>F\_CREATOR<br />F\_CREATED\_TIME<br />F\_LAST\_MODIFIER<br />F\_LAST\_MODIFIED\_TIME</td>
        </tr>
    </tbody>
</table>

作为父类，DomainEntity 实现了 Serializable 接口，这样所有继承自 DomainEntity 的实体类就都可以支持序列化了。每个基础实体类都拥有一些预定义属性，可以按需选取继承。上表列出了每个实体类及其属性的概要信息。建议通过继承基础实体类的方式来达到业务实体类可序列化的目的，而且通过继承也免去了开发人员重复定义一些常用属性的麻烦。但是要注意一点，如果自定义业务实体类继承了基础实体类，那么就不要再给业务实体类定义和基础实体类中相同的属性了，也就是说不要覆盖基类中定义过的属性。

##### DomainEntity

DomainEntity 实现了 Serializable 接口，并定义了一个 id 属性（默认采用 UUID 的生成策略）。所有自定义业务实体类都应该继承 DomainEntity。

```java
@MappedSuperclass
public class DomainEntity implements Serializable {
    ...

    @Id
    @Column(name = "F_ID")
    @GeneratedValue(generator="system-uuid")
    @org.hibernate.annotations.GenericGenerator(name="system-uuid", strategy = "uuid")
    public String getId() {
        return id;
    }

    ...
}
```

##### SimpleDomainEntity

SimpleDomainEntity 继承自 DomainEntity，并且扩展了 name 和 description 属性。有这两个字段需求的实体类型应该继承此类。

```java
@MappedSuperclass
public class SimpleDomainEntity extends DomainEntity {
    ...

    @Basic
    @Column(name = "F_NAME")
    public String getName() {
        return this.name;
    }

    @Basic
    @Column(name = "F_DESC", length = 2000)
    public String getDescription() {
        return this.description;
    }

    ...
}
```

##### RevisionDomainEntity

RevisionDomainEntity 继承自 SimpleDomainEntity，并且扩展了 creator，createdTime，lastModifier 和 lastModifiedTime 四个属性。与前面两者的不同之处在于，该类型不仅仅扩展了四个与修订信息有关的字段，而是在系统运行时，这四个字段的值会根据用户登录的上下文自动填充。因此有需要记录修订信息的业务实体，应该继承此类型。

```java
@MappedSuperclass
public class RevisionDomainEntity extends SimpleDomainEntity {
    ...

    @Basic
    @Column(name = "F_CREATOR", length = 50)
    public String getCreator() {
        return this.creator;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREATED_TIME")
    public Date getCreatedTime() {
        return this.createdTime;
    }

    @Basic
    @Column(name = "F_LAST_MODIFIER", length = 50)
    public String getLastModifier() {
        return this.lastModifier;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_LAST_MODIFIED_TIME")
    public Date getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    ...
}
```

##### 如何自定义业务实体类

假如我们要定义一个 People 实体类，这个实体类具有 id，name，sex 和 age 属性，具体代码如下：

```java
@Entity
@Table(name="ZDA_PEOPLE")
public class People extends DomainEntity {

    private static final long serialVersionUID = 2338396716859666598L;

    private String name;
    private String sex;
    private int age;

    @Basic
    @Column(name = "F_NAME", length = 32, nullable = false)
    public String getName() {
        return name;
    }

    @Basic
    @Column(name = "F_SEX", length = 32, nullable = false)
    public String getSex() {
        return sex;
    }

    @Basic
    @Column(name = "F_AGE", nullable = false)
    public int getAge() {
        return age;
    }

}
```

**注：表名和字段名需要遵守一定的命名规范：标识符全部采用大写字母，表名前增加 ZDA\_ 前缀（或其他项目相关前缀），字段名前增加 F\_ 前缀。并且所有标记要打在 getter 方法上，而不是属性字段上。**

通过继承 DomainEntity，People 类型无需再定义 id 属性，并且已经实现了 Serializable 接口。

以上代码中的所有实体关系映射（Object Relationship Mapping, ORM）都是通过 Java 注解实现的，开发人员需要对 Hibernate Annotations 和 Hibernate JPA Annotations 有一定的了解。请参考以下网站获取更多信息：

- [Hibernate Annotations](http://www.techferry.com/articles/hibernate-jpa-annotations.html "Hibernate Annotations")
- [Hibernate JPA Annotations](http://docs.jboss.org/hibernate/annotations/3.5/reference/en/html_single/#entity-mapping "Hibernate JPA Annotations")

### 框架组成结构

基于本框架构建的项目称为应用程序（application）。应用程序下面包含若干模块（module），而模块下面还可以有子模块，由此形成一棵以应用程序为根的模块树。每个模块都是由 router，service 和 manager 三层结构组成的，它们分别放置于每个模块下的 routers, services 和 managers 文件夹中（注意全部都是复数）。

![](assets/images/user-guide/main.png)**这个图片的表意不明确需要替换**

- router \- 请求路由层。定义请求的分发与处理规则，是每个请求的入口点。在 router 层中应该处理与请求或响应本身有关的逻辑，比如信息验证和/或数据格式转换等，而不应该处理任何与业务逻辑有关的内容，这样的代码应该写在 service 层中。
- service \- 业务逻辑处理层。所有与业务逻辑相关的代码应该写在 service 层中，但是不包含访问底层持久化等组件的功能，这些是 manager 层需要处理的事情。最重要的，任何有关**事务**的处理都应该在 service 层中完成。
- manager \- 数据持久化层，或底层调用层。任何需要访问持久化组件，或需要跟其它底层组件发生交互而完成的任务应该在 manager 层中实现。

**注：这三层结构是有明确边界的，之间应该是顺序调用的（router \-&gt; service \-&gt; manager），而且分工明确，不应彼此渗透。良好的架构模式有助于代码的可重用性、可扩展性与可维护性。**

#### 应用程序入口点

应用程序入口点可以和 Java 程序中的 main 方法做一个类比：main 方法是用来启动 Java 程序的入口和起点；本框架类似的存在一个 main.js 文件，用来加载和启动所有框架内的功能，以及基于此框架开发的项目中的所有功能，这个文件就是所谓的应用程序入口点。有过 Spring 开发经验的用户应该都了解 ApplicationContext 的意义，在 Spring 框架中 ApplicationContext 就相当于一个入口点。main.js 的内容看上去大致是这个样子的（后面会详细介绍）：

```javascript
// src/main/javascript/main.js
var {createModuleRouter} = require('coala/router');

var router = createModuleRouter();
router.autoMount(this);
router.mount('/scaffold', 'coala/scaffold/router');

exports.router = router;
```

#### router

router 是用来分发和处理请求的，有些类似于 Spring 中的 DispatcherServlet。当有请求来到服务端的时候，router 会拦截所有请求，并根据其中定义的规则，分发请求到一个请求处理方法，该请求处理方法会调用 service（进而是 manager）来实现业务逻辑处理，并最终将响应返回给客户端。下面的代码实现了最简单的 Hello World 功能：

```javascript
var {createRouter} = require('coala/router');
var response = require('coala/response');
var router = createRouter();

router.get('/greeting', function () {
    return response.html('<h1>Hello World!</h1>');
});
```

首先从 coala/router 模块中取出 createRouter 方法，这是创建 router 对象的入口方法；然后获取 coala/response 模块，用来为请求提供响应。调用 createRouter 方法创建一个 router（疑问：createRouter 方法是否可以调用多次？），并调用 get 方法为所有访问到 /greeting 地址的请求输出一个 HTML 片段响应（不完全正确，见下文解释）。该程序运行效果如图所示：

（暂无图片）

这里需要注意的是 router 的 get 方法，这个方法的名称很容易让人产生误解，以为是从 router 中获取某些东西，其实不然。这里的 get 方法对应于 HTTP 协议中的 GET 请求，因此该方法的含义是指：所有（且只有）通过 GET 方法访问 /greeting 地址的请求，才会被后面的回调方法处理，从而返回期待的 Hello World 页面。另外，在调用 get 方法的时候其实并不会立即触发任何请求和响应，而仅仅是在 router 的内部结构中定义了一个映射关系：当有 GET 请求到 /greeting 地址的时候，就执行相应的回调方法，生成响应。因此回调方法真正被调用的时候，是在请求真正发生的时候。这个时间点通常会晚于 get 方法调用的时间点。

[HTTP/1.1](http://www.ietf.org/rfc/rfc2616.txt) 协议中定义了四种常用的请求方式（HTTP Methods）：GET、POST、PUT 和 DELETE，分别对应 router 中的 get、post、put 和 **del** 方法。因为 delete 是 JavaScript 的关键字，所以没办法使用 delete 全称，只能用缩写 del。前两种方法是我们比较熟悉也是经常使用的，但后两种就相对陌生。即便是我们熟悉的 GET 和 POST 方法也经常有人搞不清楚他们的具体含义和应用场景，因此在这里把这四种 HTTP/1.1 协议中定义的请求方式再详细描述一下：

- GET \- 使用 GET 方法请求一个地址，正如其字面所言，应该是获取存在于该地址的某些资源或数据，可以通过 ? 在请求的 URL 后面添加参数，以访问不同的资源或返回不同的数据。除此之外，更深层次的含义，每次 GET 请求都不应该对服务器的状态产生任何影响，也就是说对某个资源进行 0 次 GET 请求、1 次 GET 请求或若干次 GET 请求得到的结果都应该是一样的，否则就不应该使用 GET。因此我们称 GET 请求具有安全和幂等的特性。
- POST \- 使用 POST 方法请求一个地址，通常是要向该地址发送数据，并且这些数据要能够对服务端的数据产生影响，通常是用来新增一些数据，或者完成其他三种方法无法完成的任务，比如批量操作等。因此对一个地址执行 0 次、1 次或若干次 POST 请求，其结果都可能会不一样。因此 POST 请求既不是安全的也不是幂等的。
- PUT \- 使用 PUT 方法请求一个地址，也会通过请求体（request body）向服务端发送数据，与 POST 方法不同的是，通常 PUT 用来处理一些诸如更新和修改的操作。可以想像对某一个地址执行 0 次和执行 1 次 PUT 请求，其结果是不同的，因此 PUT 请求不是安全的；但是执行 1 次和执行若干次 PUT 请求产生的结果却应该是相同的，因此 PUT 请求是幂等。
- DELETE \- 顾名思义，使用 DELETE 方法请求一个地址，就是要删除该地址对应的资源或数据。同 PUT 请求类似，DELETE 请求也是不安全但幂等的（请读者自行分析）。

router 中定义的 get、post、put 和 del 方法，具有相同的方法签名，以 put 为例：

```javascript
router.put(path, function () {
    // 处理到 path 路径的 put 请求
});
```

第一个参数 path 可以是一个正则表达式对象，或者是一个字符串。如果是正则表达式对象，则可以通过正则表达式自身的抽取功能，从访问的路径中提取所需要的参数。如果是字符串对象，path 参数可以包含静态的地址片段和路径占位符。路径占位符分为命名占位符和非命名占位符。命名占位符使用一个冒号（:）加变量标识符的方法来表示，这种占位符会匹配除了斜杠（/）和点号（.）之外的所有字符，命名占位符可在后面跟上一个问号（?）以表示该占位符是可选的；非命名占位符使用星号（\*）来表示，它可以匹配包含斜杠（/）和点号（.）在内的全部字符。

```
/users/:userId
userId 是命名占位符，可以匹配如下路径：
/users/tom      // userId = tom
/users/mary     // userId = mary

/users/:userId?
userId 是可选的命名占位符，可以匹配如下路径：
/users/jerry    // userId = jerry
/users          // userId = null

/users/:userId.:format?
userId 是命名占位符，format 是可选的命名占位符，可以匹配如下路径：
/users/12       // userId = 12, format = null
/users/12.html  // userId = 12, format = html

/books/*
非命名占位符，可以匹配如下路径：
/books/development/java/thingking-in-java.pdf   // * = development/java/thinking-in-java.pdf
/books/biography/us/steve-jobs.pdf              // * = biography/us/steve-jobs.pdf

/files/*.*
两个非命名占位符，可以匹配如下路径：
/files/jquery.js
/files/javascripts/jquery.js
```

第二个参数是请求处理方法，用来编写请求处理逻辑。此方法的第一个参数永远接收 request 对象，其余的参数按顺序是各占位符的匹配值。例如：

```javascript
router.get('/', function () { ... }); // 如果不需要 request 参数，可以不用声明
router.get('/', function (req) { ... });
router.get('/users/:userId', function (req, userId) { ... }); // 声明 request 和 userId 参数
router.get('/users/:userId.:format?', function (req, userId, format) { ... }); // 声明 request, userId 和 format 参数，其中 format 参数是可选的
router.put('users/:userId', function (req, userId) { ... }); // 处理 put 请求
router.del('users/:userId', function (req, userId) { ... }); // 处理 del 请求
```

**注：占位符的匹配值是按照占位符出现的顺序依次在请求处理方法的签名中出现的，与占位符名称和参数名称是否相同无关。**

这里出现的 request 对象是 [JSGI](http://wiki.commonjs.org/wiki/JSGI) 规范中定义的，主要有如下一些属性：

- **.params** \- 以键值对方式存储的所有查询字符串中的参数以及 POST 请求中的参数
- **.env.servletRequest** \- 当需要使用底层 Servlet API 的时候，获取 ServletRequest 类型的请求对象
- **.env.servletResponse** \- 当需要使用底层 Servlet API 的时候，获取 ServletResponse 类型的响应对象

请求处理方法返回一个对象作为响应，该对象需要包含三个必有属性：

- **status** \- HTTP Status Code，用来表示响应的状态。例如：200 代表成功，404 代表请求的资源未找到等
- **headers** \- HTTP Response Header，响应的头信息
- **body** \- HTTP Response Body，响应的主体内容

```javascript
router.get('/users/:userId', function (req, userId) {
    var user = findByUserId(userId); // 该方法需自行实现
    return {
        status: 200,
        headers: {
            'Content-Type': 'application/json'
        },
        body: '{userId: user.userId, name: user.userName, age: user.age, gender: user.gender}'
    }
});
```

使用这种方式返回响应，需要对 HTTP 协议有深入的了解，尤其需要了解各请求和响应的头信息的含义。而且此处 body 字段的内容必须是静态的数据，如果需要返回流式数据的话，仅仅使用 body 就无能为力了。因此框架引入了更方便的方式对请求进行响应。

```javascript
var response = require('coala/response');
```

使用如上的方法获取 response 工具对象，该对象包含如下一些属性和方法：

##### charset()

如果调用此方法时不传入参数，则返回当前正在使用的字符编码（默认为 UTF-8）；如果传入参数，则将该参数设置为当前字符编码。字符编码会跟随在响应头信息的 Content-Type 字段处。

```javascript
var currentChartset = response.charset(); // 返回当前使用的字符编码，默认为 UTF-8

response.charset('GB2312'); // 设置当前使用的字符编码为 GB2312
currentCharset = response.charset() // 返回 GB2312
```

##### html()

返回 HTML 格式的响应。当仅有一个传入参数，且该参数是一个 JavaScript 对象的时候，方法使用此对象作为请求响应。与规范中定义的不同之处在于，除了 body 字段之外，status 和 headers 字段都是可选的。如果 status 字段省略则默认为 200，如果 headers 字段省略则默认为 Content-Type: text/html。当传入参数只有一个，但不是 JavaScript 对象的时候，或者有多个传入参数，则方法将这些参数对应的字符串组合输出为 body 的内容，status 取默认的 200，headers 取默认的 Content-Type: text/html。

```javascript
response.html('<h1>Hello World</h1>');

// 等同于
response.html({
    body: '<h1>Hello World</h1>'
});

// 等同于
response.html({
    status: 200,
    headers: {
        'Content-Type': 'text/html'
    },
    body: '<h1>Hello World</h1>'
});

response.html({
    status: 201,
    body: '<h1>201 Created</h1>'
});

response.html(
    '<table><tr>',
    '<td>姓名</td>',
    '<td>年龄</td>',
    '</tr></table>'
);
```

##### xml()

返回 XML 格式的响应。同 html() 方法雷同，只是 headers 默认取值为 Content-Type: application/xml。

##### redirect()

返回 303 重定向响应，将页面重定向到指定路径。该方法需要一个重定向的目标地址作为参数。

```javascript
response.redirect('/sso/acounts/openid/signin');
```

##### notFound()

返回 404 请求路径未找到响应，该方法需要一个未找到的路径地址作为参数。

```javascript
response.notFound('/users/steve-jobs');
```

##### error()

返回 500 服务器错误响应，该方法需要一个错误消息作为参数。

```javascript
response.error('系统发生致命错误，请联系管理员！');
```

##### json()

在介绍此方法之前，需要先对本框架使用的 JSON 序列化组件有一个简单的认识。本框架的 JSON 序列化采用了 [Jackson](http://jackson.codehaus.org/) 库的 [filter](http://wiki.fasterxml.com/JacksonFeatureJsonFilter) 功能，就是可以根据指定的 filter 名称和包含的以及排除的字段来自动生成 JSON 结果。其中的 filter 可以想像为一组属性的集合，通常定义在实体类上，例如：

```java
@JsonFilter("user")
public class User extends DomainEntity {
    private String userName;
    private String password;
    private int age;
    private List<Group> groups;
}

@JsonFilter("group")
public class Group extends DomainEntity {
    private String groupName;
    private List<User> users;
}
```

以上代码定义了两个 filter，分别命名为 user 和 group，名为 user 的 filter 就包含四个属性，分别是 userName, password, age 和 groups；名为 group 的 filter 包含两个属性，分别是 groupName 和 users。

了解了 filter 的含义，再来看此方法的定义，此方法需要传入两个参数：

- **object** \- 待序列化的实体对象，可以是 JavaScript 对象也可以是 Java 实体对象
- **config** \- 序列化过程的配置信息
    - **include** \- 需要包含在序列化结果中的字段列表
    - **exclude** \- 不需要包含在序列化结果中的字段列表
    - **status** \- HTTP Status Code，可选，默认为 200
    - **headers** \- 响应头信息，可选，默认为 Content-Type: application/json

include 和 exclude 的类型都是 JavaScript 对象，对象的键是 filter 的名称，值为属性列表。需要注意的是，同一个名称的 filter 不能同时出现在 include 和 exclude 中。承接上例：

```javascript
response.json(user, {
    include: {
        group: ['id', 'groupName', 'users'] // 这个 id 属性是从 DomainEntity 继承过来的
    },
    exclude: {
        user: ['password']
    }
});
```

这段代码的含义就是生成的 JSON 结果要包含 Group 对象里面的 id, groupName 和 users 属性，而去掉 User 对象里面的 password 属性。有关 Jackson 及 JsonFilter 的更多用法请参考 Jackon 的官方文档。

##### stream()

以流的形式返回响应。该方法的第一个参数是 JSGI request 对象，第二个参数可以是一个对象或者一个方法。如果是一个对象，则包含如下字段：

- **status** \- HTTP Status Code，可选，默认为 200
- **headers** \- 响应头信息，可选，默认为 Content-Type: binary/octet-stream
- **callback** \- 数据流处理方法，该方法会被传入 request.env.servletResponse.getOutputStream() 对象，用来向响应中以流的方式写入数据

如果第二个参数是一个方法，其含义就相当于上文中的 callback。

```javascript
response.stream({
    status: 200,
    headers: {
        'Content-Type': 'application/ms-word'
    },
    callback: function (outputStream) { // outputStream = request.env.servletResponse.getOutputStream()
        // 向 outputStream 中写入数据流
    }
});
```

#### 挂载

####createModuleRouter

`createModuleRouter` 方法创建的 router 有持有两个方法，分别为  autoMount 和 mount 。

-autoMount

语法： autoMount( router )

参数： router 对象

说明： 自动挂载有两个原则：1.只能挂载名为 router.js 的文件；2.自动挂载的范围是与调用 autoMount 方法的文件所处同级文件夹的子文件夹下的文件，并且只挂载一级，如果子文件夹下还有子文件夹则不会被挂载。只有同时满足以上两个条件的文件，才会被自动挂载。自动挂载后文件的请求路径是 ： `调用autoMount方法的文件的请求路径` / `被挂载文件的相对路径`。


-mount

语法： mount( requestPath , routerPath )

参数： requestPath 是 router 访问路径；routerPath 是 router 文件的路径。

说明： 挂载指定路径的 router 文件，并且绑定访问路径。

示例如图：

![](assets/images/user-guide/autoMount-mount.png)

上图左侧为项目 src/main/javascript 文件夹下目录结构，右侧为 main.js 中代码。由于代码是 main.js 文件中的，所以当前文件就是 main.js ，假设 main.js 请求访问路径是 rootPath 。

如图上图所示,右侧目录结构中用红色方框标示的文件是代码 `router.autoMount(this);` 自动挂载的文件。 `model1/business1/router.js` 未被自动挂载是因为超过了自动挂载的层级限制，`common` 文件夹下文件未被自动挂载是因为没有名为 `router.js` 的文件。自动挂载的文件 `src/main/javascript/model1/router.js` 的访问路径为 rootPath/model1,`src/main/javascript/model2/router.js` 的访问路径为 rootPath/model2 。

右侧用蓝色标示的标示的文件是代码 `router.mount('/common', 'common/common-router');` 制定挂载的文件。 mount 方法的第一个参数 `/common` 是请求 common-router.js 的路径，第二个参数 `common/common-router` 是  common-router.js 的相对路径。指定挂载的 `src/main/javascript/common/common-router.js` 的访问路径为 rootPath/common 。

####createRouter

createRouter 创建的 router 对象用来定义和处理某个资源的操作请求。例如`用户信息`是一个具体资源 ，createRouter 创建的 router 对象就用来定义诸如 创建 、删除 、修改、查询 等针对用户信息的操作请求以及处理方式。

createRouter 创建的 router 对象提供了 “get” ， “post” ，“put” 和 “del” 四种请求处理方法，分别对应不同类型的数据操作。

- get ：检索数据时请求，单条数据或多条数据检索的时候请求

- post：插入数据或者批量删除数据时请求

- put ：更新数据时请求

- del ：删除单个数据时请求

通常，检索、更新和删除单条数据时需要通过URL来传递参数。假设有如下代码：

请求处理1

```javascript
		//处理 新增 操作
		//请求路径为 ： path
		//URL : `http://localhost：8080/drivebox/demo/`
		//执行操作 ： 新增Form中demo对象
 		router.post('/',function(){
 			//要执行的操作
 		});
```

请求处理2

```javascript
		//处理 根据id删除 操作
		//请求路径为 ： path/domain_id
		//URL : `http://localhost：8080/drivebox/demo/123`
		//执行操作 ： 删除id为123的demo数据
 		router.del('/：id',function(request，id){
 			//回调函数中的id将接收URL中的id参数，此例为123
 			//要执行的操作
 		});
```

请求处理3

```javascript
		//处理 批量删除 操作
		//请求路径为 ： path/delete
		//URL : `http://localhost：8080/drivebox/demo/delete`
		//执行操作 ： 删除demo数据
 		router.post('/delete',function(request){
 			//要执行的操作
 		});
```

请求处理4

```javascript
		//处理 修改 操作
		//请求路径为 ： path/domain_id
		//URL : `http://localhost：8080/drivebox/demo/123`
		//执行操作 ： 修改id为123的demo数据
 		router.put('/',function(request，id){
 			//回调函数中的id将接收URL中的id参数，此例为123
 			//要执行的操作
 		});
```

请求处理5

```javascript
		//处理 根据id查询 操作
		//请求路径为 ： path/domain_id
		//URL : `http://localhost：8080/drivebox/demo/123`
		//执行操作 ： 检索id为123的demo数据
 		router.get('/：id',function(request，id){
			//回调函数中的id将接收URL中的id参数，此例为123
 			//要执行的操作
 		});
```

请求处理6

```javascript
		//处理 查询列表 操作
		//请求路径为 ： path
		//URL : `http://localhost：8080/drivebox/demo/`
		//执行操作 ： 查询所有demo数据
 		router.get('/',function(request){
 			//要执行的操作
 		});
```

通过以上请求处理代码片段，我们可以看出，操作和请求处理方法是一一对应的，这是一种严格的约束。即使相同的请求路径，调用不同的处理方法也会成为不同的操作。而且请求对参数有严格的要求，如果定义的请求处理路径中带参数，那么相应的请求也不许带参数，否则无法匹配到这个请求定义。例如我们定义如下router：

```javascript
 		router.get('/:id',function(){});
```

通过 `http://path/123` 可以请求到上面定义的请求处理，而且`http://path/` 则无法请求到。并且调用请求的时候需要显示的告诉浏览器，你将采取何种请求方式： `get` 、 `put` 、`post` 、 `del` 。

关于如何显示的告诉浏览器采取何种调用方式，我们将会在前端文档中提及，在此就不做赘述了。
 

（备注：path 为当前请求的 root 路径；domain\_id 为数据id；by 为查询条件关键字；field-desc 为按某个字段排序，field 为字段名称，desc 为降序关键字；page 为分页关键字，其后要查询的页数；）

- 检索单条数据的时候URL为：path/domain\_id，例如：`http://localhost：8080/drivebox/
demo/123`，调用get请求的时候为检索id为123的demo数据。

- 检索多条数据的时候URL为：path/by/field-desc/page/1，例如：`http://localhost：8080/drivebox/demo/by/name-desc/page/2` ， 调用get请求的时候为按照 name 降序查找第2页的数据，默认分页条数为10。

- 删除单条数据的时候URL为：path/domain\_id，例如：`http://localhost：8080/drivebox/demo/123` ， 调用del请求的时候为删除id为123的demo数据。

- 更新数据的时候URL为：path/domain\_id，例如：`http://localhost：8080/drivebox/demo/123` ， 调用put求情的时候为更新id为123的demo数据。

在此章，并没有关于如何在router中调用service介绍。因为这种调用时用注入的方式实现的，而注入会涉及到`marker`的用法。我们将在下面的章节单独介绍如何使用mark来完成servce，mananger等的注入，所以在此就不做阐述。

###service###
service层主要用来处理业务，并调用manager层完成数据操作。通常，我们的业务代码都写在这一层，并且事物处理也必须在这一层完成。接下来，我们将介绍service的用法，以及service相关的方法。

####创建service
首先，我们来看下如何创建一个service对象。

框架的service封装类提供了一个名为`createService`的方法，将为会返回一个service对象。

```javascript
 		var {createService} = require('coala/service');
 		var service = exports.service = createService();
```

service中定义了两个方法：

-getEntityManager

语法： getEntityManager( EntityManagerFactory ) 

参数： EntityManagerFactory 为配置文件中 EntityManagerFactory的名称

说明： 方法的返回值为 EntityManager 对象

```javascript
 		var entityManager = service.getEntityManager('entityManagerFactory');
```

-createManager

语法： createManager( entityClass , entityManagerFactoryName )

参数： entityClass为业务实体的类路径；EntityManagerFactory 为配置文件中 EntityManagerFactory的名称

说明： 方法的返回值为 Manager 对象

```javascript
 		var entityManager = service.getEntityManager('com.zyeeda.drivebox.entity.User','entityManagerFactory');
```

###manager###
manager负责跟数据库进行交互。

manager基于 Hibernate JPA 2.0实现，提供了一系列对数据库的操作方法。

-find

语法 : find(\[ids\])

参数 : ids -id数组

说明 : 根据id查找并返回与Mnanager绑定的Entity对应的结果集

-getReference

语法 : getReference( \[ids\] )

参数 : ids -id数组

说明 : 根据id查找并返回与Mnanager绑定的Entity对应的结果集

-merge

语法 : merge( \[entities\] )

参数 : entities -Entity数组

说明 : 将 Entity 更新到数据库，并返回更新成功的记录

-save

语法 : save( \[entities\] )

参数 : Description:entities -Entity数组

说明 : 将 Entity 保存到数据库，并返回成功保存的记录

-remove

语法 : remove( \[entities\] )

参数 : entities -Entity 数组

说明 : 从数据库中删除 Entity 对应的数据，并返回成功删除的记录

-removeById

语法 : removeById( \[ids\] )

参数 : ids -id数组

说明 : 根据id删除数据库中对应的记录，并返回成功被删除的记录集合 

-contains

语法 : contains( entity )

参数 : entity -entity对象

说明 : 判断数据库中是否已经存在此 Entity 所对应的记录，如果存在则返回 true ，否则返回 false

-flush

语法 : flush()

参数 : NULL

说明 : 将处于游离状态的对象持久化到数据库中

-refresh

语法 : refresh( \[entites\] )

参数 : entites -Entity 数组

说明 : 从数据库刷新实例的状态，如果有则覆盖实体的变化

-getAll

语法 : getAll( option )

参数 : option -Object 类型对象，此对象有以下属性 orderBy ，firstResult 和 maxResults

说明 : 查询并返回所有结果集

-findByExample

语法 : findByExample( example , option )

参数 : example -org.hibernate.criterion.Example 对象；option -Object 类型对象，此对象有以下属性orderBy，firstResult，maxResults和fetchCount

说明 : 通过Manager所绑定的Entity的来构造一个查询条件，并返回满足查询条件的结构集，详细请参考[Criteria ](http://docs.jboss.org/hibernate/orm/4.1/devguide/en-US/html/ch12.html  "Criteria")

自定义查询方法

除了以上方法之外，我们还可以通过 JPA 的 orm.xml 配置文件来提供自定义的数据库操作方法。

![](assets/images/user-guide/resources.png)

定义自定义查询方法的步骤如下：

1.在 resources/META-INF/orms 目录下建立自定义 orm 配置文件，例如 test-orm.xml

2.在自定义 orm 配置文件中添加自己的查询方法，例如在 test-orm.xml 中添加

```xml
		<named-query name="queryByName">
	         	<query>
	                	from Demo where name = :name
	         	</query>
    		</named-query>
```

3.在 resources/META-INF 目录下 persistence.xml 文件中注册自定 orm 配置文件，例如添加如代码

```xml
 		<mapping-file>/META-INF/orms/test-orm.xml</mapping-file>
```

4.调用方式跟 manager 自定义的方法一样 ，例如调用 manageer.queryByName( name ) 就会自动定位到上述自定义的 queryByName 方法

如果需要了解 orm.xml 和 persistence.xml 文件的详细配置方法以及用途，请访问[http://](http:// "1111")
###marker###
本框架引入了marker来解决注入的问题，目前提供四种类型注入 inject ，managers ， services 和 tx 。在 router 中注入 service 或者在 service 中注入 manager 都是通过 marker 来完成的，类似于 spring 的 annotation 。

marker 类提供了两个方法：

-mark

语法 ： mark( name , attributes )

参数 ： name 为注入的参数类型名称，目前提供四种类型，分别为 ‘inject’  , ‘managers’  , ‘services’ , ‘ tx ’ ; attributes 为注入的对象或者对象类型的数组

- inject ： 参数为 Spring 容器中 Bean 的名称或者类型

- managers ： 参数为 mananger 对象的名称或者类型

- services ： 参数为 service 对象的名称或者类型

- tx ： 不需要参数

说明 ： 用来实现注入功能

-on

语法 ： on( fn, me )

参数 ： fn为回调函数，此函数的参数为 mark 方法的 attributes ； me 为被绑定对象

说明 ： 接收 mark 方法注入的对象

mark 和 on 是成对出现的，通过 mark 方法注入的对象必须通过 on 方法接收。mark 注入的对象在 on 的回调方法中的顺序是依次从左到右的，基本用法为：

```javascript
 mark('a').mark('b').mark('c').on(function(a, b, c){})
```

通过 mark 方法，很容易实现在 router 中注入 service ，或者在 service 中注入 manager。

例如：

mark实例1： 在 router 中注入 service 

```javascript
 		router.get('/', mark('services',service).on(function(service,request){
 			//调用 service 处理业务
 		}));
```

router 中还有另外一种调用 service 的方法：

```javascript
 		var {createService} = require('demo/service/service');
 		var service = exports.service = createService();
 		router.get('/', function(request){
 			//调用 service 处理业务
 		});
```

mark实例2 ： 在 service 中注入 mananger

```javascript
 		var {DemoEntity} = com.zyeeda.drivebox.entity;
 		var {mark} = require('coala/mark');
 
	 	exports.createService = function() {
 			return {
 				create: mark('managers',DemoEntity).mark('tx').on(function(manager,entity){
 					return manager.save(entity);
 				})
 			};
 
 		};
```

mark实例1提供了两种在 router 中调用 service 的方法 ，第一种是通过调用 mark 方法注入，第二种是通过 require 调用 service 的 createService 方法。虽然两种方式达到的效果一样，但是我们建议使用 mark 的方式注入，因为这样更方便，也更灵活。

mark实例2是在 service 中定义的 create 方法样例，create 方法调用 mark 注入了 managers 和 tx 两种类型的对象。

managers 类型的对象用来注入一个 manager 对象，但是实际上我们提供了一个名为  DemoEntity 的实体类。这个实体类被注入后， on 中接收了一个名为 manager 的对象，这个对象的类型为封装类 manager 。所以，实际上注入 mangers 类型的对象的时候，我们需要传递给 mark 方法 entity 的名称或者类型。mark 会根据注入的 entity 返回给 on 方法一个对象的 manager 对象。

tx 为Spring的事物管理器，无需参数，只要对方法 mark('tx') , 该方法会自动被添加事物处理。

mark实例2 create 中 on 方法的第二个参数 entity 是调用 create 方法需要传递的参数。

mark 注入的约束

- router 层不能注入 managers 和 tx

- service 层可以注入 service ，manager ， tx 和 inject 


####Scaffold###
通过在实体中配置 @scaffold 注解的方式，可以实现实体类的 CRUD 操作，并且不需要写任何后台代码。同时可以通过 Scaffold 的配置文件实现扩展功能，例如对自动生成的 CRUD 方法的重写和过滤，对查询结果的过滤和类型转换等功能。通过对 Scaffold 的应用，可以大量减少后台的代码，进一步提升开发效率。

配置Scaffold

假设存在一个数据字典类 DriveType ，下面以实现此数据字典类的 CRUD 功能为例介绍 Scaffold 的实现步骤。

第一步 ： 创建实体类并在实体上添加 @Scaffold 注解

![](assets/images/user-guide/drive-type-entity.png)

如图，在 entity 目录中建立如图所示的 DriveType.java 文件 ，DriveType 类的头部添加如下注解

```java
		@Entity(name = "DriveType")
 		@Table(name = "ZDA_DRIVE_TYPE")
 		@Scaffold(path = "/system/driveType")
 		public class DriveType extends SimpleDomainEntity {
```

注解 @Scaffold 提供了一个参数 path 属性， 该属性的值既是 CRUD 操作对应的 URL ，又是 Scaffold 配置文件的路径。

第二步 ： 在 config.js 中配置实体包路径

![](assets/images/user-guide/drive-config.png)
 
在 config.js 中添加如下代码：

```javascript
 		exports.env = {
     		entityPackages : [ 'com.zyeeda.drivebox.entity' ]
 		};
```

config.js 为固定名称和位置的配置文件， entityPackages 指定的是实体类的包路径。

第三步 ： 在 scaffold 目录中建立配置文件

![](assets/images/user-guide/scaffold-path.png)

如图在 src/main/javascript/scaffold/system 下创建 driveType.js 文件，此文件的路径和 `@Scaffold(path = "/system/driveType")` 一致。加载 Scaffold 注解的时候会根据 path 属性的的值去加载 src/main/javascript/scaffold 目录下对应的文件。

driveType.js 文件中代码如下：

```javascript
 		// 用来重写默认的请求处理方法
 		exports.handlers = {
 			create：function(){
 				//重写create
 			}
 		};
 
 		// 去除不需要的请求处理方法
 		//过滤掉 list 方法和 get 方法
 		exports.exclude = ['list', 'get'];
 
 		// 过滤后台传递给前台的 json 数据
 		exports.filters = {
		};
 
 		// 处理额外的 URL
 		exports.doWithRouter = function(router) {
 			router.get('/json',function(){
 				//定义默认处理方法以外的处理方法
 			});
 		};
 
 		// 数据类型转换器
 		exports.converters = {
 			//createTime 为 DriveType 类的一个字段
 			createTime ： function(){
 				//类型转换方法
 			}
 		};
```

driveType.js 用来实现以下功能：

- handlers ：用来重写 Scaffold 默认的请求处理方法。下图为 Scaffold 提供的默认请求方式

	![](assets/images/user-guide/auto-mehtod.png)

- exclude : 过滤掉不需要的请求处理方法。只能过滤掉默认方法 list ， get ， create ， updte ， remove 中的一个或者多个。

	例如配置 `exports.exclude = ['list', 'get'];` ，无法通过 get 方式请求到 list 方法和 get 方法。

- filters ： 过滤后台传递给前台的 json 数据

- doWithRouter ： 处理额外的 URL。用于给 router 定义除默认方法外额外的处理方法。例如：

```javascript
 		exports.doWithRouter = function(router) {
 			router.get('/json',function(){
 				//定义默认处理方法以外的处理方法
 			});
 		};
```


通过上例的配置，DriveType 类就拥有了一个名为 `/json` 的方法。

- converters ：数据类型转换器。声明实体类的一个字段，然后给字段制定一个数据转换器。



通过	Scaffold 注解的方式，很容易实现原本需要通过 router ，service  和 manager 建立的层层调用的后台处理结构。这种方式更简洁，需要编写的代码更少。同时，并不会影响业务处理。所以，基于 Scaffold 的注解是一种值得尝试的方式。









