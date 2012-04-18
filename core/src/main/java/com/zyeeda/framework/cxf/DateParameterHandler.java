package com.zyeeda.framework.cxf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.ParameterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateParameterHandler implements ParameterHandler<Date> {
	
	private static final Logger logger = LoggerFactory.getLogger(DateParameterHandler.class);

	@Override
	public Date fromString(String str) {
		logger.debug("date/datetime string = {}", str);
		if (StringUtils.isBlank(str)) {
			return null;
		}
		
		String[] parts = StringUtils.split(str);
		SimpleDateFormat sdf = null;
		try {
			if (parts.length == 1) {
				logger.debug("parsing string using date format: yyyy-MM-dd");
				sdf = new SimpleDateFormat("yyyy-MM-dd");
				return sdf.parse(str);
			}
			if (parts.length == 2) {
				logger.debug("parsing string using datetime format: yyyy-MM-dd hh:mm:ss");
				sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
				return sdf.parse(str);
			}
			throw new RuntimeException("Invalid date/datetime format.");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

}
