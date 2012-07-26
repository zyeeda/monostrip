![中昱达](assets/images/logo.png "中昱达")

用户手册
=================================

Zyeeda Framework 2.0 是公司全新研发的技术平台与开发框架。在设计与研发过程中，始终秉承如下一些理念：

- *Don't make me think.* 这是本框架的核心开发理念，在任何情况下使用该框架，都不希望框架本身为开发人员带来过多思考过程。
- *One way to do it.* 在本框架的约束下，实现任何功能，都有且只有唯一的一种方式和途径。
- *Minimize your work.* 框架的封装尽量使得开发人员使用最少的代码编写最多的功能。

和 1.x 版本相比，新框架进行了大量的革新与调整，主要体现在如下几个方面：

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

本框架需要使用 JDK 1.6 或以上版本进行编译和运行，请确认系统中正确配置了 Java 开发和运行环境。

### 1.2、Maven

[Apache Maven](http://maven.apache.org/ "Maven") 是一款基于项目对象模型（Project Object Model, POM）的项目管理工具，本框架的编译需要使用 Maven。有关 Maven 的配置与运行，请参考[官方文档](http://maven.apache.org/download.html "Download and Install Maven")。

在内网环境下使用时，建议配置 Maven 的镜像服务器到本地私服。打开 Maven 安装目录下的 conf/settings.xml 文件，找到 &lt;mirrors&gt; 节点，增加如下的配置（<span class="label label-info">提示</span> **其中服务器的地址和端口请向管理员索取**）：

```xml
<mirror>
    <id>zyeeda</id>
    <url>http://${host}:${port}/nexus/content/groups/public</url>
    <mirrorOf>*</mirrorOf>
</mirror>
```

### 1.3、Node 和 Cake

本框架的大部分代码是采用 CoffeeScript 写成的，CoffeeScript 是比 JavaScript 更高级的语言，可以通过工具编译为 JavaScript。编译 CoffeeScript，并将生成的结果打包需要 Cake 环境，而 Cake 又是运行在 Node 上面的。Node 是基于 Google V8 引擎构件的另一款服务器端 JavaScript 框架。有关安装和配置 [Node](http://nodejs.org/ "node.js") 以及 [Cake](http://coffeescript.org/#cake "Cake, and Cakefiles") 的方法请参考有关文档。

### 1.4、Mercurial

[Mercurial](http://mercurial.selenic.com/ "Mercurial") 是一款分布式版本管理软件。如果需要获取和查看本项目的源代码，就需要了解 Mercurial 的基本使用方法。想了解有关内容请参考官方[用户手册](http://mercurial.selenic.com/guide/ "Learning Mercurial in Workflows")。

### 1.5、获取源代码

框架源代码托管于 [Bitbucket](https://bitbucket.org "Atlassian Bitbucket") 服务器，使用如下命令获取源代码（<span class="label label-info">提示</span> **需要向管理员所要访问权限**）：

```
hg clone https://bitbucket.org/zyeeda/zyeeda-framework-2.0
```

### 1.6、编译

如果正确配置了以上环境，编译本框架就变得非常简单，只需要两个命令：

```
cake build
mvn clean install
```

2、预备知识
-----------

在开始详细介绍框架功能之前，首先需要了解一下框架引入的新技术和新概念。如果对这些内容已经掌握，可以跳过本章节。

### 2.1、JavaScript

本框架和基于本框架开发的系统会大量使用 JavaScript 语言，如果不熟悉该语言，请参考如下一些学习资源：

- [JavaScript Tutorial from w3schools.com](http://www.w3schools.com/js/default.asp)
- [Learn JavaScript from Mozilla Developer Network](https://developer.mozilla.org/en-US/learn/javascript)

### 2.2、exports 和 require

exports 和 require 来源于 CommonJS 规范，为 JavaScript 提供了模块化功能。服务器端 JavaScript 不同于客户端 JavaScript 的一大区别在于其每一个单独的 JavaScript 文件会形成一个模块（module），在不做任何额外操作的情况下，各模块之间是无法相互贯通的。也就是说模块定义了一个程序边界，变量与方法只能在模块内部相互访问，想要在模块之间实现互操作，就必须进行所谓的“导出”与“导入”操作，在 CommonJS 中的术语称为 exports 和 require。

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

**require** 用来请求被其它模块 exports 出来的内容。require 是一个方法，接收要请求的模块路径作为参数。不同于 Java 的类加载机制，由于 JavaScript 是解析执行的，直到文件被首次 require 的时候，引擎才会解析其内容，并将结果缓存起来，再次访问的时候就不用重新解析。当文件发生变化的时候，require 会再次解析该文件并缓存，从而达到动态语言的效果。

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

上面的代码的功能是将 user 对象的两个属性 firstName 和 lastName 分别赋值给两个的变量。设想一种情况，假如 user 具有非常多的属性，想要进行类似的赋值，就必须写很多行赋值语句，而且每一行都要包含相同的对 user 对象的引用。针对这种情况，JavaScript 1.7 版本以后，开始引入一种新的概念，称为“解构赋值”。关于解构赋值的更多信息，请参考[这里](https://developer.mozilla.org/en/New_in_JavaScript_1.7, "New in JavaScript 1.7")。

```javascript
var {firstName, lastName} = user;

// 以上写法等同于
var firstName = user.firstName;
var lastName = user.lastName;
```


3、框架详细介绍
---------------

为了更好的理解和使用本框架，与框架代码一起发布的还有一个 Starter Kit 新手包。该系统是一个可以独立运行的 Web 应用程序，构建于本框架的基础结构之上，主要作用是用来展示框架的组成结构和各种配置文件的使用方法。可以通过如下方法获取该系统的源代码：

```
hg clone https://bitbucket.org/zyeeda/zyeeda-starter-kit-1.0
```

想要运行该系统也非常容易，只需要在项目根目录下执行命令：

```
mvn clean package
mvn jetty:run
```

### 3.1、工作区目录结构

基于本框架构建的项目，目录结构要遵守一定的规范。以新手包为例：

![](assets/images/user-guide/1-starter-kit.png)

这是一个典型的 Maven 项目，在 src/main 目录下有六个子目录，各自用途如下：

- **java** \- Java 文件目录。所有由 Java 语言编写的程序要放到此目录下，比如领域实体等
- **processes** \- 流程定义文件目录，所有使用流程设计器或手写的流程定义文件（及相应的 png 或 svg 预览图）都应存放在此目录下
- **resources** \- 配置文件及静态资源文件目录。Spring、JPA、日志等配置文件或模板文件等都存放在此目录下
- **rules** \- 规则定义文件目录
- **sql** \- SQL 脚本目录。系统初始化及升级时使用的 SQL 脚本，应该放置在此目录下
- **webapp** \- Web 应用程序根目录

### 3.2、实体设计

本框架提供了三个基础实体类，如下表：

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

作为父类，DomainEntity 实现了 Serializable 接口，这样所有继承自 DomainEntity 的实体类就都可以支持序列化了。每个基础实体类都拥有一些预定义属性，可以按需选取继承。上表列出了每个实体类及其属性的概要信息。建议通过继承基础实体类的方式来达到业务实体类可序列化的目的，而且通过继承也免去了开发人员重复定义一些常用属性的麻烦。但是要注意一点，如果自定义业务实体类继承了基础实体类，那么就不要再给业务实体类定义和基础实体类中相同的属性了，也就是说不要覆盖基类中定义过的属性。（**要不要加一个跟流程有关的基础类型**）

#### 3.2.1、DomainEntity

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

#### 3.2.2、SimpleDomainEntity

SimpleDomainEntity 继承自 DomainEntity，并且扩展了 name 和 description 属性。有这两个属性需求的实体类型应该继承该类型。

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

#### 3.2.3、RevisionDomainEntity

RevisionDomainEntity 继承自 SimpleDomainEntity，并且扩展了 creator，createdTime，lastModifier 和 lastModifiedTime 四个属性。与前面两者的不同之处在于，该类型不仅仅扩展了四个与修订信息有关的字段，而是在系统运行时，这四个字段的值会根据登录用户的上下文自动填充。因此有需要记录修订信息的业务实体，应该继承此类型。

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

#### 3.2.4、如何自定义业务实体类

假如我们要定义一个 People 实体类，这个实体类具有 id，name，gender 和 age 属性，具体代码如下：

```java
@Entity
@Table(name="ZDA_PEOPLE")
public class People extends DomainEntity {

    private static final long serialVersionUID = 2338396716859666598L;

    private String name;
    private String gender;
    private int age;

    @Basic
    @Column(name = "F_NAME", length = 32, nullable = false)
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "F_GENDER", length = 32, nullable = false)
    public String getGender() {
        return this.gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    @Basic
    @Column(name = "F_AGE", nullable = false)
    public int getAge() {
        return this.age;
    }
    public void setAge(int age) {
        this.age = age;
    }

}
```

**注：表名和字段名需要遵守一定的命名规范：标识符全部采用大写字母，表名前增加 ZDA\_ 前缀（或其他项目相关前缀），字段名前增加 F\_ 前缀。并且所有标记要打在 getter 方法上，而不是属性字段上。**

通过继承 DomainEntity，People 类型无需再定义 id 属性，并且已经实现了 Serializable 接口。

以上代码中的所有实体关系映射（Object Relationship Mapping, ORM）都是通过 Java 注解表示的，开发人员需要对 Hibernate Annotations 和 Hibernate JPA Annotations 有一定的了解。请参考以下网站获取更多信息：

- [Hibernate Annotations](http://www.techferry.com/articles/hibernate-jpa-annotations.html "Hibernate Annotations")
- [Hibernate JPA Annotations](http://docs.jboss.org/hibernate/annotations/3.5/reference/en/html_single/#entity-mapping "Hibernate JPA Annotations")

### 3.3、框架组成结构

所有服务器端 JavaScript 脚本都需要存放在 src/main/webapp/WEB-INF/app 目录中，此目录是所有脚本文件加载的根目录。后面描述中提到的有关文件的绝对路径，都是以这里作为起始的。

基于本框架开发的项目称为应用程序（application）。应用程序包含若干模块（module）（<span class="label label-important">注意</span> **这里模块的概念要区别于前文提到的 CommnsJS 中的模块，这里的模块是真正的业务上的模块**），而模块下面还可以有子模块，由此形成一棵以应用程序为根的模块树。每个模块都是由 router，service 和 manager 三层结构组成的，它们分别放置于每个模块下的 \_\_routers\_\_，\_\_services\_\_ 和 \_\_managers\_\_ 目录中（<span class="label label-important">注意</span> **为了表明这三个目录是系统预定义的目录，因此需要在目录前后都增加双下划线，以示和自定义模块目录的区别。另外这三个文件夹都是复数形式的。要严格遵守这些规范，否则会导致文件加载不到的问题。**）。

![](assets/images/user-guide/2-webapp.png)

- **router** \- 请求路由层。定义请求的分发与处理规则，是每个请求的入口点。在 router 层中应该处理与请求或响应本身有关的逻辑，比如信息验证和/或数据格式转换等，而不应该处理任何与业务逻辑有关的内容，这样的代码应该写在 service 层中。
- **service** \- 业务逻辑处理层。所有与业务逻辑相关的代码都应该写在 service 层中，但是不包含访问底层持久化等组件的功能，这些是 manager 层需要处理的事情。最重要的，任何有关事务的处理工作都应该在 service 层中完成。
- **manager** \- 数据持久化层，或底层调用层。任何需要访问持久化组件，或需要跟其它底层组件发生交互而完成的任务应该在 manager 层中实现。

<span class="label label-important">注意</span> **这三层结构是有明确边界的，之间应该是顺序调用的（router \-&gt; service \-&gt; manager），而且分工明确，不应彼此渗透。良好的架构模式有助于代码的可重用性、可扩展性与可维护性。**

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

### 3.4、路由（Router）

与 router 相关的脚本全部存放在每个模块的 \_\_routers\_\_ 目录中，但对每个 router 的文件名没有额外限制，可以根据具体的功能特征进行选取，最好根据业务功能划分，对 router 文件进行组合与拆分，以控制单个脚本文件的体积。每个 router 文件必须导出一个名为 router 的对象（使用 createRouter 方法创建，见下文），以便自动自动加载机制可以加载到。

router 是用来分发和处理请求的，有些类似于 Spring 中的 DispatcherServlet。当有请求来到服务端的时候，router 会拦截所有请求，并根据其中定义的规则，分发请求到一个请求处理方法，该请求处理方法会调用 service（进而是 manager）来实现业务逻辑处理，并最终将响应返回给客户端。下面的代码实现了最简单的 Hello World 功能：

```javascript
var {createRouter} = require('coala/router');
var response = require('coala/response');
var router = createRouter();

router.get('/greeting', function () {
    return response.html('<h1>Hello World!</h1>');
});

exports.router = router;
```

首先从 coala/router 模块中取出 createRouter 方法，这是创建 router 对象的入口方法；然后获取 coala/response 模块，用来为请求提供响应。调用 createRouter 方法创建一个 router（疑问：createRouter 方法是否可以调用多次？），并调用 get 方法为所有访问到 /greeting 地址的请求输出一个 HTML 片段响应（不完全正确，见下文解释）。该程序运行效果如图所示：

（暂无图片）

这里需要注意的是 router 的 get 方法，这个方法的名称很容易让人产生误解，以为是从 router 中获取某些数据，其实不然。这里的 get 方法对应于 HTTP 协议中的 GET 请求，因此该方法的含义是指：所有（且只有）通过 GET 方法访问 /greeting 地址的请求，才会被后面的回调方法处理，从而返回期待的 Hello World 页面。另外，在调用 get 方法的时候其实并不会立即触发任何请求和响应，而仅仅是在 router 的内部结构中定义了一个映射关系：当有 GET 请求到达 /greeting 地址的时候，就执行相应的回调方法，生成响应。因此回调方法真正被调用的时候，是在请求真正发生的时候。这个时间点通常会晚于 get 方法调用的时间点。

[HTTP/1.1](http://www.ietf.org/rfc/rfc2616.txt) 协议中定义了四种常用的请求方式（HTTP Methods）：GET、POST、PUT 和 DELETE，分别对应 router 中的 get、post、put 和 **del** 方法。（<span class="label label-info">提示</span> **因为 delete 是 JavaScript 的关键字，所以没办法使用 delete 全称，只能使用缩写 del。**）前两种方法是我们比较熟悉也是经常使用的，但后两种就相对陌生。即便是我们熟悉的 GET 和 POST 方法也经常有人搞不清楚他们的具体含义和应用场景，因此在这里把这四种 HTTP/1.1 协议中定义的请求方式再详细描述一下：

- **GET** \- 使用 GET 方法请求一个地址，正如其字面所言，应该是获取存在于该地址的某些资源或数据，可以通过 ? 在请求的 URL 后面添加参数，以访问不同的资源或返回不同的数据。除此之外，更深层次的含义，每次 GET 请求都不应该对服务器的状态产生任何影响，也就是说对某个资源进行 0 次 GET 请求、1 次 GET 请求或若干次 GET 请求得到的结果都应该是一样的，否则就不应该使用 GET。因此我们称 GET 请求具有安全和幂等的特性。
- **POST** \- 使用 POST 方法请求一个地址，通常是要向该地址发送数据，并且这些数据要能够对服务端的数据产生影响，通常是用来新增一些数据，或者完成其他三种方法无法完成的任务，比如批量操作等。因此对一个地址执行 0 次、1 次或若干次 POST 请求，其结果都可能会不一样。因此 POST 请求既不是安全的也不是幂等的。
- **PUT** \- 使用 PUT 方法请求一个地址，也会通过请求体（request body）向服务端发送数据，与 POST 方法不同的是，通常 PUT 方法用来处理一些诸如更新和修改的操作。可以想像对某一个地址执行 0 次和执行 1 次 PUT 请求，其结果是不同的，因此 PUT 请求不是安全的；但是执行 1 次和执行若干次 PUT 请求产生的结果却应该是相同的，因此 PUT 请求是幂等。
- **DELETE** \- 顾名思义，使用 DELETE 方法请求一个地址，就是要删除该地址对应的资源或数据。同 PUT 请求类似，DELETE 请求也是不安全但幂等的（请读者自行分析）。

router 中定义的 get、post、put 和 del 方法，具有相同的方法签名，以 put 为例：

```javascript
router.put(path, function () {
    // 处理到 path 路径的 PUT 请求
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
router.get('/', function (request) { ... });
router.get('/users/:userId', function (request, userId) { ... }); // 声明 request 和 userId 参数
router.get('/users/:userId.:format?', function (request, userId, format) { ... }); // 声明 request, userId 和 format 参数，其中 format 参数是可选的
router.put('users/:userId', function (request, userId) { ... }); // 处理 put 请求
router.del('users/:userId', function (request, userId) { ... }); // 处理 del 请求
```

<span class="label label-info">提示</span> **占位符的匹配值是按照占位符出现的顺序依次在请求处理方法的签名中出现的，与占位符名称和参数名称是否相同无关。**

这里出现的 request 对象是 [JSGI](http://wiki.commonjs.org/wiki/JSGI) 规范中定义的，主要有如下一些属性：

- **.params** \- 以键值对方式存储的所有查询字符串中的参数以及请求体中的参数
- **.env.servletRequest** \- 当需要使用底层 Servlet API 的时候，获取 ServletRequest 类型的请求对象
- **.env.servletResponse** \- 当需要使用底层 Servlet API 的时候，获取 ServletResponse 类型的响应对象

请求处理方法返回一个对象作为响应，该对象需要包含三个必有属性：

- **status** \- HTTP Status Code，用来表示响应的状态。例如：200 代表成功，404 代表请求的资源未找到等
- **headers** \- HTTP Response Header，响应的头信息
- **body** \- HTTP Response Body，响应的主体内容

```javascript
router.get('/users/:userId', function (req, userId) {
    var user = findByUserId(userId); // 自定义方法
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

使用如上的方法获取 response 工具对象，该对象包含如下一些方法：

#### charset()

如果调用此方法时不传入参数，则返回当前正在使用的字符编码（默认为 UTF-8）；如果传入参数，则将该参数设置为当前字符编码。字符编码会跟随在响应头信息的 Content-Type 字段处。

```javascript
var currentChartset = response.charset(); // 返回当前使用的字符编码，默认为 UTF-8

response.charset('GB2312'); // 设置当前使用的字符编码为 GB2312
currentCharset = response.charset() // 返回 GB2312
```

#### html()

返回 HTML 格式的响应。当仅有一个传入参数，且该参数是一个 JavaScript 对象的时候，方法使用此对象作为请求响应。与规范中定义的不同之处在于，除了 body 字段之外，status 和 headers 字段都是可选的。如果 status 字段省略则默认为 200，如果 headers 字段省略则默认为 Content-Type: text/html。当传入参数只有一个，但不是 JavaScript 对象的时候，或者有多个传入参数，则方法将这些参数对应的字符串连接后输出为 body 的内容，status 取默认的 200，headers 取默认的 Content-Type: text/html。

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

#### xml()

返回 XML 格式的响应。与 html() 方法雷同，只是 headers 默认取值为 Content-Type: application/xml。

#### redirect()

返回 303 重定向响应，将页面重定向到指定路径。该方法需要一个重定向的目标路径作为参数。

```javascript
response.redirect('/sso/acounts/openid/signin');
```

#### notFound()

返回 404 请求路径未找到响应，该方法需要一个未找到的路径地址作为参数。

```javascript
response.notFound('/users/steve-jobs');
```

#### error()

返回 500 服务器错误响应，该方法需要一个错误消息作为参数。

```javascript
response.error('系统发生致命错误，请联系管理员！');
```

#### json()

在介绍此方法之前，需要先对本框架使用的 JSON 序列化组件有一个简单的认识。本框架的 JSON 序列化采用了 Jackson 库的 filter 功能，就是可以根据指定的 filter 名称和包含的以及排除的字段来自动生成 JSON 结果。其中的 filter 可以想像为一组属性的集合，通常定义在实体类上，例如：

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

以上代码定义了两个 filter，分别命名为 user 和 group，名为 user 的 filter 包含五个属性，分别是 id（继承自 DomainEntity），userName，password，age 和 groups；名为 group 的 filter 包含三个属性，分别是 id（继承自 DomainEntity），groupName 和 users。

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
        group: ['id', 'groupName', 'users']
    },
    exclude: {
        user: ['password']
    }
});
```

这段代码的含义就是生成的 JSON 结果要包含 Group 对象里面的 id, groupName 和 users 属性，而去掉 User 对象里面的 password 属性。有关 [Jackson](http://jackson.codehaus.org/ "Jackson JSON Processor") 及 [JsonFilter](http://wiki.fasterxml.com/JacksonFeatureJsonFilter "JacksonFeatureJsonFilter") 的更多用法请参考官方文档。

#### stream()

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

<span class="label label-important">注意</span> **不要在 callback 方法中手动关闭 outputStream 流，当响应完毕后，系统会自动将其关闭。**

### 3.5、路由、模块和自动挂载

根据以上的介绍，我们应该了解到，应用程序的入口点是 main.js，应用程序由模块和子模块构成，并形成树状结构，在每个模块下都包含 \_\_routers\_\_、\_\_services\_\_ 和 \_\_managers\_\_ 等目录，在 \_\_routers\_\_ 目录中定义有所有的路由和请求处理逻辑，但问题是这些路由、模块以及主应用程序是如何衔接在一起并配合工作的？简单回答就是：**全自动**。

详细一点来说，框架启动时会加载 main.js 启动主应用程序，主应用程序会根据模块的目录结构加载模块，并自动将该模块的访问路径添加到访问路径中，然后会遍历模块 \_\_routers\_\_ 目录下的所有文件，挂载每个文件导出的 router 对象（这就是为什么前面介绍 router 的时候要求每个 router 文件必须导出这个对象的原因了）。自动挂载的最终结果就是请求的访问路径，看上去和模块的目录结构是相同的。如下所示：

![](assets/images/user-guide/3-router-mount.png)

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

### 3.6、标记（Marker）

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

#### tx

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

<span class="label label-info">提示</span> **有关 propagationBehavior 和 isolationLevel 的枚举值请参考 [TransactionDefinition](http://static.springsource.org/spring/docs/3.1.x/javadoc-api/org/springframework/transaction/TransactionDefinition.html)**

如果使用了 needStatus 配置，可以在 on 后面的方法中接收这个 TransactionStatus 参数。使用方法如下：

```javascript
mark('tx', {needStatus: true}).on(function (status) {
    ...
});
```

#### beans

该 marker 向方法注入 Spring 容器中声明的 Java Bean 对象。

```javascript
mark('beans', 'bean1', 'bean2', 'bean3').on(function (bean1, bean2, bean3) {
    ...
});
```

此 marker 的第一个参数表明了该 marker 的类型，后面是一组变参，将需要注入的 Java Bean 的 ID 罗列在此，后面的方法就可以按次序接收到注入的 Java Bean 对象。

#### services

该 marker 向方法注入 service 对象，关于 service 对象的描述请参考有关章节。

```javascript
mark('services', 'modulePath1:serviceName1', 'modulePath2:serviceName2', 'modulePath3:serviceName3').on(function (service1, service2, service3) {
    ...
});
```

此 marker 的第一个参数表明了该 marker 的类型，后面也同样是一组变参。其中的 service 使用了一种特殊的表示方法，称为“坐标”。该坐标由两部分组成，以冒号（:）分割。冒号的左边是该 service 所在模块的绝对路径，冒号右边是该 service 的文件名。这样的写法会带来如下一些好处：

- 屏蔽了 service 的具体查找路径，也就是免去了在路径中写入 \_\_services\_\_ 这样的目录
- 强制要求 service 一定放置在 \_\_services\_\_ 目录，突出框架的规范性要求

此 marker 要求注入的 service 都必须导出一个名为 createService 的方法。

#### managers

该 marker 向方法注入 manager 对象，关于 manager 对象的描述请参考有关章节。

```javascript
mark('managers', User, Group, 'modulePath:managerName').on(function (userManager, groupManager, otherManager) {
    ...
});
```

同上，此 marker 的第一个参数也表明了该 marker 的类型，后面也是一组变参，该参数里面可以包含两种类型的对象。首先可以指定一种实体类型，则框架会自动注入一个通用的 manager 用来访问该实体，另外如果是指定一个到 manager 的坐标（同 service 的坐标概念类似），则框架注入该坐标指向的 manager。同注入 service 的 marker 类似，这里也要求自定义的 manager 文件必须导出一个名为 createManager 的方法。

### 3.7、服务（Service）

service 在本框架中充当业务逻辑层的角色，所有与业务有关的代码都应该在这里完成。但是 service 中的代码不应该参与访问底层存储或其它基础组件，这部分工作应该是由 manager 来完成的，service 应该只是调用 manager 里面的方法而已。为了使得 marker 可以自动注入 service，要求每个 service 文件必须导出一个名为 createService 的方法，marker 在每次注入的时候都会调用该方法，生成一个新的 service 实例，因此 service 的注入模式是 protoytype 的，而不是 singleton 的。

所有的 service 都要求存放在模块的 \_\_services\_\_ 目录下，并应该按照功能分类进行合并与拆分，每个 service 文件的命名，框架并没有强制要求。

```javascript
// system/users/__services__/user-service.js

exports.createService = function () {
    return {
        createUser: mark('managers', User).mark('tx').on(function (userManager, user) { ... }),
        editUser: mark('managers', User).mark('tx').on(function (userManager, id, newUser) { ... }),
        deleteUser: mark('managers', User).mark('tx').on(function (userManager, id) { ... }),

        showUser: mark('managers', User).on(function (userManager, id) { ... }),
        listUsers: mark('managers', User).on(function (userManager) { ... })
    };
}
```

以上代码定义了一个 user service 用来处理 User 实体的基本添、删、改、查等操作。我们来详细分析此段代码的含义。

首先，根据 marker 的要求，service 必须导出名为 createService 的方法，用来在注入的时候创建 service 对象。这里的 createService 直接返回了一个普通的 JavaScript 字面对象，该对象包含 5 个方法，用来分别处理创建用户、编辑用户、删除用户、显示用户和用户列表的业务操作。这些都是比较容易理解的，难点在于这些方法与 marker 的配合使用。在 marker 的章节中我们提到过，managers marker 用来向方法中注入 manager 对象，而 tx marker 用来使得方法运行在事务中，而且 marker 可以串联使用。拿 createUser 方法为例，marker 向该方法中注入了通用的 user manager 对象，同时使得该方法被事务控制起来；而后面的 showUser 和 listUsers 两个方法，只注入了 user manager 对象，而没有注入事务，这是因为这两个方法进行的是只读操作，为了提升性能，无需在这里显示声明事务。但问题并没有就此结束，细心的读者一定发现，除了被注入的 userManager 对象之外，很多 on 后面的方法还声明了多余的参数，这些参数的作用何在？而且如何使用？

回想前面讲解 marker 时的所说的，marker 的返回值是什么。marker 返回一个被装饰过的方法，该方法绑定了那些被注入的参数，而那些未被注入的参数，则留给新方法作为参数使用。还是不太明白，那么我们可以认为当调用 createService 方法之后，返回的 userService 对象具有类似如下的方法签名：

```javascript
userService = {
    createUser: function (user) { ... },
    editUser: function (id, newUser) { ... },
    deleteUser: function (id) { ... },

    showUser: function (id) { ... },
    listUsers: function () { ... }
}
```

调用 userService 里面的方法的时候，就只需传入那些没有被注入的参数就可以了。

service 一般是在 router 中进行使用的，配合上面的例子，对应的 user-router.js 应该看起来像这个样子：

```javascript
// system/users/__routers__/user-router.js

var {createRouter} = require('coala/router');
var router = createRouter();

router.post('/', mark('services', '/system/users:user-service').on(function (userService) { ... }));
router.put('/:userId', mark('services', '/system/users:user-service').on(function (userService, userId) { ... }));
router.del('/:userId', mark('services', '/system/users:user-service').on(function (userService, userId) { ... }));

router.get('/:userId', mark('services', '/system/users:user-service').on(function (userService, userId) { ... }));
router.get('/list', mark('services', '/system/users:user-service').on(function (userService) { ... }));

exports.router = router;
```

请读者根据前面的思路，自行分析以上代码的含义。

### 3.8、管理器（Manager）

manager 在本框架中充当数据访问层的角色。大多数情况下，为了完成基本的添、删、改、查等操作，一般无需自定义 manager，直接使用通用的就可以了。

通用 manager 还有一个功能就是可以动态加载并自动调用 orm.xml 文件中定义的 named query。JPA 规范中允许将 named query 以配置文件的形式保存到 orm.xml 文件中，这样做可以带来若干好处：比如可以很方便的集中编辑所有的 HQL/SQL 语句，而且预先定义的 HQL 语句会在系统启动的时候进行预编译，提高运行期的语句解析速度。但是随之产生一个问题，就是该文件改动以后，必须重新启动系统才可以重新加载，这个过程显然有悖于本框架的基本理念。因此框架在实现通用 manager 的时候在这方面进行了加强。只要 orm.xml 文件的内容被修改过，调用 manager 方法的时候就会自动加载这些变更了的文件。这种形式的动态加载自然会对运行效率有一定的影响，因此系统可以通过参数配置运行的模式，只有运行在开发模式下，此项功能才会启用。

通用的 manager 还充分利用了 JavaScript 语言的动态性，声明在 orm.xml 文件中的 named query 的名称，可以直接当作 manager 的方法名来使用。比如在某个 orm.xml 文件中声明了一个名为 findByUserName 的 named query，那么就可以直接在通用的 manager 里面调用以 findByUserName 为名称的方法，而且 query 中定义的命名参数，可以以一个 JavaScript 对象的形式传入。

当通用 manager 无法满足要求的时候，框架也提供了扩展机制。想要扩展一个 manager 就需要在模块的 \_\_managers\_\_ 目录下创建一个文件，为了满足 marker 的要求，该文件要导出名为 createManager 的方法。

```javascript
exports.createManager = mark('managers', User).on(function (userManager) {
    return userManager.mixin({
        method1: function (entityManager) { ... },
        method2: function (entityManager) { ... }
    });
});
```

可以看到这里仍然使用了 managers mark 来注入一个通用的 manager，原因是我们希望扩展的 manager 仍然具有通用的 manager 的所有功能。在这里调用了通用 manager 内置的一个方法 mixin，顾名思义就是将当前对象与参数中的对象的属性和方法进行混合，即返回的结果是一个新的对象，该对象拥有这两个对象的属性和方法的并集。而且 mixin 里面的方法会被自动注入 EntityManager 对象以便对底层数据库进行访问。

<span class="label label-important">注意</span> **在 manager 的方法中不要进行任何与事务有关的操作，这些操作应该是在 service 层进行的。**
