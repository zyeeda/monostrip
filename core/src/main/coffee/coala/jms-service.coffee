{mark}                  = require 'coala/mark'
{Session, DeliveryMode} = javax.jms
{HashMap}               = java.util

exports.createService = ->
    publishMessage: mark('beans', 'connectionFactory').on (connectionFactory, message) ->
        connection = connectionFactory.createConnection()
        session = connection.createSession false, Session.AUTO_ACKNOWLEDGE
        topic = session.createTopic 'directory.messages'
        publisher = session.createProducer topic
        publisher.setDeliveryMode DeliveryMode.NON_PERSISTENT
        connection.start()
        publisher.send session.createTextMessage(msg)
        connection.stop()
        connection.close()

    receiveMessage: mark('beans', 'connectionFactory').on (connectionFactory, fn) ->
        connection = connectionFactory.createConnection()
        session = connection.createSession false, Session.AUTO_ACKNOWLEDGE
        topic = session.createTopic 'directory.messages'
        connection.start()
        comsumer = session.createConsumer topic
        comsumer.setMessageListener(
            onMessage = (args) ->
                fn.apply null, args
        )
