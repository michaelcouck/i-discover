package ikube.discover.analyze.train;

import org.neuroph.core.Layer;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.nnet.ConvolutionalNetwork;
import org.neuroph.nnet.comp.Kernel;
import org.neuroph.nnet.comp.layer.Layer2D;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * TODO: JavaDoc
 *
 * @author Michael Couck
 * @version 01.00
 * @since 01-01-2016
 */
public class NeurophConvolutionalNeuralNetworkTrain implements ITrain<Layer[]> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    class LearningListener implements LearningEventListener {

        long start = System.currentTimeMillis();

        public void handleLearningEvent(final LearningEvent event) {
            BackPropagation backPropagation = (BackPropagation) event.getSource();
            double calculationTime = (double) (System.currentTimeMillis() - this.start) / 1000.0D;
            logger.info(
                    "  Iteration: " + backPropagation.getCurrentIteration() +
                    ", error : " + backPropagation.getTotalNetworkError() +
                    ", time : " + calculationTime);
            this.start = System.currentTimeMillis();
        }
    }

    private DataSet trainingDataSet;
    private DataSet crossValidationDataSet;

    NeurophConvolutionalNeuralNetworkTrain(final DataSet trainingDataSet, final DataSet crossValidationDataSet) {
        this.trainingDataSet = trainingDataSet;
        this.crossValidationDataSet = crossValidationDataSet;
    }

    @Override
    public Layer[] train() throws IOException {
        Layer2D.Dimensions inputDimension = new Layer2D.Dimensions(32, 32);
        Kernel convolutionKernel = new Kernel(5, 5);
        Kernel poolingKernel = new Kernel(2, 2);

        MomentumBackpropagation backPropagation = new MomentumBackpropagation();
        backPropagation.setMomentum(0.7);
        backPropagation.setLearningRate(0.1);
        backPropagation.setMaxError(0.1);
        backPropagation.setMaxIterations(100);
        backPropagation.addListener(new LearningListener());
        backPropagation.setErrorFunction(new MeanSquaredError());

        ConvolutionalNetwork convolutionNetwork = new ConvolutionalNetwork
                .Builder(inputDimension, 1)
                .withConvolutionLayer(convolutionKernel, 10)
                .withPoolingLayer(poolingKernel)
                .withConvolutionLayer(convolutionKernel, 1)
                .withPoolingLayer(poolingKernel)
                .withConvolutionLayer(convolutionKernel, 1)
                .withFullConnectedLayer(10)
                .createNetwork();
        convolutionNetwork.setLearningRule(backPropagation);

        logger.info("Started training...");
        convolutionNetwork.learn(trainingDataSet);
        logger.info("Done training!");

        convolutionNetwork.save("/mnist.nnet");

        // CrossValidation crossValidation = new CrossValidation(convolutionNetwork, crossValidationDataSet, 5);
        // crossValidation.run();

        convolutionNetwork.setInput(crossValidationDataSet.getRowAt(0).getInput());
        convolutionNetwork.calculate();
        double[] output = convolutionNetwork.getOutput();
        logger.info("Output : " + Arrays.toString(output));

        return convolutionNetwork.getLayers();
    }

}
