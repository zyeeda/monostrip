package com.zyeeda.framework.openid.consumer.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;

import com.zyeeda.framework.openid.consumer.OpenIdConsumer;
import com.zyeeda.framework.openid.consumer.OpenIdConsumerService;
import com.zyeeda.framework.service.AbstractService;

public class DefaultOpenIdConsumerServiceProvider extends AbstractService implements OpenIdConsumerService {

    private String returnToUrl;
    
    private String openIdProvider;
    
    private String realm;
    
    private OpenIdConsumer consumer;
    
    public void setReturnToUrl(String returnToUrl) {
        this.returnToUrl = returnToUrl;
    }

    public void setOpenIdProvider(String openIdProvider) {
        this.openIdProvider = openIdProvider;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    public void start() throws Exception {
        this.consumer = new ShiroSessionOpenIdConsumer();
        this.consumer.setReturnToUrl(this.returnToUrl);
        this.consumer.setRealm(this.realm);
    }
    
    @Override
    public void stop() {
        this.consumer = null;
    }

	@Override
	public AuthRequest authRequest(HttpServletRequest request, HttpServletResponse response) throws OpenIDException {
		return this.consumer.authRequest(this.openIdProvider, request, response);
	}

	@Override
	public Identifier verifyResponse(HttpServletRequest request) throws OpenIDException {
		return this.consumer.verifyResponse(request);
	}

}
