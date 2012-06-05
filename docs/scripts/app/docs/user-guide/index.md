![中昱达](assets/images/logo.png "中昱达")


Zyeeda Framework 2.0 用户手册
=================================


##简介

Zyeeda Framework 2.0 是公司的最新框架，通过良好的封装和约束，开发人员可以轻松的完成功能开发，从而使开发人员脱离技术约束，专注于业务功能开发。



##先决条件#


###Maven

[Apache Maven](http://maven.apache.org/ "Maven")是一款基于项目对象模型（POM）的项目管理工具，本框架的编译需要依赖Apache Maven。

请下载并配置 Maven ，并确保配置正确，Maven 详细的用法及命令请参考官方文档。



###Mercurial

[Mercurial](http://mercurial.selenic.com/ "Mercurial") 是一款分布式版本管理软件,通过简单快捷的命令就可以完成所有版本管理的功能。

目前公司多数的项目都是采用此版本管理工具，所以此工具也是必备和必会的。




##源码下载

本框架版本服务器为 Mercurial ，下载地址为  http://192.168.1.14/hg/zyeeda-framework-2.0 ，请先将框架源码 clone 到本地。


##概述


在开始详细介绍框架前，我们需要对框架的部分架构和技术要素做一下简单的说明。因为 zyeeda-framework-2.0 的封装大量使用了 JavaScript 技术，并且基于此框架构建的项目需要编写大量的 JavaScript 代码来替代传统的 Java 代码，也就是说后台的逻辑代码大部分也将由 JavaScript 来完成。这种开发模式对于习惯了传统 Java 项目开发的技术人员来说可能会产生一定的困惑，但这应该不会成为一种阻碍,因为我们要学习的并不是一种新的技术。

####Stick 、 Ringo 和  Rhino

这三种技术都可以看做是本框架的基础技术，但是是否熟悉和理解这三项并不会影响到框架的使用。之所以提供这样的简介，只是为了给开发人员提供了解和学习新技术的一个窗口。

[Stick](https://github.com/hns/stick "Stick") 是一种后端JS，是基于 RingJS 封装的一个模块化的 JSGI 中间件。而本框架则是基于 Stick 封装而来，提供了整套的调用逻辑。

[Ringo](http://ringojs.org/ "Ringo") 是一个符合 CommonJS 标准的运行时，并且这个运行时是完全由 Java 实现的。同时，这个运行时是构建在  Mozalla Rhino 这个 JavaScript 引擎之上的。

[Rhino](http://www.mozilla.org/rhino/ "Rhino") 是一个后端 JavaScript 执行引擎。框架封装的代码就是通过它来解析，并且和 Java 代码进行交互的。

####exports 和 require

exports 和 require 来源于 CommonJS ，分别为提供调用和请求调用。

exports 的官方解释是允许标明的脚本向其它标明或未标明的脚本提供属性、函数和对象。通俗的讲，就是如果你定义了需要被外部调用的对象、属性或者方法，那么必须调用 exports 来声明这个方法，这样它才能被外部对象调用到。

exports 样例代码：
```javascript
//文件名称为 apps.js
exports.app = function(req) {
    return {
        status: 200,
        headers: {"Content-Type": "text/plain"},
        body: ["Hello World!"]
    };
};
```

exports 样例代码给我们展示了如何定义一个能够被外部调用到的对象，但是如何在外部文件中调用到这个对象，这个时候就必须用到 require 了。

require 用来请求声明为 exports 的对象，require 的参数为模块标示，返回被请求模块的属性、函数和对象。

require 样例代码：
```javascript
var app = require('apps');
```

require 样例代码给我们展示了如何 require 一个对象。你应该已经注意到了，require 请求的东西首先要被 exports 。

exports 和 require 对于本框架来说非常重要，必须明白且掌握它们的用法才能开始下面的内容。


框架详细介绍
------------


###zyeeda-drivebox-2.0 最小系统

zyeeda-drivebox-2.0 最小系统是公司在 zyeeda-framework-2.0 和其他开源框架基础上整合的不包括业务代码的可发布的基础平台类项目。

它本身也是一个项目，并且提供完善的框架整合，提供常用的工具类，但是不包括特定的业务代码。基于公司的框架开发的项目都将以 zyeeda-drivebox-2.0 最小系统为基础，在此系统上添加各自项目的业务代码。所以 zyeeda-drivebox-2.0 最小系统是一个基础的技术平台，通过这个平台可以扩展各种各样的项目。

在开始详细介绍 zyeeda-drivebox-2.0 最小系统以前，请和公司的配置管理员联系，从版本库中将源码 Clone 到本地。

###项目目录结构

本文档参照 zyeeda-drivebox-2.0 对项目目录结构做大概的介绍，为下面展开讲解项目的架构及应用做铺垫。

zyeeda-drivebox-2.0的目录结构如下图：

   ![](assets/images/user-guide/project.png)

zyeeda-drivebox-2.0 为 maven 项目，文件主目录为 `src/main` ，主目录下面有五个目录，分别为 java  ， resources ， rules ，javascript 和 webapp 。

各目录用途如下：

- java ： java 文件目录，在项目中主要用来存放 Entity 对象文件。

- javascript ： javascript 文件目录，除了 Entity 文件，基本上其他后台文件都下这个目录下。

- resources ： 配置文件目录，spring 、JPA 等配置文件都在此目录下。

- rules ：工作流规则文件目录，主要用来存放 bpmn 配置文件

- webapp ： 前端文件目录。

接下来，我们将以 zyeeda-drivebox-2.0 为例，介绍基于 zyeeda-framework-2.0 开发的项目的典型架构。

首先将介绍如何定义业务类实体。

###实体设计

框架提供了三个基础的实体类 ，如下表：

 ![](assets/images/user-guide/entity.png)


DomainEntity 、SimpleDomainEntity 、 RevisionDomainEntity 分别定义了一些常用属性，并且这三个基类之间是依次继承的关系。

作为基类 DomainEntity 实现了 Serializable 接口，这样所有继承了 DomainEntity 的实体类就都可序列化了。每个基础实体类实体都拥有一些属性，上表中标示出了每个属性所对应的数据库字段的名称。通过继承，自定义的业务实体可以不用自己实现 Serializable 接口，并且会从父类继承一些常用属性。

我们建议通过继承基础实体类来达到业务实体可序列话的目的，而且通过继承也免去了开发人员反复定义一些常用属性。但是要注意一点，如果自定义业务实体类继承了基础实体类，那么就不要再给业务实体类定义和基础实体类中名称一样的属性了。下面会给出三个实体的代码片段，以便更好的了解这三个基础实体类。

#####DomainEntity

DomainEntity为所有Entity的基类，它实现了Serializable接口，并且拥有id属性，建议所有自定义业务实体类必须继承 DomainEntity 。
代码如下：
```java
	@javax.persistence.MappedSuperclass
	public class DomainEntity implements Serializable {

		private static final long serialVersionUID = 6570499338336870036L;

		private String id;
		@javax.persistence.Id
		@javax.persistence.Column(name = "F_ID")
		@javax.persistence.GeneratedValue(generator="system-uuid")
		@org.hibernate.annotations.GenericGenerator(name="system-uuid", strategy = "uuid")
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}
```

#####SimpleDomainEntity

SimpleDomainEntity 继承自 DomainEntity,并且扩展了 name 和 description 属性。

代码如下:
```java
		@javax.persistence.MappedSuperclass
		public class SimpleDomainEntity extends DomainEntity {

		    private static final long serialVersionUID = -2200108673372668900L;
			
		    private String name;
		    private String description;
		    
		    @javax.persistence.Basic
		    @javax.persistence.Column(name = "F_NAME")
		    public String getName() {
				return this.name;
		    }

		    @javax.persistence.Basic
		    @javax.persistence.Column(name = "F_DESC", length = 2000)
		    public String getDescription() {
				return this.description;
		    }
			..............
		}
```

#####RevisionDomainEntity

RevisionDomainEntity 继承自 SimpleDomainEntity ,并且扩展了 creator ，createdTime ，lastModifier 和 lastModifiedTime 四个属性。

代码如下:
```java
		@javax.persistence.MappedSuperclass
		public class RevisionDomainEntity extends SimpleDomainEntity {

		    private static final long serialVersionUID = 2055338408696881639L;
			
		    private String creator;
		    private Date createdTime;
		    private String lastModifier;
		    private Date lastModifiedTime;
		    
		    @javax.persistence.Basic
		    @javax.persistence.Column(name = "F_CREATOR", length = 50)
		    public String getCreator() {
				return this.creator;
		    }	    
		    @javax.persistence.Temporal(TemporalType.TIMESTAMP)
		    @javax.persistence.Column(name = "F_CREATED_TIME")
		    public Date getCreatedTime() {
				return this.createdTime;
		    }
		    @javax.persistence.Basic
		    @javax.persistence.Column(name = "F_LAST_MODIFIER", length = 50)
		    public String getLastModifier() {
				return this.lastModifier;
		    }
		    @javax.persistence.Temporal(TemporalType.TIMESTAMP)
		    @javax.persistence.Column(name = "F_LAST_MODIFIED_TIME")
		    public Date getLastModifiedTime() {
				return this.lastModifiedTime;
		    }
			..............
		}
```


#####如何自定义业务实体类



假设我们有名为 people 的实体类，这个实体类有 id ，name ，sex ，age 这些属性，那么，我们的实体类代码将如下：
```java
		@Entity
		@Table(name="ZDA_People")
		public class People extends DomainEntity {

			private static final long serialVersionUID = 2338396716859666598L;
		    
			@Basic
			@Column(name = "name", length = 32, nullable = false)
			private String name;
		    
			@Basic
			@Column(name = "age", nullable = false)
			private int age;
		    
			@Basic
			@Column(name = "sex", length = 32, nullable = false)
			private String sex;
			...........
		}
```

通过继承 DomainEntity，无需再重复定义 id 属性，并且已经实现了 Serializable 接口。

框架中业务实体类的配置和映射都是通过 Annotation 的方式实现的，所以需要开发人员对 Hinbernate JPA 的注解有所了解。例如上例中以 `@` 开头的注解,例如 `@Entity` 、 `@Table` 、 `@Column` 等都来源于 [Hibernate Annotations](http://www.techferry.com/articles/hibernate-jpa-annotations.html "Hibernate Annotations") ，如果需要了解或者学习也可参考 [hibernate-jpa-annotations](http://docs.jboss.org/hibernate/annotations/3.5/reference/en/html_single/#entity-mapping "hibernate-jpa-annotations") 。

###框架组成结构###

本框架后台分三层结构，分别是 router，service 和 manager 。每一层的职责都不同，并且是依次调用的关系,即 router 调用 service , service 调用 manager。

- router ： 定义请求规则和处理请求，作为所有请求的入口。router 调用 service 来处理业务。

- service ： 作为业务处理类，所有的业务处理将在 service 层完成，包括事务处理。service 处理完业务后调用 manager 层来做数据持久化操作。

- manager ： 数据持久层，用来完成数据库交互工作。

每一个业务模块都应该由这样的三层结构组成，并且不同的业务实体类需要定义不同的 router 、 service 和 manager 。如下图，表示的是项目的层级挂载和调用关系。

![](assets/images/user-guide/call-order.png)

上图中` { ` 表示挂载 ， ` -> ` 表示调用。

由上图可以看出`根 router ` 可以挂载子模块的 ` router ` , 子模块又可以挂载子模块的业务类或者下级子模块的 ` router ` 。 通过这种层层挂载的方式，router 可以形成一个树状结构。经由根 router 可以通过树状结构的路径调用到被请求的叶子 router ，然后叶子 router 再调用 service 来处理业务 ，service 调用 manager 处理数据库请求。

router 的挂载没有层级限制，而且 router 的挂载路径也正是请求的访问路径。

关于如何创建 、挂载 、请求 router 的问题将方法下面 router 的章节去讲。

在介绍 router 之前，先要介绍一下 main.js 。因为从项目结构上来说，main.js 是整个后台代码的入口。

### main.js####

`src/main/javascript` 文件夹下有一个固定文件 main.js ，它是整个后台程序的入口文件。

整个项目的 `根router` 会被定义在 main.js 文件中 ，然后再通过 `根router` 来挂载子模块的router。

以 zyeeda-drivebox-2.0 为例，项目目录结构图如下：

![](assets/images/user-guide/main.png)

 `src/main/javascript` 下定义了两个模块 ，分别为 demo 和 sample 。mian.js 作为后台入口定义了 `根router ` ，然后分别挂载了 demo 和 sample 文件夹下的 router.js .
 
### router ####

router 是框架的请求处理层，也是整个后端架构的最上层，它主要负责处理和响应前端发送过来的请求 。除此之外它还有一个很重要的作用，挂载子模块的 router 。

通过挂载子 router ，可以将整个项目的资源串成一个树状结构，并且将 `根router` 作为统一的入口；通过定义请求规则和响应请求，可以约定所有资源的访问规则,并且处理这些请求。

框架的封装类 router 提供了两个创建 router 的方法 `createModuleRouter` 和 `createRouter` ，这里两个方法都可以创建 router 对象 ，但是创建的 router 对象的用途不同。`createModuleRouter` 方法创建的 router 对象用来自动挂载子模块的 router 或者挂载指定的 router；`createRouter` 方法创建的 router 对象用来定义具体的请求规则和处理请求 。

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









