![中昱达](assets/images/logo.png "中昱达")


**Zyeeda Framework 2.0 用户手册**
=================================

**简介**
--------

Zyeeda Framework 2.0 是公司的最新框架，通过良好的封装和约束，开发人员可以轻松的完成功能开发，从而使开发人员脱离技术约束，专注于业务功能开发。

**先决条件**
------------

###Maven

Apache Maven是一款基于项目对象模型（POM）的项目管理工具，本框架的编译需要依赖Apache Maven，如果需要了解相关知识请访问[Maven] [3]官网。

###HG
Hg为一款分布式版本管理软件,如果需要下载客户端或者学习Hg的用法，请访问[mercurial] [2]的官网 。HG为公司项目版本管理的标准工具，开始项目前请下载并安装HG。

**Downloads**
----------
本框架版本服务器为Hg，下载地址为  http://192.168.1.14/hg/zyeeda-framework-2.0


**Overview**
--------------

框架大量使用了基于JavaScript第三方开源框架的再封装，使得前后台的语法比较接近，减少开发人员的学习成本。

后台采用 stick ([https://github.com/hns/stick](https://github.com/hns/stick "stick")) 封装而成，stick 基于 ringojs([http://ringojs.org/](http://ringojs.org/ "ringojs")) 实现，rhino([http://www.mozilla.org/rhino/](http://www.mozilla.org/rhino/ "ringo")) 作为 stick 的引擎。在此我们将不对这三者做详细阐述，如果需要了解请参考一下信息：

- rhino  - Rhino是 JavaScript 的一个开源实现，完全用 Java 编写的。它通常被嵌入到Java 应用程序提供脚本给最终用户。
- ringojs - Ringo 是一个符合 CommonJS 标准的用 Java 实现的运行时，并且是构建在 Mozalla Rhino的JavaScript 引擎之上的。
- stick- stick 是基于 RingoJS 封装的一个模块化的 JSGI 中间件组成层和应用框架。


####exports 和 require
exports 允许标明的脚本向其它标明或未标明的脚本提供属性、函数和对象。 
通常情况下标明脚本中的信息仅对对象同主体标明的脚本可用。通过导出属性、函数或对象，标明脚本将使得信息对于任何脚本(标明或未标明的)都可用。

样例代码：
```javascript
exports.app = function(req) {
    return {
        status: 200,
        headers: {"Content-Type": "text/plain"},
        body: ["Hello World!"]
    };
};
```

require 用来加载 exports 的属性、函数和对象。require 的参数为模块标示，返回被请求模块的属性、函数和对象。

样例代码：
>		var {createModuleRouter} = require('coala/router');

如果需要详细了解exports和require的详细用法请参考[CommonJS] [10]


**Introduction**
------------
###**最小项目**
为了方便项目的开发，我们提供了作为所有应用 zyeeda-framework-2.0 构建项目的最小项目--   zyeeda-drivebox-2.0 ，所有的项目都将基于此项目扩展而来。 zyeeda-drivebox-2.0 位于14上HG版本管理服务器上，版本库地址为 http://192.168.1.14/hg/zyeeda-drivebox-2.0 ，请自行clone该项目。

#####**项目目录结构**
本文档将参照 zyeeda-drivebox-2.0 对项目目录结构做大概的介绍，为下面展开讲解项目的架构及应用做铺垫。

zyeeda-drivebox-2.0的目录结构入下图：

   ![](assets/images/user-guide/project.png)

zyeeda-drivebox-2.0 为 maven 项目，文件主目录为 `src/main` ，主目录下面为五个目录，分别为 java  ， resources ， rules ，javascript 和 webapp 。

以下将分别介绍各目录的用途：

- java ： 此目录为 java 文件目录，主用用来存放 Entity

- javascript ： 此目录存放 javascript ，所有的项目业务逻辑代码都在此目录下

- resources ： 此目录为配置文件目录，存放各配置文集，例如 spring 、hibernate 等

- rules ： 此目录为规则文件目录，主要用来存放 bpmn 配置文件

- webapp ： 此目录为项目的前端文件目录

接下来，我们将以 zyeeda-drivebox-2.0 为例介绍应用 zyeeda-framework-2.0 开发项目的典型架构。

首先将介绍如何定义业务类实体：

###**实体设计**
本框架提供了三个基础实体类，分别为 DomainEntity ，SimpleDomainEntity ，     RevisionDomainEntity 。这三个类都实现了 Serializable 接口，并且定义了一些常用属性，自定义业务实体类可以通过继承以上三个基础类来得到一些常用属性和实现可序列化的目的。我们建议所有的自定义业务实体类都通过继承以上的基础类了实现可序列化，不继承任何基础类也是允许的，但是必须实现 Serializable 接口。

我们将在下面的内容中介绍三个基础类，以及如何定义业务实体类。


#####***DomainEntity***
DomainEntity为所有Entity的基类，它实现了Serializable接口，并且拥有id属性，所有自定义实体类必须继承DomainEntity，无需再定义id属性。<br/>
代码如下：
>		@javax.persistence.MappedSuperclass
>		public class DomainEntity implements Serializable {
>
>	    private static final long serialVersionUID = 6570499338336870036L;
>
>	    private String id;
>	    @javax.persistence.Id
>	    @javax.persistence.Column(name = "F_ID")
>	    @javax.persistence.GeneratedValue(generator="system-uuid")
>	    @org.hibernate.annotations.GenericGenerator(name="system-uuid", strategy = "uuid")
>		public String getId() {
>			return id;
>		}
>	    public void setId(String id) {
>			this.id = id;
>		}
>		}


#####***SimpleDomainEntity***

SimpleDomainEntity 继承自 DomainEntity,并且扩展了另外两个属性-- name 和 description

代码如下:
>		@javax.persistence.MappedSuperclass
>		public class SimpleDomainEntity extends DomainEntity {
>
>		    private static final long serialVersionUID = -2200108673372668900L;
>			
>		    private String name;
>		    private String description;
>		    
>		    @javax.persistence.Basic
>		    @javax.persistence.Column(name = "F_NAME")
>		    public String getName() {
>				return this.name;
>		    }

>		    @javax.persistence.Basic
>		    @javax.persistence.Column(name = "F_DESC", length = 2000)
>		    public String getDescription() {
>				return this.description;
>		    }
>			..............
>		}

#####***RevisionDomainEntity***

RevisionDomainEntity 继承自 SimpleDomainEntity ,并且扩展了四个属性-- creator ，createdTime ，lastModifier 和 lastModifiedTime

代码如下:
>		@javax.persistence.MappedSuperclass
>		public class RevisionDomainEntity extends SimpleDomainEntity {
>
>		    private static final long serialVersionUID = 2055338408696881639L;
>			
>		    private String creator;
>		    private Date createdTime;
>		    private String lastModifier;
>		    private Date lastModifiedTime;
>		    
>		    @javax.persistence.Basic
>		    @javax.persistence.Column(name = "F_CREATOR", length = 50)
>		    public String getCreator() {
>				return this.creator;
>		    }	    
>		    @javax.persistence.Temporal(TemporalType.TIMESTAMP)
>		    @javax.persistence.Column(name = "F_CREATED_TIME")
>		    public Date getCreatedTime() {
>				return this.createdTime;
>		    }
>		    @javax.persistence.Basic
>		    @javax.persistence.Column(name = "F_LAST_MODIFIER", length = 50)
>		    public String getLastModifier() {
>				return this.lastModifier;
>		    }
>		    @javax.persistence.Temporal(TemporalType.TIMESTAMP)
>		    @javax.persistence.Column(name = "F_LAST_MODIFIED_TIME")
>		    public Date getLastModifiedTime() {
>				return this.lastModifiedTime;
>		    }
>			..............
>		}

#####***如何自定义业务实体类***

假设我们有名为 people 的实体类，这个实体类有 id ，name ，sex ，age 这些属性，那么，我们的实体类代码将如下：
>		@Entity
>		@Table(name="ZDA_People")
>		public class People extends DomainEntity {

>	    	private static final long serialVersionUID = 2338396716859666598L;
>	    
>	    	@Basic
>	    	@Column(name = "name", length = 32, nullable = false)
>	    	private String name;
>	    
>	    	@Basic
>	    	@Column(name = "age", nullable = false)
>	    	private int age;
>	    
>	    	@Basic
>	    	@Column(name = "sex", length = 32, nullable = false)
>	    	private String sex;
>			...........
>		}

通过继承 DomainEntity，我们无需再重复定义 id 属性。

备注：上例中以 `@` 开头的注解,例如 `@Entity` 、 `@Table` 、 `@Column` 等都来源于     [Hibernate Annotations] [4] ，如果需要了解或者学习也可参考 [hibernate-jpa-annotations] [5] 。

###<b>框架组成结构</b>###
本框架后台分三层结构，分别为 router，service 和 manager

- router 用来定义请求规则和处理请求，作为所有请求的入口。router 将调用 service 来处理业务。

- service 作为业务处理类，所有的业务处理将在 service 层完成，包括事务处理。service 处理完业务后调用 manager 层来做数据持久化操作。

- manager 为数据持久层，用来完成数据库交互工作。

####**main.js**####
zyeeda-drivebox-2.0 项目的 src/main/javascript 目录结构如下

![](assets/images/user-guide/main.png)

 `main.js` 为后台代码的入口，后台代码的调用关系为 main.js --> router --> service --> manager。可以认为 main.js 是一个固定名称和位置的文件，程序会自动调用 main.js 来完成后台的加载和调用。

在下面介绍 router 的时候，会介绍 router 的用法，以及如何在 main.js 中调用 router。
接下来，开始介绍 router：

#### **router** ####

router 主要用来定义请求规则和处理请求。顾名思义，它本身并不处理任何业务，但是根据 router 中定义的请求规则，你可以调用 service 来处理你的业务。

router 的创建方式有两种 `createModuleRouter` 和 `createRouter` ,这两个方法都是由框架封装的 router 类中 exports 出来可被调用的方法。以下将提供两段样例代码来说明这两个方法的用法以区别。

样例1 -- createModuleRouter
>		var {createModuleRouter} = require('coala/router');
>		var router = exports.router = createModuleRouter();
>		router.autoMount(this);

样例2 -- createRouter
>		var {createRouter} = require('coala/router');
>		var {html} = require('coala/response');
>		var router = exports.router = createRouter();
>		router.get('/', function(request){
>		    return html('sub first');
>		});

在讲解`样例1`和`样例2`之前，我们先回顾一下 `require` 的用法：

> 	var {createModuleRouter} = require('coala/router');

这句话的意思是请求 `coala` 目录下 `router` 文件中的 `createModuleRouter` 方法，所以这样我们就很容易明白 require 的用法， `require` 的参数是一个文件路径。但是我们要确保我们请求的对象是已经 exports 过的。

> var router = exports.router = createModuleRouter();

这句代码的意思是，我们定义的 `router` 对象将被 exports 出去，其他对象可以通过 require 方法来请求到这个对象。

好了，言归正传，我们来看下`样例1`和`样例2`的区别。

请注意样例1中的这两行代码

> 		router.autoMount(this);

`autoMount()` 是 router expters 的方法，用来自动挂载子模块的 router ,但是只能挂载跟定义`router.autoMount(this)` 这个文件在同一个文件夹下的子文件夹的下一级的名为 `router.js` 文件中定义的对象。

autoMount 的语法 `aotuMount( router )`

这里要强调一下`同一个文件夹下的子文件夹的下一级`，因为这个是 `autoMount` 的挂载限制。

假如有如下的目录结构，javascript 文件夹中有 scaffold 文件夹和 main.js 文件，scaffold 文件夹下又有router.js和downScaffold子文件夹，downScaffold文件夹中还有一个router.js。那么如果我们在main.js中调用`router.autoMount()`,那么scaffold文件夹下的router.js中定义的router对象将被挂载，而downScaffold文件夹下的router.js中定义的router对象将不会被挂载。

样例1中`router.mount('/scaffold', 'coala/scaffold/router')`为被挂载的router的项目相对访问路径，第二个参数为该访问路径对应的被挂载的router文件路径。

那么综合样例1的代码，我们首先创建了一个router，并且自动挂载当前文件下子文件夹中的router.js,并且指定`coala/scaffold/router`这个router.js中定义的router的访问路径是`/scaffold`。

接下来，我们来分析样例2的代码。

样例2的代码创建router后直接调用`router.get()`，get方法的第一个参数指定的是访问路径，第二个参数为处理函数。

所以综样例1和样例2的代码，可以看出`createModuleRouter`和`createRouter`的区别：前者用来创建router，挂载子模块的router对象，并给被挂载的router对象制定访问路径；后者用来创建router，并给router的请求定义访问路径和处理方式。

根据上一节对main.js的描述，关于main.js中应该如何调用router其实已经很清晰了。样例1的代码正是main.js中的代码片段，通过`autoMount`，可以在main.js中自动挂载子模块的router对象，并实现后台请求的唯一入口。

需要注意的是，假设样例2中代码为样例1中被挂载的router中的代码，那么样例2中get方法的请求路径为
`项目根路径/scaffold/`。实际上项目中的router为层层挂载的，访问子模块中router定义的请求的时候，应该加上上一层router指定给下层router的路径。

接下来将延续样例2的代码，介绍如何定义具体的请求。

#####router的请求方式

router提供了 “get” ， “post” ，“put” 和 “del” 四种请求处理方法，分别对应不同类型的数据操作。

- get ：检索数据时请求，单条数据或多条数据检索的时候请求

- post：插入数据或者批量删除数据时请求

- put ：更新数据时请求

- del ：删除单个数据时请求

通常，检索、更新和删除单条数据时需要通过URL来传递参数。假设有如下代码：

（备注：path 为当前请求的 root 路径；domain\_id 为数据id；by 为查询条件关键字；field-desc 为按某个字段排序，field 为字段名称，desc 为降序关键字；page 为分页关键字，其后要查询的页数；当前实际URL为 http://localhost：8080/drivebox/demo）

请求处理1
>		//处理 新增 操作
>		//请求路径为 ： path
>		//URL : `http://localhost：8080/drivebox/demo/`
>		//执行操作 ： 新增Form中demo对象
> 		router.post('/',function(){
> 			//要执行的操作
> 		});

请求处理2
>		//处理 根据id删除 操作
>		//请求路径为 ： path/domain_id
>		//URL : `http://localhost：8080/drivebox/demo/123`
>		//执行操作 ： 删除id为123的demo数据
> 		router.del('/：id',function(request，id){
> 			//回调函数中的id将接收URL中的id参数，此例为123
> 			//要执行的操作
> 		});

请求处理3
>		//处理 批量删除 操作
>		//请求路径为 ： path/delete
>		//URL : `http://localhost：8080/drivebox/demo/delete`
>		//执行操作 ： 删除demo数据
> 		router.post('/delete',function(request){
> 			//要执行的操作
> 		});

请求处理4
>		//处理 修改 操作
>		//请求路径为 ： path/domain_id
>		//URL : `http://localhost：8080/drivebox/demo/123`
>		//执行操作 ： 修改id为123的demo数据
> 		router.put('/',function(request，id){
> 			//回调函数中的id将接收URL中的id参数，此例为123
> 			//要执行的操作
> 		});

请求处理5
>		//处理 根据id查询 操作
>		//请求路径为 ： path/domain_id
>		//URL : `http://localhost：8080/drivebox/demo/123`
>		//执行操作 ： 检索id为123的demo数据
> 		router.get('/：id',function(request，id){
>			//回调函数中的id将接收URL中的id参数，此例为123
> 			//要执行的操作
> 		});

请求处理6
>		//处理 查询列表 操作
>		//请求路径为 ： path
>		//URL : `http://localhost：8080/drivebox/demo/`
>		//执行操作 ： 查询所有demo数据
> 		router.get('/',function(request){
> 			//要执行的操作
> 		});

通过以上请求处理代码片段，我们可以看出，操作和请求处理方法是一一对应的，这是一种严格的约束。即使相同的请求路径，调用不同的处理方法也会成为不同的操作。而且请求对参数有严格的要求，如果定义的请求处理路径中带参数，那么相应的请求也不许带参数，否则无法匹配到这个请求定义。例如我们定义如下router：

> 		router.get('/:id',function(){});

通过`http://path/123`可以请求到上面定义的请求处理，而且`http://path/`则无法请求到。并且调用请求的时候需要显示的告诉浏览器，你将采取何种请求方式： `get` 、 `put` 、`post` 、 `del` 。

关于如何显示的告诉浏览器采取何种调用方式，我们将会在前端文档中提及，在此就不做赘述了。
 

（备注：path 为当前请求的 root 路径；domain\_id 为数据id；by 为查询条件关键字；field-desc 为按某个字段排序，field 为字段名称，desc 为降序关键字；page 为分页关键字，其后要查询的页数；）

- 检索单条数据的时候URL为：path/domain\_id，例如：`http://localhost：8080/drivebox/
demo/123`，调用get请求的时候为检索id为123的demo数据。

- 检索多条数据的时候URL为：path/by/field-desc/page/1，例如：`http://localhost：8080/drivebox/demo/by/name-desc/page/2`，调用get请求的时候为按照 name 降序查找第2页的数据，默认分页条数为10。

- 删除单条数据的时候URL为：path/domain\_id，例如：`http://localhost：8080/drivebox/demo/123`，调用del请求的时候为删除id为123的demo数据。

- 更新数据的时候URL为：path/domain\_id，例如：`http://localhost：8080/drivebox/demo/123`，调用put求情的时候为更新id为123的demo数据。

在此章，并没有关于如何在router中调用service介绍。因为这种调用时用注入的方式实现的，而注入会涉及到`marker`的用法。我们将在下面的章节单独介绍如何使用mark来完成servce，mananger等的注入，所以在此就不做阐述。

#####参数传递#####

###<b>service</b>###
service层主要用来处理业务，并调用manager层完成数据操作。通常，我们的业务代码都写在这一层，并且事物处理也必须在这一层完成。接下来，我们将介绍service的用法，以及service相关的方法。

#####创建service
首先，我们来看下如何创建一个service对象。

框架的service封装类提供了一个名为`createService`的方法，将为会返回一个service对象。

> 		var {createService} = require('coala/service');
> 		var service = exports.service = createService();

service中定义了两个方法：

**-getEntityManager**

语法： getEntityManager( EntityManagerFactory ) 

参数： EntityManagerFactory 为配置文件中 EntityManagerFactory的名称

说明： 方法的返回值为 EntityManager 对象

> 		var entityManager = service.getEntityManager('entityManagerFactory');

**-createManager**

语法： createManager( entityClass , entityManagerFactoryName )

参数： entityClass为业务实体的类路径；EntityManagerFactory 为配置文件中 EntityManagerFactory的名称

说明： 方法的返回值为 Manager 对象

> 		var entityManager = service.getEntityManager('com.zyeeda.drivebox.entity.User','entityManagerFactory');

###<b>manager</b>###
manager负责跟数据库进行交互。

manager基于 Hibernate JPA 2.0实现，提供了一系列对数据库的操作方法。

**-find**

语法 : find(\[ids\])

参数 : ids -id数组

说明 : 根据id查找并返回与Mnanager绑定的Entity对应的结果集

**-getReference**

语法 : getReference( \[ids\] )

参数 : ids -id数组

说明 : 根据id查找并返回与Mnanager绑定的Entity对应的结果集

**-merge**

语法 : merge( \[entities\] )

参数 : entities -Entity数组

说明 : 将 Entity 更新到数据库，并返回更新成功的记录

**-save**

语法 : save( \[entities\] )

参数 : Description:entities -Entity数组

说明 : 将 Entity 保存到数据库，并返回成功保存的记录

**-remove**

语法 : remove( \[entities\] )

参数 : entities -Entity 数组

说明 : 从数据库中删除 Entity 对应的数据，并返回成功删除的记录

**-removeById**

语法 : removeById( [ids] )

参数 : ids -id数组

说明 : 根据id删除数据库中对应的记录，并返回成功被删除的记录集合 

**-contains**

语法 : contains( entity )

参数 : entity -entity对象

说明 : 判断数据库中是否已经存在此 Entity 所对应的记录，如果存在则返回 true ，否则返回 false

**-flush**

语法 : flush()

参数 : NULL

说明 : 将处于游离状态的对象持久化到数据库中

**-refresh**

语法 : refresh( [entites] )

参数 : entites -Entity 数组

说明 : 从数据库刷新实例的状态，如果有则覆盖实体的变化

**-getAll**

语法 : getAll( option )

参数 : option -Object 类型对象，此对象有以下属性 orderBy ，firstResult 和 maxResults

说明 : 查询并返回所有结果集

**-findByExample**

语法 : findByExample( example , option )

参数 : example -org.hibernate.criterion.Example 对象；option -Object 类型对象，此对象有以下属性orderBy，firstResult，maxResults和fetchCount

说明 : 通过Manager所绑定的Entity的来构造一个查询条件，并返回满足查询条件的结构集，详细请参考[Criteria ](http://docs.jboss.org/hibernate/orm/4.1/devguide/en-US/html/ch12.html  "Criteria")

**自定义查询方法**

除了以上方法之外，我们还可以通过 JPA 的 orm.xml 配置文件来提供自定义的数据库操作方法。

![](assets/images/user-guide/resources.png)

定义自定义查询方法的步骤如下：

1.在 resources/META-INF/orms 目录下建立自定义 orm 配置文件，例如 test-orm.xml

2.在自定义 orm 配置文件中添加自己的查询方法，例如在 test-orm.xml 中添加

> 		<named-query name="queryByName">
>         	<query>
>                	from Demo where name = :name
>         	</query>
>    	</named-query>

3.在 resources/META-INF 目录下 persistence.xml 文件中注册自定 orm 配置文件，例如添加如代码

>  		<mapping-file>/META-INF/orms/test-orm.xml</mapping-file>

4.调用方式跟 manager 自定义的方法一样 ，例如调用 manageer.queryByName( name ) 就会自动定位到上述自定义的 queryByName 方法

如果需要了解 orm.xml 和 persistence.xml 文件的详细配置方法以及用途，请访问[http://](http:// "1111")
###**marker**###
本框架引入了marker来解决注入的问题，目前提供四种类型注入 inject ，managers ， services 和 tx 。在 router 中注入 service 或者在 service 中注入 manager 都是通过 marker 来完成的，类似于 spring 的 annotation 。

marker 类提供了两个方法：

**-mark**

语法 ： mark( name , attributes )

参数 ： name 为注入的参数类型名称，目前提供四种类型，分别为 ‘inject’  , ‘managers’  , ‘services’ , ‘ tx ’ ; attributes 为注入的对象或者对象类型的数组

- inject ： 参数为 Spring 容器中 Bean 的名称或者类型

- managers ： 参数为 mananger 对象的名称或者类型

- services ： 参数为 service 对象的名称或者类型

- tx ： 不需要参数

说明 ： 用来实现注入功能

**-on**

语法 ： on( fn, me )

参数 ： fn为回调函数，此函数的参数为 mark 方法的 attributes ； me 为被绑定对象

说明 ： 接收 mark 方法注入的对象

mark 和 on 是成对出现的，通过 mark 方法注入的对象必须通过 on 方法接收。mark 注入的对象在 on 的回调方法中的顺序是依次从左到右的，基本用法为：


> 		mark('a').mark('b').mark('c').on(function(a, b, c){})

通过 mark 方法，很容易实现在 router 中注入 service ，或者在 service 中注入 manager。

例如：

**mark实例1**： 在 router 中注入 service 

> 		router.get('/', mark('services',service).on(function(service,request){
> 			//调用 service 处理业务
> 		}));

	router 中还有另外一种调用 service 的方法：

> 		var {createService} = require('demo/service/service');
> 		var service = exports.service = createService();
> 		router.get('/', function(request){
> 			//调用 service 处理业务
> 		});


**mark实例2** ： 在 service 中注入 mananger
> 		var {DemoEntity} = com.zyeeda.drivebox.entity;
> 		var {mark} = require('coala/mark');
> 
>	 	exports.createService = function() {
> 			return {
> 				create: mark('managers',DemoEntity).mark('tx').on(function(manager,entity){
> 					return manager.save(entity);
> 				})
> 			};
> 
> 		};

mark实例1提供了两种在 router 中调用 service 的方法 ，第一种是通过调用 mark 方法注入，第二种是通过 require 调用 service 的 createService 方法。虽然两种方式达到的效果一样，但是我们建议使用 mark 的方式注入，因为这样更方便，也更灵活。

mark实例2是在 service 中定义的 create 方法样例，create 方法调用 mark 注入了 managers 和 tx 两种类型的对象。

managers 类型的对象用来注入一个 manager 对象，但是实际上我们提供了一个名为  DemoEntity 的实体类。这个实体类被注入后， on 中接收了一个名为 manager 的对象，这个对象的类型为封装类 manager 。所以，实际上注入 mangers 类型的对象的时候，我们需要传递给 mark 方法 entity 的名称或者类型。mark 会根据注入的 entity 返回给 on 方法一个对象的 manager 对象。

tx 为Spring的事物管理器，无需参数，只要对方法 mark('tx') , 该方法会自动被添加事物处理。

mark实例2 create 中 on 方法的第二个参数 entity 是调用 create 方法需要传递的参数。

**mark 注入的约束**

- router 层不能注入 managers 和 tx

- service 层可以注入 service ，manager ， tx 和 inject 


####**Scaffold**###
通过在实体中配置 @scaffold 注解的方式，可以实现实体类的 CRUD 操作，并且不需要写任何后台代码。同时可以通过 Scaffold 的配置文件实现扩展功能，例如对自动生成的 CRUD 方法的重写和过滤，对查询结果的过滤和类型转换等功能。通过对 Scaffold 的应用，可以大量减少后台的代码，进一步提升开发效率。

**配置Scaffold**

假设存在一个数据字典类 DriveType ，下面以实现此数据字典类的 CRUD 功能为例介绍 Scaffold 的实现步骤。

第一步 ： 创建实体类并在实体上添加 @Scaffold 注解

![](D:\zyeeda-framework\driveTypeEntity.png)

如图，在 entity 目录中建立如图所示的 DriveType.java 文件 ，DriveType 类的头部添加如下注解

>		@Entity(name = "DriveType")
> 		@Table(name = "ZDA_DRIVE_TYPE")
> 		@Scaffold(path = "/system/driveType")
> 		public class DriveType extends SimpleDomainEntity {

注解 @Scaffold 提供了一个参数 path 属性， 该属性的值既是 CRUD 操作对应的 URL ，又是 Scaffold 配置文件的路径。

第二步 ： 在 config.js 中配置实体包路径

![](D:\zyeeda-framework\driveConfig.png)
 
在 config.js 中添加如下代码：

> 		exports.env = {
>     		entityPackages : [ 'com.zyeeda.drivebox.entity' ]
> 		};

config.js 为固定名称和位置的配置文件， entityPackages 指定的是实体类的包路径。

第三步 ： 在 scaffold 目录中建立配置文件

![](D:\zyeeda-framework\ScaffoldPath.png)

如图在 src/main/javascript/scaffold/system 下创建 driveType.js 文件，此文件的路径和 `@Scaffold(path = "/system/driveType")` 一致。加载 Scaffold 注解的时候会根据 path 属性的的值去加载 src/main/javascript/scaffold 目录下对应的文件。

driveType.js 文件中代码如下：


> 		// 用来重写默认的请求处理方法
> 		exports.handlers = {
> 			create：function(){
> 				//重写create
> 			}
> 		};
> 
> 		// 去除不需要的请求处理方法
> 		//过滤掉 list 方法和 get 方法
> 		exports.exclude = ['list', 'get'];
> 
> 		// 过滤后台传递给前台的 json 数据
> 		exports.filters = {
>		};
> 
> 		// 处理额外的 URL
> 		exports.doWithRouter = function(router) {
> 			router.get('/json',function(){
> 				//定义默认处理方法以外的处理方法
> 			});
> 		};
> 
> 		// 数据类型转换器
> 		exports.converters = {
> 			//createTime 为 DriveType 类的一个字段
> 			createTime ： function(){
> 				//类型转换方法
> 			}
> 		};

driveType.js 用来实现以下功能：

- handlers ：用来重写 Scaffold 默认的请求处理方法。下图为 Scaffold 提供的默认请求方式

	![](D:\zyeeda-framework\autoMehtod.png)

- exclude : 过滤掉不需要的请求处理方法。只能过滤掉默认方法 list ， get ， create ， updte ， remove 中的一个或者多个。

	例如配置 `exports.exclude = ['list', 'get'];` ，无法通过 get 方式请求到 list 方法和 get 方法。

- filters ： 过滤后台传递给前台的 json 数据

- doWithRouter ： 处理额外的 URL。用于给 router 定义除默认方法外额外的处理方法。例如：

> 		exports.doWithRouter = function(router) {
> 			router.get('/json',function(){
> 				//定义默认处理方法以外的处理方法
> 			});
> 		};

	通过上例的配置，DriveType 类就拥有了一个名为 `/json` 的方法。

- converters ：数据类型转换器。声明实体类的一个字段，然后给字段制定一个数据转换器。



通过	Scaffold 注解的方式，很容易实现原本需要通过 router ，service  和 manager 建立的层层调用的后台处理结构。这种方式更简洁，需要编写的代码更少。同时，并不会影响业务处理。所以，基于 Scaffold 的注解是一种值得尝试的方式。









