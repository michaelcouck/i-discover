package discover.document;

import com.aspose.pdf.Document;
import com.aspose.pdf.HtmlLoadOptions;
import com.aspose.pdf.LoadOptions;
import ikube.toolkit.FILE;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public class DocumentGenerator {

    public void generatePdf(final int count) throws IOException {
        // final String connectiveGeneratedHtml = "./src/main/resources/connective-generated-html-document.html";
        // final File connectiveGeneratedHtmlFile = new File(connectiveGeneratedHtml);
        File connectiveGeneratedHtmlFile = FILE.findFileRecursively(new File("."), "connective-generated-html-document.html");
        CompletableFuture[] completableFutures = new CompletableFuture[count];
        byte[] bytes = Files.readAllBytes(connectiveGeneratedHtmlFile.toPath());
        for (int i = 0; i < count; i++) {
            // final int copy = i;
            completableFutures[i] = CompletableFuture.supplyAsync((Supplier<Void>) () -> {
                try {
                    InputStream clonedInputStream = new ByteArrayInputStream(bytes);
                    LoadOptions options = new HtmlLoadOptions();
                    Document document = new Document(clonedInputStream, options);
                    // document.save("./connective-generated-html-document-" + UUID.randomUUID().toString() + ".pdf");
                    OutputStream outputStream = new ByteArrayOutputStream();
                    document.save(outputStream);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        }
        CompletableFuture.allOf(completableFutures).join();
    }

}
