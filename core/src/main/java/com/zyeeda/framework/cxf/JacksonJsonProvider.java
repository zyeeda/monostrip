package com.zyeeda.framework.cxf;

import java.text.SimpleDateFormat;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.SerializationConfig.Feature;

public class JacksonJsonProvider extends JacksonJaxbJsonProvider {

	@SuppressWarnings("deprecation")
    public JacksonJsonProvider() {
		ObjectMapper m = this._mapperConfig.getConfiguredMapper();
		if (m == null) {
			m = this._mapperConfig.getDefaultMapper();
		}
		SerializationConfig sc = m.getSerializationConfig().withDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		sc.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		sc.set(Feature.FAIL_ON_EMPTY_BEANS, false);
		m.setSerializationConfig(sc);
	}
}
