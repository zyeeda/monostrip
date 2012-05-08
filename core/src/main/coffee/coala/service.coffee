{EntityManager, EntityManagerFactory} = javax.persistence;
{EntityManagerFactoryUtils} = org.springframework.orm.jpa;
{Context} = com.zyeeda.framework.web.SpringAwareJsgiServlet;
{createManager} = require 'coala/manager'

exports.createService = ->

    context: Context.getInstance()

    # parameter name is the name of EntityManagerFactory which is configed in spring context
    getEntityManager: (name = false) ->
        emf = if name then @context.getBean name else @context.getBeanByClass EntityManagerFactory
        em = EntityManagerFactoryUtils.doGetTransactionalEntityManager emf, null
        throw new Error('can not find an EntityManager in current thread') unless em?
        em

    createManager: (entityClass, entityManagerFactoryName = false) ->
        em = @getEntityManager entityManagerFactoryName
        createManager em, entityClass

    # invoke manager's __noSuchMethod__
    # args[0] is entity class, args[1] is query options
    __noSuchMethod__: (name,args) ->
        dao = @createManager args[0]
        dao[name] args[1]
