package com.zyeeda.framework.openid.provider.internal;

import javax.annotation.Resource;

import org.openid4java.association.AssociationException;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerException;
import org.openid4java.server.ServerManager;

import com.zyeeda.framework.openid.provider.OpenIdProviderService;
import com.zyeeda.framework.service.AbstractService;

public class DefaultOpenIdProviderService extends AbstractService implements OpenIdProviderService {

private final static String ENDPOINT_URL_KEY = "endpointUrl";
    
    private final static String DEFAULT_ENDPOINT_URL = "/provider/endpoint";
    private final static String DEFAULT_ENDPOINT_COMPLETE_URL = "/provider/endpoint/complete";
    
    private String endpointUrl;
	private ServerManager serverManager;
	
	@Resource(name=ENDPOINT_URL_KEY)
	public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    @Override
    public void start() throws Exception {
	    if( this.endpointUrl == null ) {
            this.endpointUrl = DEFAULT_ENDPOINT_URL;
        }
        
        this.serverManager = new ServerManager();
        this.serverManager.setOPEndpointUrl(this.endpointUrl);
        this.serverManager.setSharedAssociations(new InMemoryServerAssociationStore());
        this.serverManager.setPrivateAssociations(new InMemoryServerAssociationStore());
        this.serverManager.getRealmVerifier().setEnforceRpId(false);
	}

	@Override
	public Message associateRequest(ParameterList params) {
		Message message = this.serverManager.associationResponse(params);
		return message;
	}

	@Override
	public Message verifyRequest(ParameterList params) {
        Message message = this.serverManager.verify(params);
		return message;
	}
	
	@Override
	public Message authResponse(ParameterList params, String userSelectedId,
			String userSelectedClaimedId, boolean authenticatedAndApproved, String opEndpointUrl) throws MessageException, ServerException, AssociationException {
		Message message = this.serverManager.authResponse(params, userSelectedId,
				userSelectedClaimedId, authenticatedAndApproved, opEndpointUrl, true);
		/*if (message instanceof AuthSuccess) {
			AuthSuccess authSuccess = (AuthSuccess) message;
			
			AuthRequest authReq = AuthRequest.createAuthRequest(params, this.serverManager.getRealmVerifier());
			if (authReq.hasExtension(AxMessage.OPENID_NS_AX)) {
				MessageExtension ext = authReq.getExtension(AxMessage.OPENID_NS_AX);
				if (ext instanceof FetchRequest) {
					FetchRequest fetchReq = (FetchRequest)ext;
					Map<?, ?> required = fetchReq.getAttributes(true);
					//Map<?, ?> optional = fetchReq.getAttributes(false);
					
					Map<String, String> userData = new HashMap<String, String>(10);
					if (required.containsKey("id")) {
						userData.put("id", userSelectedClaimedId);
					}
					
					FetchResponse fetchResp = FetchResponse.createFetchResponse(fetchReq, userData);
					authSuccess.addExtension(fetchResp);
				}
			}
			// TODO other extensions support
			this.serverManager.sign(authSuccess);
		}*/
		return message;
	}
	
	@Override
	public String getEndpointUrl() {
		return this.endpointUrl;
	}
	
	@Override
	public String getEndpointCompleteUrl() {
		return DEFAULT_ENDPOINT_COMPLETE_URL;
	}

}
