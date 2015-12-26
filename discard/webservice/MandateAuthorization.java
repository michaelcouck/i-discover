package ikube.discover.webservice;

import ikube.discover.webservice.model.Authorization;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService
public interface MandateAuthorization {

    public static final String NAME = "MandateAuthorization";
    public static final String SERVICE_NAME = NAME + "ImplService";
    public static final String MANDATE_AUTHORIZATION_ENDPOINT = "ikube.discover.webservice.MandateAuthorization";

    @WebMethod
    @WebResult
    Authorization authorizeMandate(@WebParam(name = "authorization") final Authorization authorization);

}
