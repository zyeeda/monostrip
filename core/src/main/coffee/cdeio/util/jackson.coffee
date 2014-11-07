
{cdeio} = require 'cdeio/config'

{TimeZone}                              = java.util
{SimpleDateFormat}                      = java.text
{ObjectMapper, SerializationFeature}    = com.fasterxml.jackson.databind
{AfterburnerModule}                     = com.fasterxml.jackson.module.afterburner
{JodaModule}                            = com.fasterxml.jackson.datatype.joda
{CustomIntrospector}                    = com.zyeeda.cdeio.jackson

mapper = new ObjectMapper()
mapper.registerModule new AfterburnerModule()
mapper.registerModule new JodaModule()
mapper.setAnnotationIntrospector new CustomIntrospector()

df = new SimpleDateFormat(cdeio.dateTimeFormat)

mapper.configure SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false
mapper.setDateFormat df
mapper.setTimeZone TimeZone.default

exports.objectMapper = mapper
