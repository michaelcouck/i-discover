package ikube.discover.experimental;

import ikube.discover.database.model.Wrapper;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Amam rouy.
 */
public class XsdSchemaGenerator {

    String generateSchemas(final Class<?> clazz) throws JAXBException, IOException {
        final AtomicReference<String> xsdFileName = new AtomicReference<>();
        JAXBContext jc = JAXBContext.newInstance(clazz);
        jc.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(final String namespaceURI, final String suggestedFileName) {
                xsdFileName.set(clazz.getSimpleName() + ".xsd");
                return new StreamResult(xsdFileName.get());
            }
        });
        return xsdFileName.get();
    }

    String marshall(final Object target) throws JAXBException, IOException {
        Object toMarshall;
        JAXBContext jaxbContext;
        OutputStream outputStream = new ByteArrayOutputStream();

        if (!List.class.isAssignableFrom(target.getClass())) {
            jaxbContext = JAXBContext.newInstance(target.getClass());
            toMarshall = target;
        } else {
            jaxbContext = JAXBContext.newInstance(Wrapper.class);
            Wrapper<JAXBElement> wrapper = new Wrapper<>();
            toMarshall = wrapper;
            for (final Object child : (List) target) {
                Class<?> childClass = child.getClass();
                @SuppressWarnings("unchecked")
                JAXBElement<?> jaxbElement = new JAXBElement(new QName(childClass.getSimpleName()), childClass, child);
                wrapper.getElements().add(jaxbElement);
            }
        }

        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshall(toMarshall, jaxbMarshaller, outputStream);
        return outputStream.toString();
    }

    @SuppressWarnings("unchecked")
    void marshall(final Object target, final Marshaller jaxbMarshaller, final OutputStream outputStream) throws JAXBException {
        jaxbMarshaller.marshal(target, outputStream);
    }

    <T> T unmarshall(final Class<T> targetClass, final InputStream inputStream) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(targetClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        @SuppressWarnings("unchecked")
        T target = (T) jaxbUnmarshaller.unmarshal(inputStream);
        return target;
    }

}
