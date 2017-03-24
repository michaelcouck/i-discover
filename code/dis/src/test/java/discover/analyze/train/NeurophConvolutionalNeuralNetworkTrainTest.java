package discover.analyze.train;

import discover.AbstractTest;
import ikube.toolkit.THREAD;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neuroph.core.data.DataSet;
import org.neuroph.samples.convolution.mnist.MNISTDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * TODO: JavaDoc
 *
 * @author Michael Couck
 * @version 01.00
 * @since 01-01-2016
 */
public class NeurophConvolutionalNeuralNetworkTrainTest extends AbstractTest {

    private DataSet crossValidationDataSet;
    private DataSet trainingDataSet;

    @Before
    public void before() throws IOException {
        THREAD.initialize();
        trainingDataSet = MNISTDataSet.createFromFile("src/main/resources/data_sets/train-labels.idx1-ubyte",
                "src/main/resources/data_sets/train-images.idx3-ubyte", '\uea60');
        crossValidationDataSet = MNISTDataSet.createFromFile("src/main/resources/data_sets/t10k-labels.idx1-ubyte",
                "src/main/resources/data_sets/t10k-images.idx3-ubyte", 10000);
    }

    @Test
    @Ignore
    public void train() throws IOException, ExecutionException, InterruptedException {
        int chunks = 64;
        List<Future<Object>> futures = new ArrayList<>();
        for (int i = 0; i < chunks; i++) {
            DataSet subTrainingDataSet = getSubDataSet(trainingDataSet, i, chunks);
            DataSet subCrossValidationDataSet = getSubDataSet(crossValidationDataSet, i, chunks);
            @SuppressWarnings("Convert2Lambda")
            Future future = THREAD.submit("" + i, new Runnable() {
                @Override
                public void run() {
                    NeurophConvolutionalNeuralNetworkTrain neurophTrain = new NeurophConvolutionalNeuralNetworkTrain(subTrainingDataSet, subCrossValidationDataSet);
                    try {
                        neurophTrain.train();
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            //noinspection unchecked
            futures.add(future);
        }
        THREAD.waitForFutures(futures, Integer.MAX_VALUE);
        // Aggregate the results dependant on the similarity of the models
        // printWeights(futures);
    }

    private DataSet getSubDataSet(final DataSet dataSet, final int batchNumber, final int chunks) {
        int batchSize = dataSet.size() / chunks;
        DataSet subDataSet = new DataSet(1024, 10);
        for (int j = batchNumber * batchSize; j < (batchNumber * batchSize) + batchSize; j++) {
            subDataSet.addRow(dataSet.getRowAt(j));
        }
        return subDataSet;
    }

}