package com.zyeeda.framework.cxf;

import org.apache.cxf.jaxrs.provider.JSONProvider;

public class DedicatedJsonProvider extends JSONProvider {

	public DedicatedJsonProvider() {
		super();
		
		this.setSerializeAsArray(true);
		this.setDropRootElement(true);
		this.setDropCollectionWrapperElement(true);
		this.setEnableBuffering(true);
	}
}
