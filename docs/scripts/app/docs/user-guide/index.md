Zyeeda Framework 2.0 用户手册
=============================

Zyeeda Framework 2.0 是公司全新研发的技术平台与开发框架。在设计与研发过程中，始终秉承如下一些理念：

- **Don't Make Me Think.** 这是本平台的核心开发理念，在任何情况下使用该平台，都不希望平台本身为开发和使用人员带来过多困扰。无论从用户界面、系统结构以及应用程序接口（API）方面，都力求清晰易懂，不引入或少引入陌生概念，各种术语也力争与外界标准相兼容。
- **Only One Way to Do it.** 在本平台的约束下，实现任何功能，都有且只有唯一的一种方式和途径。这是对上一条理念的升华，开发人员在日常工作中，都曾碰到过这样的情况：有多余一种途径可以实现某个客户需求，那么究竟哪一种更好，哪一种稍差？技术水平低一些的人员可能无从分辨，只能盲目选择或者选择自己熟悉的；而技术水平较高的人员则会进行一番论证然后选择最优的办法。前者无疑是不可靠的，而后者又浪费了优势资源的时间，因此在设计和实现本平台的过程中，就将最佳实践一起引入，指导开发人员使用唯一途径实现指定需求。
- **Minimize Your Work.** 平台的封装尽量使得开发人员使用最少的代码编写最多的功能。秉承这个理念，可以大大降低开发人员的工作量，从而缩短项目工期，在同样的周期时间内花更少的资源完成更多的项目。

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
- [Stick](https://github.com/hns/stick "Stick") 是一套基于 Ringo 的模块化 [JSGI](http://wiki.commonjs.org/wiki/JSGI/Level0/A/Draft2 "JavaScript Gate Interface") 中间件组装层和应用程序框架。


1、准备环境
-----------

### 1.1、JDK

本框架需要使用 JDK 1.6 或以上版本进行编译和运行，请确认系统中正确配置了 Java 开发和运行环境。

### 1.2、Maven

[Apache Maven](http://maven.apache.org/ "Maven") 是一款基于项目对象模型（Project Object Model, POM）的项目管理工具，本框架的编译需要使用 Maven。有关 Maven 的配置与运行，请参考[官方文档](http://maven.apache.org/download.html "Download and Install Maven")。

在内网环境下使用时，建议配置 Maven 的镜像服务器到内网私服。打开 Maven 安装目录下的 conf/settings.xml 文件，找到 &lt;mirrors&gt; 节点，增加如下配置：

```xml
<mirror>
    <id>zyeeda.internal.repo</id>
    <url>http://10.1.2.11:8081/nexus/content/groups/public</url>
    <mirrorOf>*</mirrorOf>
</mirror>
```

配置好 Maven 的镜像服务器后，可以在项目的 POM 文件中以如下的方式引用本框架：

```xml
<dependency>
    <groupId>com.zyeeda</groupId>
    <artifactId>zyeeda-framework</artifactId>
    <version>2.0.0.B1</version>
</dependency>
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

上面的代码的功能是将 user 对象的两个属性 firstName 和 lastName 分别赋值给两个和属性同名的变量。设想一种情况，假如 user 具有非常多的属性，想要进行类似的赋值，就必须写很多行赋值语句，而且每一行都要包含相同的对 user 对象的引用。针对这种情况，JavaScript 1.7 版本以后，开始引入一种新的操作，称为“解构赋值”。关于解构赋值的更多信息，请参考[这里](https://developer.mozilla.org/en/New_in_JavaScript_1.7, "New in JavaScript 1.7")。

```javascript
var {firstName, lastName} = user;

// 以上写法等同于
var firstName = user.firstName;
var lastName = user.lastName;
```

3、Starter Kit
---------------

为了更好的理解和使用本框架，与框架代码一起发布的还有一个 Starter Kit 新手包。该系统是一个可以独立运行的 Web 应用程序，构建于本框架的基础结构之上，主要作用是用来展示框架的组成结构和各种配置文件的使用方法。可以通过如下方式获取该项目的源代码：

```
hg clone http://10.1.2.13/hg/zyeeda-starter-kit-1.0
```

想要运行该系统也非常容易，只需要在项目根目录下运行命令：

```
mvn jetty:run
```

然后在浏览器中访问 http://localhost:8080 即可。


4、项目工作区
-----------------

基于本框架构建的项目，工作区要遵守一定的目录结构规范。以新手包为例：

![新手包](assets/images/user-guide/1-starter-kit.png)

这是一个典型的 Maven 项目，在 src/main 目录下有六个子目录，各自用途如下：

- **java** \- Java 文件目录。所有由 Java 语言编写的程序都要放到此目录下，比如领域实体等
- **processes** \- 流程定义文件目录，所有使用流程设计器或手写的流程定义文件（及相应的 png 或 svg 预览图）都应存放在此目录下
- **resources** \- 配置文件及静态资源文件目录。Spring、JPA、日志等配置文件或模板文件等都存放在此目录下
- **rules** \- 规则定义文件目录
- **sql** \- SQL 脚本目录。系统初始化及升级时使用的 SQL 脚本，应该放置在此目录下
- **webapp** \- Web 应用程序根目录


5、后端框架详细介绍
-------------------

### 5.1、实体设计

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

作为父类，DomainEntity 实现了 Serializable 接口，这样所有继承自 DomainEntity 的实体类就都可以支持序列化了。每个基础实体类都拥有一些预定义属性，可以按需选取继承。上表列出了每个实体类及其属性的概要信息。建议通过继承基础实体类的方式来达到业务实体类可序列化的目的，而且通过继承也免去了开发人员重复定义一些常用属性的麻烦。

#### 5.1.1、DomainEntity

DomainEntity 实现了 Serializable 接口，并定义了一个 id 属性（默认采用 UUID 的生成策略）。所有自定义业务实体类都应该继承 DomainEntity。

#### 5.1.2、SimpleDomainEntity

SimpleDomainEntity 继承自 DomainEntity，并且扩展了 name 和 description 属性。有这两个属性需求的实体类型应该继承该类型。

#### 5.1.3、RevisionDomainEntity

RevisionDomainEntity 继承自 SimpleDomainEntity，并且扩展了 creator，createdTime，lastModifier 和 lastModifiedTime 四个属性。与前面两者的不同之处在于，该类型不仅仅扩展了四个与修订信息有关的字段，而是在系统运行时，这些字段的值会根据登录用户的上下文状态自动填充。因此有需要记录修订信息的业务实体，应该继承此类型。

#### 5.1.4、如何自定义业务实体类

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

通过继承 DomainEntity，People 类型无需再定义 id 属性，而且已经实现了 Serializable 接口。

<span class="label label-important">注意</span> 表名和字段名需要遵守一定的命名规范：标识符全部采用大写字母，表名前增加 ZDA\_ 前缀（或其他项目相关前缀），字段名前增加 F\_ 前缀。标识符总长度不超过 32 个字符，以便与 Oracle 数据库兼容。并且所有标记要打在 getter 方法上，而不是属性字段上。

<span class="label label-info">提示</span> 以上代码中的所有实体关系映射（Object Relationship Mapping, ORM）都是通过 Java 注解表示的，开发人员需要对 Hibernate Annotations 和 Hibernate JPA Annotations 有一定的了解。请参考以下网站获取更多信息：

- [Hibernate Annotations](http://www.techferry.com/articles/hibernate-jpa-annotations.html "Hibernate Annotations")
- [Hibernate JPA Annotations](http://docs.jboss.org/hibernate/annotations/3.5/reference/en/html_single/#entity-mapping "Hibernate JPA Annotations")

### 5.2、web.xml

在前面介绍 Maven 工作区的时候，其实可以看出，基于本框架构建的项目是一个典型的 Java Web Application。按照规范，必须在 WEB-INF 目录下存在 web.xml 文件，该文件包含了整个应用程序的启动和描述信息。本框架需要引入的 web.xml 文件的内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<web-app version="2.5"
    xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
    xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">

    <!-- 应用程序的显示名称 -->
    <display-name>Java Web Application</display-name>

    <!-- 在 ServletContext 启动/关闭的时候，触发构建/销毁 Spring ApplicationContext 的监听器 -->
    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <!-- 初始化 Spring ApplicationContext 需要使用的配置文件的路径 -->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:spring/application-context.xml</param-value>
    </context-param>

    <!-- 定义 Open Session in View Filter -->
    <filter>
        <filter-name>osiv</filter-name>
        <filter-class>org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>osiv</filter-name>
        <url-pattern>/invoke/*</url-pattern>
    </filter-mapping>

    <!-- 定义 JSGI Filter -->
    <servlet>
        <servlet-name>jsgi</servlet-name>
        <servlet-class>com.zyeeda.cdeio.web.SpringAwareJsgiServlet</servlet-class>
        <init-param>
            <param-name>debug</param-name>
            <param-value>true</param-value>
        </init-param>
        <init-param>
            <param-name>production</param-name>
            <param-value>false</param-value>
        </init-param>
        <init-param>
            <param-name>verbose</param-name>
            <param-value>true</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>jsgi</servlet-name>
        <url-pattern>/ns/invoke/*</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>jsgi</servlet-name>
        <url-pattern>/invoke/*</url-pattern>
    </servlet-mapping>

</web-app>
```

#### 5.2.1、Open Session in View

Open Session in View（以下简称 OSiV）是一种架构模式，用来解决开发人员在进行数据库访问操作时，打开数据库 session 之后忘记关闭，导致资源泄漏的问题；还可以解决在视图层访问具有延迟加载特性的实体属性时，session 不存在或已关闭的问题（这也是 Open Session in View 名称的由来）。

Java Web Application 是以线程来处理每个用户请求的，而线程又是程序执行中的最小单位，因此在用户请求到达的最开始打开数据库 session，并将其保存在当前线程中，直到请求结束后再自动关闭，这样在整个请求过程中就总会有一个可用的 session 存在，无论是在数据访问层、业务处理层还是视图层，而且不需要开发人员来手动控制其开启和关闭。

Open Session in View 通常是以 filter 的形式实现的。在使用 JPA 2.0 接口环境的上下文中，也通常把 Open Session in View 改称为 Open EntityManager in View，这是因为 JPA 中的 EntityManager 就是充当 session 来使用的。

在上面的配置中，使用了 Spring 框架提供的 OpenEntityManagerInViewFilter 来实现 Open Session in View 的功能。并且将此 filter 映射到了以 /invoke 开头的请求路径中，这样所有经过 /invoke 访问的请求就都会默认开启数据库访问 session。

<span class="label label-info">提示</span> 有关 Open Session in View 的更详细讨论，请参考[这里](https://community.jboss.org/wiki/OpenSessionInView "Open Session in View")。

#### 5.2.2、JSGI

可以把 JSGI 跟 JSP 做一个简单类比：JSP 是使用 Java 语言来实现动态网页技术的，而 JSGI 是使用 JavaScript 语言来实现的。通常在 Java SE 标准兼容的应用程序服务器中（比如 Apache Tomcat 或者 Jetty），JSP 的支持是由 servlet 来提供的，所以 JSGI 也是一样。在上面的配置文件中，声明了一个 SpringAwareJsgiServlet 类型的 servlet，并在系统启动的时候自动加载（&lt;load-on-startup&gt;1&lt;/load-on-startup&gt;）。该 servlet 具有如下一些配置参数：

- **debug** \- 是否处于调试模式。如果在处于调试模式下，系统启动的时候会同时启动一个 JavaScript 调试器。默认 false
- **production** \- 是否处于生产模式。如果处于生产模式，程序代码变更后不会自动重载。默认 false
- **verbose** \- 是否输出较多日志。默认 false

JSGI servlet 默认被配置到两组 URL 路径中：以 /invoke 开头的路径除了会经过上面介绍的 Open Session in View filter 之外，在这里会被 JSGI servlet 处理；另外的以 /ns/invoke 开头的路径，也会被此 servlet 处理，但是因为不包含在 /invoke 路径下，所以不会经过 Open Session in View filter，也就不会默认打开用于数据库访问的 session，开发人员可以在这里自行控制 session 的生命周期，所以这里 ns 的意思就是 no session。

### 5.3、应用程序入口点

JSGI servlet 在启动的时候会默认加载 WEB-INF/app 目录，并寻找名为 main.js 的启动文件。该文件**必须**导出名为 app 的对象，用来供 JSGI servlet 启动整个系统。所以 main.js 就是整个后端应用程序的入口点。

main.js 的内容通常来说只有一行，看上去是这个样子的：

```javascript
exports.app = require('cdeio/router').createApplication(this);
```

<span class="label label-important">注意</span> exports.app 是 main.js 必须的。

<span class="label label-info">提示</span> 下文如无特别说明，所有关于服务器端 JavaScript 的绝对路径引用，都是以 WEB-INF/app 作为根目录的。

### 5.4、组成结构

基于本框架开发的项目称为应用程序（application）。应用程序包含若干模块（module），而模块下面还可以有子模块，由此形成一棵以应用程序为根的模块树。每个模块下会包含三个系统预定义目录： \_\_routers\_\_，\_\_services\_\_ 和 \_\_managers\_\_，分别代表了支撑模块实现功能的三层结构，即：路由（router），服务（service）和管理器（manager）。

<span class="label label-important">注意</span> 这里模块的概念要区别于前文提到的 CommnsJS 中的模块，这里的模块是指业务上的模块。

![框架组成结构](assets/images/user-guide/2-webapp.png)

- **router** \- 请求路由层。定义请求的分发与处理规则，是每个请求的入口点。在 router 层中应该处理与请求或响应本身有关的逻辑，比如信息验证和/或数据格式转换等，而不应该处理任何与业务逻辑有关的内容，这样的代码应该写在 service 层中。
- **service** \- 业务逻辑处理层。所有与业务逻辑相关的代码都应该写在 service 层中，但是不包含访问底层持久化等组件的功能，这些是 manager 层需要处理的事情。最重要的，任何有关事务的处理工作都应该在 service 层中完成。
- **manager** \- 数据持久化层，或底层调用层。任何需要访问持久化组件，或需要跟其它底层组件发生交互而完成的任务应该在 manager 层中实现。

<span class="label label-important">注意</span> 这三层结构是有明确边界的，之间应该是顺序调用的（router \-&gt; service \-&gt; manager），而且分工明确，不应彼此渗透。良好的架构模式有助于代码的可重用性、可扩展性与可维护性。

### 5.5、路由（Router）

与 router 有关的脚本全部存放在每个模块下的 \_\_routers\_\_ 目录中，应该根据业务功能特征对 router 进行划分，并选取适当的文件名，这些文件名会作为访问路径的一部分，出现在最终请求的 URL 里面。每个 router 文件都必须导出一个名为 router 的对象（使用 createRouter 方法创建，见下文），以便自动自动加载机制可以加载到。

router 是用来分发和处理请求的，有些类似于 Spring 中的 DispatcherServlet。当有请求来到服务端的时候，router 会拦截所有请求，并根据其中定义的规则，分发请求到一个请求处理方法，该请求处理方法会调用 service（进而是 manager）来实现业务逻辑处理，并最终将响应返回给客户端。下面的代码实现了最简单的 Hello World 功能：

```javascript
var {createRouter} = require('cdeio/router');
var response = require('cdeio/response');
var router = createRouter();

router.get('/greeting', function () {
    return response.html('<h1>Hello World!</h1>');
});

exports.router = router;
```

首先从 cdeio/router 模块中取出 createRouter 方法，这是创建 router 对象的入口方法；然后获取 cdeio/response 模块，用来为请求提供响应。调用 createRouter 方法创建一个 router，并调用 get 方法为所有访问到 /greeting 地址的请求输出一个 HTML 片段响应（不完全正确，见下文解释）。该程序运行效果如图所示：

![运行效果](assets/images/user-guide/3-hello-world.png)

这里需要注意的是 router 的 get 方法，这个方法的名称很容易让人产生误解，以为是从 router 中获取某些数据，其实不然。这里的 get 方法对应于 HTTP 协议中的 GET 请求，因此该方法的含义是指：所有（且只有）通过 GET 方法访问 /greeting 地址的请求，才会被后面的回调方法处理，从而返回期待的 Hello World 页面。另外，在调用 get 方法的时候其实并不会立即触发任何请求和响应，而仅仅是在 router 的内部结构中定义了一个映射关系：当有 GET 请求到达 /greeting 地址的时候，就执行相应的回调方法，生成响应。因此回调方法真正被调用的时候，是在请求真正发生的时候。这个时间点通常会晚于 get 方法调用的时间点。

[HTTP/1.1](http://www.ietf.org/rfc/rfc2616.txt) 协议中定义了四种常用的请求方式（HTTP Methods）：GET、POST、PUT 和 DELETE，分别对应 router 中的 get、post、put 和 **del** 方法。（<span class="label label-info">提示</span> 因为 delete 是 JavaScript 的关键字，所以没办法使用 delete 全称，只能使用缩写 del。）前两种方法是我们比较熟悉也是经常使用的，但后两种就相对陌生。即便是我们熟悉的 GET 和 POST 方法也经常有人搞不清楚他们的具体含义和应用场景，因此在这里把这四种 HTTP/1.1 协议中定义的请求方式再详细描述一下：

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

<span class="label label-info">提示</span> 占位符的匹配值是按照占位符出现的顺序依次在请求处理方法的签名中出现的，与占位符名称和参数名称是否相同无关。

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
        body: '{userId: ' + user.userId + ', name: ' + user.userName + ', age: ' + user.age + ', gender: ' + user.gender + '}'
    }
});
```

使用这种方式返回响应，需要对 HTTP 协议有深入的了解，尤其需要了解各请求和响应的头信息的含义。而且此处 body 字段的内容必须是静态的数据，如果需要返回流式数据的话，仅仅使用 body 就无能为力了。因此框架引入了更方便的方式对请求进行响应。

```javascript
var response = require('cdeio/response');
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
@JsonFilter("userFilter")
public class User extends DomainEntity {
    private String userName;
    private String password;
    private int age;
    private List<Group> groups;

    // 省略了相应的 getter 和 setter 方法
}

@JsonFilter("groupFilter")
public class Group extends DomainEntity {
    private String groupName;
    private List<User> users;

    // 省略了相应的 getter 和 setter 方法
}
```

以上代码定义了两个 filter，分别命名为 userFilter 和 groupFiter，名为 userFilter 的 filter 包含五个属性，分别是 id（继承自 DomainEntity），userName，password，age 和 groups；名为 groupFilter 的 filter 包含三个属性，分别是 id（继承自 DomainEntity），groupName 和 users。

了解了 filter 的含义，再来看此方法的定义，此方法需要传入两个参数：

- **object** \- 待序列化的实体对象，可以是 JavaScript 对象也可以是 Java 实体对象
- **config** \- 序列化过程的配置信息
    - **include** \- 需要包含在序列化结果中的字段列表
    - **exclude** \- 不需要包含在序列化结果中的字段列表
    - **status** \- HTTP Status Code，可选，默认为 200
    - **headers** \- 响应头信息，可选，默认为 Content-Type: application/json

include 和 exclude 的类型都是 JavaScript 对象，对象的键是 filter 的名称，值为属性列表。需要注意的是，同一个名称的 filter 不能同时出现在 include 和 exclude 中。承接上例：

```javascript
response.json(user, { // user 是 User 类型的一个实例
    include: {
        groupFilter: ['id', 'groupName', 'users']
    },
    exclude: {
        userFilter: ['password']
    }
});
```

这段代码的含义就是生成的 JSON 结果要包含 Group 类型对象里面的 id, groupName 和 users 属性，而去掉 User 类型对象里面的 password 属性。有关 [Jackson](http://jackson.codehaus.org/ "Jackson JSON Processor") 及 [JsonFilter](http://wiki.fasterxml.com/JacksonFeatureJsonFilter "JacksonFeatureJsonFilter") 的更多用法请参考官方文档。

#### stream()

以流的形式返回响应。该方法的第一个参数是 JSGI request 对象，第二个参数可以是一个对象或者一个方法。如果是一个对象，则包含如下字段：

- **status** \- HTTP Status Code，可选，默认为 200
- **headers** \- 响应头信息，可选，默认为 Content-Type: binary/octet-stream
- **callback** \- 数据流处理方法，该方法会被传入 request.env.servletResponse.getOutputStream() 对象，用来向响应中以流的方式写入数据

如果第二个参数是一个方法，其含义就相当于上文中的 callback。

```javascript
response.stream(request, {
    status: 200,
    headers: {
        'Content-Type': 'application/ms-word'
    },
    callback: function (outputStream) { // outputStream = request.env.servletResponse.getOutputStream()
        // 向 outputStream 中写入数据流
    }
});
```

<span class="label label-important">注意</span> 不要在 callback 方法中手动关闭 outputStream 流，当响应完毕后，系统会自动将其关闭。

### 5.6、路由、模块和自动挂载

根据以上的介绍，我们应该了解到，应用程序的入口点是 main.js，应用程序由模块和子模块构成，并形成树状结构，在每个模块下都包含 \_\_routers\_\_、\_\_services\_\_ 和 \_\_managers\_\_ 目录，在 \_\_routers\_\_ 目录中定义有所有的路由和请求处理逻辑，但问题是这些路由、模块以及主应用程序是如何衔接在一起并配合工作的？简单回答就是：**全自动**。

详细一点来说，框架启动时会加载 main.js 开启主应用程序，主应用程序会根据模块的目录结构加载模块，并自动将该模块的路径添加到访问路径中，然后会遍历模块下 \_\_routers\_\_ 目录，按文件名挂载每个 router 文件导出的 router 对象（这就是为什么前面介绍 router 的时候要求每个 router 文件必须导出这个对象的原因了）。自动挂载的最终结果就是请求的访问路径，看上去和模块的目录结构是相同的。如下所示：

![自动挂载](assets/images/user-guide/4-auto-mount.png)

图中所示的应用程序有两个一级模块 system 和一个二级模块 workspace，则所有可访问的请求路径如下：

```
/system                     -> /__routers__/system.js
/system/users               -> /system/__routers__/user.js
/system/departments         -> /system/__routers__/departments.js
/system/groups              -> /system/__routers__/groups.js
/system/workspace/home      -> /system/workspace/__routers__/home.js
/system/workspace/tasks     -> /system/workspace/__routers__/tasks.js
/system/workspace/calendar  -> /system/workspace/__routers__/calendar.js
```

### 5.7、标记（Marker）

在开始介绍 service 和 manager 之前，需要先来了解一下 marker 的用法。marker 类似于 Java 中的 annotation，主要用来向方法中注入对象。基本用法类似这样：

```javascript
var {mark} = require('cdeio/marker');

mark('something').on(function () {
    ...
});
```

直接通过字面理解，就是将什么东西标记在某个方法之上。经过 mark 的方法会被注入一些参数，而返回结果是另一方法，该方法绑定了所有被注入的参数，而留下那些没有被注入的参数作为新方法的参数。mark 方法注入的参数会按照 marker 出现先后顺序以此排列。

mark 方法是可以串联使用的，也就是多个 mark 方法可以连接在一起，形成一个 marker 链，最后以 on 作为结束：

```javascript
mark('first').mark('second').mark('third').on(function () {
    ...
});
```

目前框架支持四种类型的 marker，分别介绍如下：

#### 5.7.1、tx

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

<span class="label label-info">提示</span> 有关 propagationBehavior 和 isolationLevel 的枚举值请参考 [TransactionDefinition](http://static.springsource.org/spring/docs/3.1.x/javadoc-api/org/springframework/transaction/TransactionDefinition.html) 类型。

如果使用了 needStatus 配置，可以在 on 后面的方法中接收这个 TransactionStatus 参数，使用方法如下：

```javascript
mark('tx', {needStatus: true}).on(function (status) {
    ...
});
```

<span class="label label-important">注意</span> 如果在 marker 链中使用 tx marker，则其必须出现在 marker 链的最尾部。

#### 5.7.2、beans

该 marker 向方法注入 Spring 容器中声明的 Java Bean 对象。

```javascript
mark('beans', 'bean1', 'bean2', JavaBeanClass).on(function (bean1, bean2, javaBeanInstance) {
    ...
});
```

此 marker 的第一个参数表明了该 marker 的类型，后面是一组变参，将需要注入的 Java Bean 的 ID 或者类型罗列在此，后面的方法就可以按次序接收到注入的 Java Bean 对象。

#### 5.7.3、services

该 marker 向方法注入 service 对象，关于 service 对象的描述请参考有关章节。

```javascript
mark('services', 'modulePath1:serviceName1', 'modulePath2:serviceName2').on(function (service1, service2) {
    ...
});
```

此 marker 的第一个参数表明了该 marker 的类型，后面也同样是一组变参。其中的 service 使用了一种特殊的表示方法，称为“坐标”。该坐标由两部分组成，以冒号（:）分割。冒号的左边是该 service 所在模块的绝对路径，冒号右边是该 service 的文件名。这样的写法会带来如下一些好处：

- 屏蔽了 service 的具体查找路径，也就是免去了在路径中写入 \_\_services\_\_ 这样的目录
- 强制要求 service 一定放置在 \_\_services\_\_ 目录，突出框架的规范性要求

<span class="label label-important">注意</span> 此 marker 要求注入的 service 必须导出一个名为 createService 的方法。

#### 5.7.4、managers

该 marker 向方法注入 manager 对象，关于 manager 对象的描述请参考有关章节。

```javascript
mark('managers', User, Group, 'modulePath:managerName').on(function (userManager, groupManager, otherManager) {
    ...
});
```

同上，此 marker 的第一个参数也表明了该 marker 的类型，后面也是一组变参，该参数里面可以包含两种类型的对象。首先可以指定实体类型，则框架会自动注入一个通用的 manager 用来访问该实体，另外如果是指定到 manager 的坐标（同 service 的坐标概念类似），则框架注入该坐标指向的 manager。

<span class="label label-important">注意</span> 此 marker 要求注入的 manager 必须导出一个名为 createManager 的方法。

### 5.8、服务（Service）

service 在本框架中充当业务逻辑层的角色，所有与业务有关的代码都应该在这里完成。但是 service 中的代码不应该参与访问底层存储或其它基础组件，这部分工作应该是由 manager 来完成的，service 应该只是调用 manager 里面的方法而已。为了使得 marker 可以自动注入 service，要求每个 service 文件必须导出一个名为 createService 的方法，marker 在每次注入的时候都会调用该方法，生成一个新的 service 实例，因此 service 的注入模式是 protoytype 的，而不是 singleton 的。

所有的 service 都要求存放在模块的 \_\_services\_\_ 目录下，并应该按照功能分类进行合并与拆分，每个 service 文件的命名，框架并没有强制要求。

```javascript
// system/__services__/users.js

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

<span class="label label-important">注意</span> 多个 marker 串联的时候，tx marker 一定要放到最后面

<span class="label label-important">注意</span> 使用 marker 注入的参数会按照 marker 出现的顺序依次注入

回想前面讲解 marker 的时候所说的，marker 的返回值是什么。marker 返回一个被装饰过的方法，该方法绑定了那些被注入的参数，而留下那些未被注入的参数，给新方法作为参数使用。还是不太明白，那么我们可以认为当调用 createService 方法之后，返回的 userService 对象具有类似如下的方法签名：

```javascript
userService = {
    createUser: function (user) { ... },
    editUser: function (id, newUser) { ... },
    deleteUser: function (id) { ... },

    showUser: function (id) { ... },
    listUsers: function () { ... }
}
```

类比前面 user service 定义中的代码，所有 userManager 参数，都已经被 managers marker 填充过了，调用 userService 里面的方法的时候，就只需传入那些没有被注入的参数就可以了。

service 一般是在 router 中进行使用的，配合上面的例子，对应的 user router 应该看起来是这个样子：

```javascript
// system/__routers__/users.js

var {createRouter} = require('cdeio/router');
var router = createRouter();

router.post('/', mark('services', 'system:users').on(function (userService) { ... }));
router.put('/:userId', mark('services', 'system:users').on(function (userService, userId) { ... }));
router.del('/:userId',mark('services', 'system:users').on(function (userService, userId) { ... }));

router.get('/:userId', mark('services', 'system:users').on(function (userService, userId) { ... }));
router.get('/list', mark('services', 'system:users').on(function (userService) { ... }));

exports.router = router;
```

请读者根据前面的思路，自行分析以上代码的含义。

### 5.9、管理器（Manager）

manager 在本框架中充当数据访问层的角色。大多数情况下，如果只需完成基本的添、删、改、查等操作，用户无需自定义 manager，直接使用框架提供的通用的 manager 就可以了。

通用 manager 还有一个功能就是可以动态加载并自动调用 orm.xml 文件中定义的 named query。JPA 规范中允许将 named query 以配置文件的形式保存到 orm.xml 文件中，这样做可以带来若干好处：比如可以很方便的集中编辑所有的 HQL/SQL 语句，而且预先定义的 HQL 语句会在系统启动的时候进行预编译，提高运行期的语句解析速度。但是随之产生一个问题，就是该文件改动以后，必须重新启动系统才可以被重新加载，这个过程显然有悖于本框架的基本理念。因此框架在实现通用 manager 的时候在这方面进行了加强。只要 orm.xml 文件的内容被修改过，调用 manager 方法的时候就会自动加载这些变更了的文件。这种形式的动态加载自然会对运行效率有一定的影响，因此系统可以通过参数配置运行的模式，只有运行在开发模式下，此项功能才会启用。

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

可以看到这里仍然使用了 managers mark 来注入一个通用的 manager，原因是我们希望扩展的 manager 仍然具有通用的 manager 的所有功能。在这里调用了通用 manager 内置的 mixin 方法，顾名思义就是将当前对象与参数中的对象的属性和方法进行混合，即返回的结果是一个新的对象，该对象拥有这两个对象的属性和方法的并集。而且 mixin 里面的方法会被自动注入 EntityManager 对象以便对底层数据库进行访问。这里的 EntityManager 就是由 Open Session in View 来控制的，所以在开发过程中无需关注其生命周期状态，直接就可以使用。

<span class="label label-important">注意</span> 在 manager 的方法中不要进行任何与事务有关的操作，这些操作应该是在 service 中进行的。
