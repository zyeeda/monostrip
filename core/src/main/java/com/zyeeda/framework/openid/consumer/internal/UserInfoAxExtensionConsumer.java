package com.zyeeda.framework.openid.consumer.internal;

import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

import com.zyeeda.framework.openid.consumer.AxExtensionConsumer;

public class UserInfoAxExtensionConsumer implements AxExtensionConsumer {

	@Override
	public FetchRequest prepareFetchRequest() throws MessageException {
		FetchRequest fetchReq = FetchRequest.createFetchRequest();
		fetchReq.addAttribute("id", "http://axschema.org/namePerson/friendly", true);
		fetchReq.addAttribute("username", "http://axschema.org/namePerson", false);
		fetchReq.addAttribute("gender", "http://axschema.org/person/gender", false);
		fetchReq.addAttribute("position", "http://zyeeda.com/openid/ax/csg/person/position", false);
		fetchReq.addAttribute("degree", "http://zyeeda.com/openid/ax/csg/person/degree", false);
		fetchReq.addAttribute("email", "http://axschema.org/contact/email", false);
		fetchReq.addAttribute("mobile", "http://axschema.org/contact/phone/cell", false);
		fetchReq.addAttribute("birthday", "http://axschema.org/birthDate", false);
		fetchReq.addAttribute("dateOfWork", "http://zyeeda.com/openid/ax/csg/person/dateOfWork", false);
		fetchReq.addAttribute("department", "http://zyeeda.com/openid/ax/csg/person/department", false);
		return fetchReq;
	}

	@Override
	public void processFetchResponse(FetchResponse fetchResp) {
		// TODO Auto-generated method stub

	}

}
