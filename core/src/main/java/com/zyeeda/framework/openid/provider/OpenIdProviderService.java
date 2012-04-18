package com.zyeeda.framework.openid.provider;

import org.openid4java.association.AssociationException;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerException;

import com.zyeeda.framework.service.Service;

public interface OpenIdProviderService extends Service {

	public Message associateRequest(ParameterList params);
	
	public Message verifyRequest(ParameterList params);
	
	public Message authResponse(ParameterList params, String userSelectedId,
			String userSelectedClaimedId, boolean authenticatedAndApproved, String opEndpointUrl) throws MessageException, ServerException, AssociationException;
	
	public String getEndpointUrl();
	
	public String getEndpointCompleteUrl();
	
}
