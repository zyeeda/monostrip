{EntityManager, EntityManagerFactory} = javax.persistence;
{EntityManagerFactoryUtils} = org.springframework.orm.jpa;
{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet;
{createManager} = require 'coala/manager'
{objects,type} = require 'coala/util'
{tx} = require 'coala/tx'

exports.createService = (entityClass) ->

    context: Context.getInstance()
    entityClass: entityClass
    # parameter name is the name of EntityManagerFactory which is configed in spring context
    getEntityManager: (name = false) ->
        emf = if name then @context.getBean name else @context.getBeanByClass EntityManagerFactory
        em = EntityManagerFactoryUtils.doGetTransactionalEntityManager emf, null
        throw new Error('can not find an EntityManager in current thread') unless em?
        em

    createManager: (entityClass, entityManagerFactoryName = false) ->
        em = @getEntityManager entityManagerFactoryName
        clazz = @entityClass or entityClass
        createManager em, clazz

    tx: (cb) ->
        tx @context, cb

    __noSuchMethod__: (name,args) ->
        dao = @createManager @entityClass
        dao[name] args[0]

exports.servicesAnnotationHandler = (context, attributes, fn, args) ->
    attributes = [attributes] if type(attributes) is 'string'
    throw new Error('attributes must be a string or an string array') if type(attributes) isnt 'array'
    services = (require(m).createService() for m in attributes)
    args = services.concat args
    fn.apply null, args
