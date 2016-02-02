package ikube.discover.analyze.train;

import ikube.discover.AbstractTest;
import ikube.discover.tool.THREAD;
import org.junit.Before;
import org.junit.Test;
import org.neuroph.core.Layer;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.core.data.DataSet;
import org.neuroph.samples.convolution.mnist.MNISTDataSet;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * TODO: JavaDoc
 *
 * @author Michael Couck
 * @version 01.00
 * @since 01-01-2016
 */
public class NeurophMultiLayerPerceptronTrainTest extends AbstractTest {

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
    public void train() throws IOException, ExecutionException, InterruptedException {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int chunks = operatingSystemMXBean.getAvailableProcessors();
        List<Future<Object>> futures = new ArrayList<>();
        for (int i = 0; i < chunks; i++) {
            DataSet subTrainingDataSet = getSubDataSet(trainingDataSet, i, chunks);
            DataSet subCrossValidationDataSet = getSubDataSet(crossValidationDataSet, i, chunks);
            @SuppressWarnings("Convert2Lambda")
            Future future = THREAD.submit("" + i, new Callable<Layer[]>() {
                @Override
                public Layer[] call() throws Exception {
                    NeurophMultiLayerPerceptronTrain neurophTrain = new NeurophMultiLayerPerceptronTrain(subTrainingDataSet, subCrossValidationDataSet);
                    try {
                        return neurophTrain.train();
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

    @SuppressWarnings("UnusedDeclaration")
    private void printWeights(final List<Future<Object>> futures) throws ExecutionException, InterruptedException {
        for (final Future future : futures) {
            Layer[] layers = (Layer[]) future.get();
            logger.info("Layers : " + layers.length);
            for (final Layer layer : layers) {
                Neuron[] neurons = layer.getNeurons();
                logger.info("Neurons : " + neurons.length);
                for (final Neuron neuron : neurons) {
                    Weight[] weights = neuron.getWeights();
                    logger.info("Weights : " + Arrays.toString(weights));
                    for (final Weight weight : weights) {
                        weight.getValue();
                    }
                }
            }
        }
    }

}