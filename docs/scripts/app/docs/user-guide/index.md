

![alt text](http://www.zyeeda.com/sites/default/files/zeropoint_logo.png "中昱达") 
<b>zyeeda-framework-2.0</b>
========================
<br/>

zyeeda-framework-2.0是公司的最新框架，该开发框架的应用可以提升开发效率。该框架通过良好的封装和约束，开发人员可以轻松的完成功能开发，从而使开发人员脱离技术约束，专注于业务功能开发。<br/><br/>
本文档将详细介绍开发平台的框架结构和API
<b>Downloads</b>
----------
本框架版本服务器为Hg，下载地址为  [zyeeda-framework-2.0] [1] <br/>
Hg为一款分布式版本管理软件,如果需要下载客户端或者学习Hg的用法，请访问[mercurial] [2]的官网 

<b>Maven</b>
--------
本框架的编译需要依赖Apache Maven，如果需要了解相关知识请访问[Maven] [3]官网

<b>Introduction</b>
------------

###<b>实体设计</b>
本框架提供了三个基础实体类，分别为DomainEntity，SimpleDomainEntity，RevisionDomainEntity。这三个类都实现了Serializable接口，并且定义了一些常用属性，自定义业务实体类可以通过继承以上三个基础类来得到一些常用属性和实现可序列化的目的。我们建议所有的自定义业务实体类都通过继承以上的基础类了实现可序列化，不继承任何基础类也是允许的，但是必须实现Serializable接口。<br/>
我们将在下面的内容中介绍三个基础类，以及如何定义业务实体类。<br/>

*******************************
####*<b>DomainEntity</b>*<br/>
DomainEntity为所有Entity的基类，它实现了Serializable接口，并且拥有id属性，所有自定义实体类必须继承DomainEntity，无需再定义id属性。<br/>
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

*******************************

####*<b>SimpleDomainEntity</b>*<br/>

SimpleDomainEntity继承自DomainEntity,并且扩展了另外两个属性--name和description<br/>
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
		public void setName(String name) {
			this.name = name;
		}
		
		@javax.persistence.Basic
		@javax.persistence.Column(name = "F_DESC", length = 2000)
		public String getDescription() {
			return this.description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
	}
```

*******************************
####*<b>RevisionDomainEntity</b>*<br/>

RevisionDomainEntity继承自SimpleDomainEntity,并且扩展了四个属性--creator，createdTime，lastModifier和lastModifiedTime<br/>
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
		public void setCreator(String creator) {
			this.creator = creator;
		}
		
		@javax.persistence.Temporal(TemporalType.TIMESTAMP)
		@javax.persistence.Column(name = "F_CREATED_TIME")
		public Date getCreatedTime() {
			return this.createdTime;
		}
		public void setCreatedTime(Date createdTime) {
			this.createdTime = createdTime;
		}
		
		@javax.persistence.Basic
		@javax.persistence.Column(name = "F_LAST_MODIFIER", length = 50)
		public String getLastModifier() {
			return this.lastModifier;
		}
		public void setLastModifier(String lastModifier) {
			this.lastModifier = lastModifier;
		}
		
		@javax.persistence.Temporal(TemporalType.TIMESTAMP)
		@javax.persistence.Column(name = "F_LAST_MODIFIED_TIME")
		public Date getLastModifiedTime() {
			return this.lastModifiedTime;
		}
		public void setLastModifiedTime(Date lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
		}
		
		public void prePersist(RevisionDomainEntity e) {
		}
	}
```

*******************************
####*<b>如何自定义实体类</b>*<br/>
假设我们有名为people的实体类，这个实体类有id，name，sex，age这些属性，那么，我们的实体类代码将如下：

```java
	@Entity<br/>
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

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getSex() {
			return sex;
		}

		public void setSex(String sex) {
			this.sex = sex;
		}
	}
```

通过继承DomainEntity，我们无需再重复定义id属性。<br/>
备注：上例中以<code>@</code>开头的注解,例如<code>@Entity</code>、<code>@Table</code>、<code>@Column</code>等都来源于[Hibernate Annotations] [4]，如果需要了解或者学习也可参考[hibernate-jpa-annotations] [5]。<br/>

*******************************

###<b>实体序列化</b>###

*******************************
###<b>框架组成结构</b>###
本框架后台分三层结构，分别为router，service和manager<br/>
<li>router用来定义请求规则和处理请求，作为所有请求的入口。router将调用service来处理业务。</li><br/>
<li>service作为业务处理类，所有的业务处理将在service层完成，包括事务处理。service处理完业务后调用manager层来做数据持久化操作。</li><br/>
<li>manager为数据持久层，用来完成数据库交互工作。</li><br/>

以下将对router，service和manager做详细介绍


####<b>router</b>###

#####*-createModuleRouter*
*Grammar* : createModuleRouter();<br/><br/>
*Explain* : 返回一个router对象<br/><br/>
*Example* :<br/>
>var {createModuleRouter} = require('coala/router');<br/>
>var router = exports.router = createModuleRouter();

#####*-createRouter*#####
*Grammar* : exports.createRouter();<br/><br/>
*Explain* : 返回一个router对象,此router自动绑定默认实现，参见attachDomain<br/><br/>
*Example* :<br/>
>var {createModuleRouter} = require('coala/router');<br/>
>var router = exports.router = createRouter();

#####*-autoMount*#####
*Grammar* : autoMount(router);<br/><br/>
*Explain* : 装载router<br/><br/>
*Example* :<br/>
>var {createModuleRouter} = require('coala/router');<br/>
>var router = exports.router = createModuleRouter();<br/>
>router.autoMount(this);<br/>

#####*-extendRouter*#####
*Grammar* : extendRouter(router);<br/><br/>
*Explain* : 继承一个router<br/><br/>
*Example* :<br/>
>var myrouter = router.extendRouter(this);

#####*-resolveEntity*#####
<b>Grammar</b> : resolveEntity(entityClass, params, converters);<br/><br/>
<b>Parameter Description</b>: 
	<li>entityClass为类路径</li>
	<li>params为object类型，存放entityClass对应的entity里存在的属性和值</li>
	<li>converters数据类型转器，详见<b>converters</b>介绍</li><br/>
<b>Explain</b> : 根据entityClass构建出entity对象，根据params给创建的entity对应的属性赋值，converters则作为新创建的entity的字段数据类型转换器<br/><br/>
<b>Example</b> :<br/>
>		var {createModuleRouter} = require('coala/router');
>		var router = exports.router = createModuleRouter();
>		router.autoMount(this);
>		var parms = new Object();
>		with(parms){
>			id = "123";
>			name = "张三";
>		}
>		var demo = router.resolveEntity('com.zyeeda.drivebox.entity',parms);


#####*-attachDomain*#####
<b>Grammar</b> :attachDomain(router, path, clazz, options)<br/><br/>
<b>Parameter Description</b>: 
<li>router -- router对象</li>
<li>path -- root路径</li>
<li>clazz -- 实体类</li>
<li>options -- 排除操作</li><br/>
<b>Explain</b> :自动对实体生成基本的list，get，create，update，remove和batchRemove操作<br/><br/>
<b>Example</b> :<br/>

#####*-createEntity*#####
<b>Grammar</b> :createEntity(clazz)<br/><br/>
<b>Parameter Description</b>: 
<li>clazz -- 类对象</li><br/>
<b>Explain</b> :根据类对象生成一个新的类实例<br/><br/>
<b>Example</b> :<br/>

#####*-getService*#####
<b>Grammar</b> :getService(options, entityMeta)<br/><br/>
<b>Parameter Description</b>: 
<li>options -- </li>
<li>entityMeta -- </li><br/>
<b>Explain</b> : <br/><br/>
<b>Example</b> :<br/>

#####*-getJsonFilter*#####
<b>Grammar</b> : getJsonFilter(options, type)<br/><br/>
<b>Parameter Description</b>: 
<li>options -- </li>
<li>type -- </li><br/>
<b>Explain</b> : 
<b>Example</b> :<br/>

#####*-defaultHandlers*#####
<b>Grammar</b> : <br/><br/>
<b>Parameter Description</b>: 
<b>Explain</b> :<br/><br/>
<b>Example</b> :<br/>

#####*-mergeEntityAndParameter*#####
<b>Grammar</b> :mergeEntityAndParameter(options, params, entityMeta, type, entity)<br/><br/>
<b>Parameter Description</b>: 
<li>options -- </li>
<li>params -- </li>
<li>entityMeta -- </li>
<li>type -- </li>
<li>entity -- </li>
<br/>
<b>Explain</b> :<br/>
<br/>
<b>Example</b> :<br/>

#####*-getOrderBy*#####
<b>Grammar</b> :getOrderBy(orders)<br/><br/>
<b>Parameter Description</b>: 
<li>orders -- </li><br/>
<b>Explain</b> :<br/><br/>
<b>Example</b> :<br/>

#####*-getPageInfo*#####
Grammar :getPageInfo(request, page)<br/><br/>
Parameter Description: 
<li>request -- </li>
<li>page -- </li><br/>
Explain :<br/><br/>
Example :<br/>

####<b>service</b>###
#####*-getEntityManager*#####
Grammar :getEntityManager(name)<br/><br/>
Parameter Description:
<li>name -EntityManagerFactory的名称</li><br/>
Explain :返回一个EntityManager<br/><br/>


#####*-createManager*#####
Grammar :createManager(entityClass, entityManagerFactoryName)<br/><br/>
Parameter Description:
<li>entityClass -- 实体的类路径</li>
<li>entityManagerFactoryName -- entityManagerFactory的名称</li><br/>
Explain :返回一个绑定了entity的Manager对象，entityClass为被绑定的entity的类路径<br/><br/>

####<b>manager</b>###
#####*-find*#####
Grammar :find([ids])<br/><br/>
Parameter Description:
<li>ids -- id数组</li><br/>
Explain :根据id查找并返回与Mnanager绑定的Entity对应的结果集<br/><br/>

#####*-getReference*#####
Grammar :getReference([ids])<br/><br/>
Parameter Description:
<li>ids -- id数组</li><br/>
Explain :根据id查找并返回与Mnanager绑定的Entity对应的结果集<br/><br/>

#####*-merge*#####
Grammar :merge([entities])<br/><br/>
Parameter Description:
<li>entities -- Entity数组</li><br/>
Explain :将Entity更新到数据库，并返回更新成功的记录<br/><br/>

#####*-save*#####
Grammar :save([entities])<br/><br/>
Parameter Description:
<li>entities -- Entity数组</li><br/>
Explain :将Entity保存到数据库，并返回成功保存的记录<br/><br/>

#####*-remove*#####
Grammar :remove([entities])<br/><br/>
Parameter Description:
<li>entities -- Entity数组</li><br/>
Explain :从数据库中删除Entity对应的数据，并返回成功删除的记录<br/><br/>

#####*-removeById*#####
Grammar :removeById([ids])<br/><br/>
Parameter Description:
<li>ids -- id数组</li><br/>
Explain :根据id删除数据库中对应的记录，并返回成功被删除的记录集合<br/><br/>

#####*-contains*#####
Grammar :contains(entity)<br/><br/>
Parameter Description:
<li>entity -- entity对象</li><br/>
Explain :判断数据库中是否已经存在此Entity所对应的记录，如果存在则返回true，否则返回false<br/><br/>

#####*-flush*#####
Grammar :flush()<br/><br/>
Parameter Description:NULL<br/><br/>
Explain :将处于游离状态的对象持久化到数据库中<br/><br/>

#####*-refresh*#####
Grammar :refresh([entites])<br/><br/>
Parameter Description:
<li>entites -- Entity数组</li><br/>
Explain :从数据库刷新实例的状态，如果有则覆盖实体的变化<br/><br/>

#####*-getAll*#####
Grammar :getAll(option)<br/><br/>
Parameter Description:
<li>option -- Object类型对象，此对象有以下属性orderBy，firstResult和maxResults</li><br/>
Explain :查询并返回所有结果集<br/><br/>

#####*-findByExample*#####
Grammar :findByExample(example, option)<br/><br/>
Parameter Description:
<li>example -- org.hibernate.criterion.Example对象</li>
<li>option -- Object类型对象，此对象有以下属性orderBy，firstResult，maxResults和fetchCount</li><br/>
Explain :通过Manager所绑定的Entity的来构造一个查询条件，并返回满足查询条件的结构集<br/><br/>
Example : 详细请参考[Criteria] [6] <br/>


####<b>service 及 marker</b>###

#####*-mark*#####
Grammar:mark(name, [attributes])<br/><br/>
Parameter Description:
<li>name -- mark类型名称</li>
<li>attributes -- mark的参数，一般为实例名称或者类型</li><br/>
Explain:用来注入，目前供提供四种注入类型，分别为inject，managers，services，tx。其中inject用来注入从springContext中得到的Bean；managers用来注入manager对象；services用来注入service；tx用来注入事物<br/><br/>

#####*-on*#####
Grammar:on(fn, me)<br/><br/>
Parameter Description:
<li>fn -- 回调函数</li>
<li>me -- 被绑定的对象</li><br/>

####<b>manager 和 scaffold</b>###




</script>
[1]: http://192.168.1.14/hg/zyeeda-framework-2.0 "zyeeda-framework-2.0"
[2]: http://mercurial.selenic.com/ "mercurial"
[3]: http://maven.apache.org/ "Maven"
[4]: http://docs.jboss.org/hibernate/annotations/3.5/reference/en/html_single/#entity-mapping "Hibernate JPA 2.0"
[5]: http://www.techferry.com/articles/hibernate-jpa-annotations.html "hibernate-jpa-annotations"
[6]: http://docs.jboss.org/hibernate/orm/4.1/devguide/en-US/html/ch12.html "Criteria"

