package com.zyeeda.framework.openid.consumer.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;

import com.zyeeda.framework.openid.consumer.OpenIdConsumer;
import com.zyeeda.framework.openid.consumer.OpenIdConsumerService;

public class DefaultOpenIdConsumerServiceProvider implements OpenIdConsumerService {
    

	private String returnToUrl;
	private String openIdProvider;
	private String realm;
	
	public String getReturnToUrl() {
        return returnToUrl;
    }

    public void setReturnToUrl(String returnToUrl) {
        this.returnToUrl = returnToUrl;
    }

    public String getOpenIdProvider() {
        return openIdProvider;
    }

    public void setOpenIdProvider(String openIdProvider) {
        this.openIdProvider = openIdProvider;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    private OpenIdConsumer openIdConsumer;
	

	public OpenIdConsumer getOpenIdConsumer() {
        return openIdConsumer;
    }

    public void setOpenIdConsumer(OpenIdConsumer openIdConsumer) {
        this.openIdConsumer = openIdConsumer;
    }

	@Override
	public AuthRequest authRequest(HttpServletRequest request, HttpServletResponse response) throws OpenIDException {
		return this.openIdConsumer.authRequest(this.openIdProvider, request, response);
	}

	@Override
	public Identifier verifyResponse(HttpServletRequest request) throws OpenIDException {
		return this.openIdConsumer.verifyResponse(request);
	}

}
