package ikube.discover.webservice;

import ikube.discover.webservice.model.Authorization;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(endpointInterface = MandateAuthorization.MANDATE_AUTHORIZATION_ENDPOINT)
public class MandateAuthorizationImpl implements MandateAuthorization {

    @Override
    @WebMethod
    @WebResult(name = "authorization")
    public Authorization authorizeMandate(@WebParam(name = "authorization") final Authorization authorization) {
        return authorization;
    }

}
