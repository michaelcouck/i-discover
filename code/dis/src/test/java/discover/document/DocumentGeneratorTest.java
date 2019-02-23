package discover.document;


import org.junit.Test;

import java.io.IOException;

public class DocumentGeneratorTest {

    @Test
    public void generatePdf() throws IOException {
        double start = System.currentTimeMillis();
        double documents = 100;
        new DocumentGenerator().generatePdf((int) documents);
        double duration = (System.currentTimeMillis() - start) / 1000;
        System.out.println("Duration : " + duration + ", documents per second : " + (documents / duration));
    }

}