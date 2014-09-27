{EntityManagerFactoryUtils, EntityManagerHolder} = org.springframework.orm.jpa
{TransactionSynchronizationManager} = org.springframework.transaction.support

exports.handler = (context, attributes, fn, args) ->
    emName = attributes[0] or 'entityManagerFactory'
    alreadyExists = false
    emf = context.getBean emName

    if TransactionSynchronizationManager.hasResource(emf)
        alreadyExists = true
    else
        em = emf.createEntityManager()
        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em));

    try
        fn.apply null, args
    finally
        if not alreadyExists
            emHolder = TransactionSynchronizationManager.unbindResource(emf)
            EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager())
