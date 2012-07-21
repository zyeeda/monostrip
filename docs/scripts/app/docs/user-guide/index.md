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


1、准备环境
-----------

### 1.1、JDK

本框架需要使用 JDK 1.6 及以上版本进行编译和运行，请确认系统中正确配置了 Java 开发和运行环境。

### 1.2、Maven

[Apache Maven](http://maven.apache.org/ "Maven") 是一款基于项目对象模型（Project Object Model, POM）的项目管理工具，本框架的编译需要使用 Maven。

有关 Maven 的配置与运行，请参考[官方文档](http://maven.apache.org/download.html "Download and Install Maven")。

### 1.3、Mercurial

[Mercurial](http://mercurial.selenic.com/ "Mercurial") 是一款分布式版本管理软件。如果需要获取和查看本项目的源代码，就需要了解 Mercurial 的基本使用方法。想了解有关内容请参考官方[用户手册](http://mercurial.selenic.com/guide/ "Learning Mercurial in Workflows")。

### 1.4、获取源代码

框架源代码托管于 [Bitbucket](https://bitbucket.org "Atlassian Bitbucket") 服务器，使用如下命令获取源代码（注：需要向管理员所要访问权限）：

```
hg clone https://bitbucket.org/zyeeda/zyeeda-framework-2.0
```


2、预备知识
-----------

在开始详细介绍框架功能之前，首先需要了解一下框架引入的新技术和新概念。如果对这些内容已经掌握，可以跳过本章节。

### 2.1、JavaScript

本框架和基于本框架开发的系统会大量使用 JavaScript 语言，如果不熟悉该语言，请参考如下一些学习资源：

- [JavaScript Tutorial from w3schools.com](http://www.w3schools.com/js/default.asp)
- [Learn JavaScript from Mozilla Developer Network](https://developer.mozilla.org/en-US/learn/javascript)

### 2.2、exports 和 require

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

### 2.3、解构赋值

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

3、框架详细介绍
---------------

为了更好的理解和使用本框架，与框架代码一起发布的还有一个 Drivebox 试驾系统。该系统是一个可以独立运行的 Web 应用程序，构建于本框架的基础结构之上。一方面可以用来演示框架在实际生产中是如何被使用的，另外一方面该系统整合了常用的业务功能（例如账户管理、组织机构管理、认证管理和授权管理等），以避免这些功能在项目过程中被重复开发。可以使用如下方法获取试驾系统的源代码（注：需要向管理员所要访问权限）：

**试驾系统的定位有待明确！**

```
hg clone https://bitbucket.org/zyeeda/zyeeda-drivebox-2.0
```

如何部署和运行该系统，请参考试驾系统用户手册。

### 3.1、工作区目录结构

基于本框架构建的项目，目录结构要遵守一定的规范。以试驾系统为例：

   ![](assets/images/user-guide/project.png)

这是一个典型的 Maven 项目，在 src/main 目录下有六个子目录，各自用途如下：

- java \- Java 文件目录。所有由 Java 语言编写的程序要放到此目录下，比如领域实体等。
- javascript \- JavaScript 文件目录。服务器端 JavaScript 程序要放到此目录下，基于本框架开发的大部分代码应该集中在此。
- resources \- 配置文件目录。Spring、JPA 等配置文件都存放在此目录下。
- rules \- 工作流及规则文件目录。（**规则和流程应该分开**）
- sql \- 系统初始化及升级时使用的 SQL 语句。
- webapp \- Web 应用程序目录。

### 3.2、实体设计

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

##### 3.2.1、DomainEntity

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

##### 3.2.2、SimpleDomainEntity

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

##### 3.2.3、RevisionDomainEntity

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

##### 3.2.4、如何自定义业务实体类

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

### 3.3、框架组成结构

所有服务器端 JavaScript 脚本都需要存放在 src/main/webapp/WEB-INF/app 目录中，此目录是所有脚本文件加载的根目录。后面描述中提到的有关文件的绝对路径，都是以这里作为起始的。

基于本框架开发的项目称为应用程序（application）。应用程序包含若干模块（module），而模块下面还可以有子模块，由此形成一棵以应用程序为根的模块树。每个模块都是由 router，service 和 manager 三层结构组成的，它们分别放置于每个模块下的 routers, services 和 managers 目录中（注意全部都是复数）。

![](assets/images/user-guide/main.png)**这个图片的表意不明确需要替换**

- router \- 请求路由层。定义请求的分发与处理规则，是每个请求的入口点。在 router 层中应该处理与请求或响应本身有关的逻辑，比如信息验证和/或数据格式转换等，而不应该处理任何与业务逻辑有关的内容，这样的代码应该写在 service 层中。
- service \- 业务逻辑处理层。所有与业务逻辑相关的代码应该写在 service 层中，但是不包含访问底层持久化等组件的功能，这些是 manager 层需要处理的事情。最重要的，任何有关**事务**的处理都应该在 service 层中完成。
- manager \- 数据持久化层，或底层调用层。任何需要访问持久化组件，或需要跟其它底层组件发生交互而完成的任务应该在 manager 层中实现。

**注：这三层结构是有明确边界的，之间应该是顺序调用的（router \-&gt; service \-&gt; manager），而且分工明确，不应彼此渗透。良好的架构模式有助于代码的可重用性、可扩展性与可维护性。**

根目录中必须能存在一个名为 main.js 的文件，作为应用程序入口点。应用程序入口点可以和 Java 程序中的 main 方法做一个类比：main 方法是用来启动 Java 程序的入口和起点；本框架类似的存在一个 main.js 文件，用来加载和启动所有框架内的模块，以及基于此框架开发的项目中的所有模块，这个文件就是本框架的入口点。有过 Spring 开发经验的用户应该都了解 ApplicationContext 的意义，在 Spring 框架中 ApplicationContext 就相当于一个入口点。

main.js 的内容看上去大致是这个样子的（后面会详细介绍）：

**这段代码要改的**

```javascript
// src/main/javascript/main.js
var {createMountPoint} = require('coala/router');

var mountPoint = createMountPoint();
mountPoint.autoMount(module);
mountPoint.mount('/scaffold', 'coala/scaffold/router');

exports.router = mountPoint;
```

exports.router 是 main.js 里面必须的，用来告知应用程序请求的入口在哪里。

#### 3.4、路由（Router）

与 router 相关的脚本全部存放在每个模块的 routers 目录中，但对每个 router 的文件名没有限制，可以根据具体的功能特征进行选取，也可以根据功能分组，对文件进行组合与拆分，以控制单个脚本文件的体积。每个 router 文件必须导出一个名为 router 的对象（使用 createRouter 方法创建），以便自动自动加载机制可以加载到。

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
/files/javascript/jquery.js
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

#### 3.5、路由、模块和自动挂载

根据以上的介绍，我们应该了解到，应用程序的入口点是 main.js，应用程序由模块和子模块构成，并形成树状结构，在每个模块下都包含 routers、services 和 managers 等目录，在 routers 目录中定义有所有的路由和请求处理逻辑，但问题是这些路由、模块以及主应用程序是如何衔接在一起并配合工作的？简单回答就是：**全自动**。

详细一点来说，框架启动时会加载 main.js 启动主应用程序，主应用程序会根据模块的目录结构加载模块，并自动将该模块的访问路径添加到根挂载点上，然后会遍历模块 routers 目录下的所有文件，挂载每个文件导出的 router 对象（这就是为什么前面介绍 router 的时候要求每个 router 文件必须导出这个对象的原因了）。自动挂载的最终结果就是请求的访问路径，看上去和模块的目录结构是相同的。如下所示：

（上图）

图中所示的应用程序有两个一级模块和若干二级模块，则所有可访问的请求路径如下：

```
/system
/system/users
/system/departments
/system/groups
/workspace/home
/workspace/tasks
/workspace/calendar
```

需要注意的是，routers、services 和 managers 目录并不是直接摆放在模块目录之下，而是放在一个名为 ROOT 的目录下，该目录是一个特殊的子模块，指代当前模块的根路径。也就是说，例如图中 system 模块下的 ROOT 目录，反映到访问路径上，并不是 /system/ROOT，而只是 /system，ROOT 作为当前模块的根路径被省略掉了，其他模块的同样。而且可以看到 routers、services 和 managers 目录只能出现在 ROOT 目录中。

#### 3.6、标记（Marker）

在开始介绍 service 和 manager 之前，需要先来了解一下 marker 的用法。marker 类似于 Java 中的 annotation，主要用来向方法中注入对象。基本用法类似这样：

```javascript
var {mark} = require('coala/marker');

mark('something').on(function () {
    ...
});
```

直接通过字面理解，就是将什么东西标记在某个方法之上。经过 mark 的方法会被注入一些参数，而返回结果是另一方法，该方法绑定了所有被注入的参数，而留下那些没有被注入的参数作为新方法的参数（参考后面的例子有助于理解）。

mark 方法是可以串联使用的，也就是多个 mark 方法可以连接在一起，最后以 on 作为结束：

```javascript
mark('first').mark('second').mark('third').on(function () {
    ...
});
```

目前框架支持四种类型的 marker，分别介绍如下：

##### tx

该 marker 向方法注入事务。经过注入的方法会运行在事务当中。事务的具体注入过程是依赖 Spring 容器的，因此关于事务注入的详细信息，请参考 Spring Transaction 的[官方文档](http://static.springsource.org/spring/docs/3.1.x/spring-framework-reference/htmlsingle/spring-framework-reference.html#transaction)。

```javascript
mark('tx').on(function () {
    ...
});
```

如果需要配置事务的属性，或控制事务的提交、回滚策略，可以在第二个参数里面进行配置：

```javascript
mark('tx', config).on(function () {
    ...
});
```

可以配置的选项有：

- **readOnly** \- 配置该事务是否只读，布尔类型，默认值 false
- **name** \- 事务名称，字符串类型，默认值 transaction name
- **propagationBehavior** \- 事务传播行为，整型枚举，默认值 0，即 PROPAGATION\_REQUIRED
- **isolationLevel** \- 事务隔离等级，整型枚举，默认值 -1，即 ISOLATION\_DEFAULT
- **timeout** \- 事务超时时间，整型，单位秒，默认值 -1
- **needStatus** \- 是否需要注入 [TransactionStatus](http://static.springsource.org/spring/docs/3.1.x/javadoc-api/org/springframework/transaction/TransactionStatus.html) 参数，布尔类型，默认值 false

如果使用了 needStatus 配置，则使用方法如下：

```javascript
mark('tx', {needStatus: true}).on(function (status) {
    ...
});
```

可以在 on 后面的方法中接收这个 TransactionStatus 参数。

##### beans

该 marker 向方法注入 Spring 容器中声明的 bean 对象。

```javascript
mark('beans', ['beanId1', 'beanId2', 'beanId3']).on(function (bean1, bean2, bean3) {
    ...
});
```

此 marker 的第二个参数是一个数组，包含了需要注入的 bean ID。

##### services

该 marker 向方法注入 service 对象。

```javascript
mark('services', ['/path/to/service1', '/path/to/service2', 'path/to/service3']).on(function (service1, service2, service3) {
    ...
});
```

此 marker 的第二个参数是一个数组，包含了需要注入的 service 的绝对路径（绝对路径的根在 main.js 所在的目录）。一般可以使用 module.resolve 方法，来通过相对于当前目录的相对路径来获取绝对路径。此 marker 要求注入的 service 都必须导出一个名为 createService 的方法。

##### managers

该 marker 向方法注入 manager 对象。

```javascript
mark('managers', [User, Group, '/path/to/other-manager']).on(function (userManager, groupManager, otherManager) {
    ...
});
```

此 marker 的第二个参数同样是一个数组，但是里面可以包含两种类型的内容。首先可以指定一种实体类型，则框架会自动注入一个通用的 manager 用来访问该实体，另外如果是指定一个到 manager 文件的绝对路径，则框架注入该文件定义的 manager，也就是用户自己扩展的 manager 对象。同注入 service 的 marker 类似，这里也要求自定义的 manager 文件必须导出一个名为 createManager 的方法。

#### 3.3.4、服务（Service）

#### 3.3.5、管理器（Manager）

#### 挂载点（Mount Point）

前面提到过，应用程序是按照模块的方式来划分的，而模块下面又有子模块，从而形成一棵模块树。模块树的每个节点相对于其子树而言，就是一个挂载点。每个模块及子模块必须被挂载到挂载点上，才可以被请求访问到。前文提到的 main.js 既是应用程序的入口点，又是根挂载点。声明一个挂载点的方式如下：

```javascript
var {createMountPoint} = require('coala/router');
var mountPoint = createMountPoint();
```

同 createRouter 方法一样，createMountPoint 方法也是从 coala/router 模块中获取的，因此广义上讲挂载点也是一种路由。挂载点对象具有如下一些方法：

##### autoMount

自动挂载子模块。想搞清楚这个方法的含义，必须先明白模块的层级结构和组织方式，如下图：

（上图）

图中可以看到，模块与子模块的关系就是文件夹与子文件夹的关系。除了根模块，每个子模块都包含一个名为 module.js 的文件，用来声明该模块的挂载信息（根模块的挂载信息文件不叫 module.js，而是叫 main.js，是不是有点恍然大悟的感觉）。module.js 需要 exprots 一个名为 router 的对象（可以是通过 createRouter 方法或 createMountPoint 方法创建的）。使用 autoMount 方法来挂载子模块，只能挂载当前模块下的一级子模块。挂载的具体过程，就是将子模块 module.js 文件 exports 出来的 router 对象，按照目录名挂载到当前模块上，因此只要每个 module.js 都执行了 autoMount 方法去挂载他们自己的子模块，且在 main.js 中也同样操作，就可以将系统中所有的模块都按照层级递归的挂载到根挂载点上。

需要特别注意的是，每个模块下会有一个特殊的名为 ROOT 的子模块，它的含义是将其下 module.js 导出的 router 对象直接挂载到父模块的根路径上。

autoMount 方法必须接收一个 module 对象作为参数：

```javascript
var mountPoint = createMountPoint();
mountPoint.autoMount(module);
```

createMountPoint 方法也可以在调用时传入 module 对象，这样就不用手工调用 autoMount 方法了：

```javascript
var mountPoint = createMountPoint(module);

// 等同于
var mountPoint = createMountPoint();
mountPoint.autoMount(module);
```

##### mount
