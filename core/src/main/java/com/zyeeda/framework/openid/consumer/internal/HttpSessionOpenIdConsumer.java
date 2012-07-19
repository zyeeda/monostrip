package com.zyeeda.framework.openid.consumer.internal;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.framework.openid.consumer.OpenIdConsumer;

public class HttpSessionOpenIdConsumer implements OpenIdConsumer {
	
	private final static Logger logger = LoggerFactory.getLogger(HttpSessionOpenIdConsumer.class);

	protected final static String OPENID_DISCOVERED_KEY = "openid.discovered";
	
	private String returnToUrl;
	private String realm;
	
	private ConsumerManager manager;
	
	public HttpSessionOpenIdConsumer() throws ConsumerException {
		this.manager = new ConsumerManager();
		this.manager.setConnectTimeout(300000);
		this.manager.setSocketTimeout(300000);
        this.manager.setAssociations(new InMemoryConsumerAssociationStore());
        this.manager.setNonceVerifier(new InMemoryNonceVerifier(60000));
        this.manager.getRealmVerifier().setEnforceRpId(false);
	}
	
	@Override
	public AuthRequest authRequest(String userSuppliedId,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) throws OpenIDException {
		
		logger.info("user supplied id = {}", userSuppliedId);
		
		List<?> discos = this.manager.discover(userSuppliedId);
		DiscoveryInformation discovered = this.manager.associate(discos);
		this.storeDiscoveryInfo(httpReq, discovered);
		AuthRequest authReq = this.manager.authenticate(discovered, this.returnToUrl);
		authReq.setRealm(this.realm);
		
		return authReq;
	}
	
	@Override
	public Identifier verifyResponse(HttpServletRequest httpReq) throws OpenIDException {
		ParameterList response = new ParameterList(httpReq.getParameterMap());

		DiscoveryInformation discovered = this.retrieveDiscoveryInfo(httpReq);

		StringBuilder receivingURL = new StringBuilder(this.returnToUrl);
		String queryString = httpReq.getQueryString();
		if (queryString != null && queryString.length() > 0) {
			receivingURL.append("?").append(httpReq.getQueryString());
		}

		VerificationResult verification = this.manager.verify(receivingURL.toString(), response, discovered);

		Identifier verified = verification.getVerifiedId();
		if (verified == null) {
			throw new OpenIDException("Cannot verify OpenID auth response.");
		}
		
		/*AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();
		if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX) && this.axExtConsumer != null) {
			MessageExtension ext = authSuccess.getExtension(AxMessage.OPENID_NS_AX);
			if (ext instanceof FetchResponse) {
				FetchResponse fetchResp = (FetchResponse) ext;
				this.axExtConsumer.processFetchResponse(fetchResp);
			}
		}*/
		// TODO some more extension
		return verified;
	}
	
	@Override
	public void setReturnToUrl(String returnToUrl) {
		this.returnToUrl = returnToUrl;
	}
	
	@Override
	public String getReturnToUrl() {
		return this.returnToUrl;
	}

	@Override
	public String getRealm() {
		return realm;
	}

	@Override
	public void setRealm(String realm) {
		this.realm = realm;
	}
	
	@Override
	public void storeDiscoveryInfo(HttpServletRequest httpReq, DiscoveryInformation discovered) {
		httpReq.getSession().setAttribute(OPENID_DISCOVERED_KEY, discovered);
	}
	
	@Override
	public DiscoveryInformation retrieveDiscoveryInfo(HttpServletRequest httpReq) {
		return (DiscoveryInformation) httpReq.getSession().getAttribute(OPENID_DISCOVERED_KEY);
	}
	
	/*@Override
	public AxExtensionConsumer getAxExtensionConsumer() {
		return this.axExtConsumer;
	}
	
	@Override
	public void setAxExtensionConsumer(AxExtensionConsumer axExtConsumer) {
		this.axExtConsumer = axExtConsumer;
	}*/
	
}
