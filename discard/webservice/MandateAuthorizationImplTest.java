package ikube.discover.webservice;

import ikube.discover.webservice.model.Authorization;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

public class MandateAuthorizationImplTest {

    @Test
    public void authorizeMandate() throws MalformedURLException {
        try {
            String endpoint = "http://localhost:8080/ws/mandate-authorization";
            // Create & publish the web service
            Endpoint.publish(endpoint, new MandateAuthorizationImpl());
            // Invoke the web service
            URL url = new URL(endpoint + "?wsdl");
            //1st argument service URI, refer to wsdl document above
            //2nd argument is service name, refer to wsdl document above
            QName qname = new QName("http://webservice.discover.ikube/", MandateAuthorization.SERVICE_NAME);
            Service service = Service.create(url, qname);
            MandateAuthorization mandateAuthorization = service.getPort(MandateAuthorization.class);
            Authorization authorization = new Authorization();
            System.out.println(mandateAuthorization.authorizeMandate(authorization));
        } catch (final Exception e) {
            e.printStackTrace();
            //try {
            //    Object response = Class.forName("ikube.discover.webservice.jaxws.AuthorizeMandateResponse").newInstance();
            //    System.out.println(response);
            //} catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
            //    e1.printStackTrace();
            //}
        }
    }

}
