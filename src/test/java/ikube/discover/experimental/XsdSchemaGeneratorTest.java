package ikube.discover.experimental;

import ikube.discover.AbstractTest;
import ikube.discover.database.model.*;
import ikube.discover.tool.FILE;
import ikube.discover.tool.OBJECT;
import ikube.discover.tool.PERFORMANCE;
import org.junit.Test;
import org.mockito.Spy;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

/**
 * Amam rouy.
 */
public class XsdSchemaGeneratorTest extends AbstractTest {

    @Spy
    private XsdSchemaGenerator xsdSchemaGenerator;

    @Test
    public void generateSchemas() throws JAXBException, IOException {
        xsdSchemaGenerator.schema(Wrapper.class);
    }

    @Test
    public void marshall() throws JAXBException {
        System.out.println("Generating...");
        Wrapper analyses = new Wrapper();
        for (int i = 0; i < 10; i++) {
            Analysis analysis = OBJECT.populateFields(new Analysis(), true, 10);
            //noinspection unchecked
            analyses.getElements().add(analysis);
        }
        System.out.println("Marshalling...");
        double duration = PERFORMANCE.execute(new PERFORMANCE.APerform() {
            public void execute() throws JAXBException, IOException {
                String xml = xsdSchemaGenerator.marshall(analyses);
                System.out.println(xml.length());
                FILE.setContents(new File("analyses.xml"), xml.getBytes());
            }
        }, "Marshalling : ", 1, true);
        System.out.println("Elements per second : " + (((double) analyses.getElements().size()) / duration));
    }

    @Test
    public void unmarshall() {
    }

}