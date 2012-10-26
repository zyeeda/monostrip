
{coala} = require 'coala/config'

{SimpleDateFormat}                      = java.text
{ObjectMapper, SerializationFeature}    = com.fasterxml.jackson.databind
{AfterburnerModule}                     = com.fasterxml.jackson.module.afterburner
{JodaModule}                            = com.fasterxml.jackson.datatype.joda

mapper = new ObjectMapper()
mapper.registerModule new AfterburnerModule()
mapper.registerModule new JodaModule()

df = new SimpleDateFormat(coala.dateFormat)
mapper.configure SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false
mapper.setDateFormat df

exports.objectMapper = mapper

