package ikube.discover.analyze.train;

import ikube.discover.AbstractTest;
import org.junit.Test;
import org.mockito.Spy;

import java.io.IOException;

public class NeurophTrainTest extends AbstractTest {

    @Spy
    private NeurophTrain neurophTrain;

    @Test
    public void train() throws IOException {
        neurophTrain.train();
    }

}