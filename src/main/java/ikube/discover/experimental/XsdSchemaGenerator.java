package ikube.discover.experimental;

import javax.xml.bind.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Amam rouy.
 */
public class XsdSchemaGenerator {

    String schema(final Class<?> clazz) throws JAXBException, IOException {
        final AtomicReference<String> xsdFileName = new AtomicReference<>();
        JAXBContext jc = JAXBContext.newInstance(clazz);
        jc.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(final String namespaceURI, final String suggestedFileName) {
                xsdFileName.set(clazz.getSimpleName().toLowerCase() + ".xsd");
                return new StreamResult(xsdFileName.get());
            }
        });
        return xsdFileName.get();
    }

    String marshall(final Object target) throws JAXBException, IOException {
        JAXBContext jaxbContext;
        OutputStream outputStream = new ByteArrayOutputStream();
        jaxbContext = JAXBContext.newInstance(target.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(target, outputStream);
        return outputStream.toString();
    }

    <T> T unmarshall(final Class<T> targetClass, final InputStream inputStream) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(targetClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        //noinspection unchecked
        return (T) jaxbUnmarshaller.unmarshal(inputStream);
    }

}
