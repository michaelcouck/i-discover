package ikube.discover.analyze.train;

import org.neuroph.contrib.model.errorestimation.CrossValidation;
import org.neuroph.core.Layer;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.nnet.ConvolutionalNetwork;
import org.neuroph.nnet.comp.Kernel;
import org.neuroph.nnet.comp.layer.FeatureMapsLayer;
import org.neuroph.nnet.comp.layer.Layer2D;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;

import java.io.IOException;
import java.util.Arrays;

/**
 * TODO: JavaDoc
 *
 * @author Michael Couck
 * @version 01.00
 * @since 01-01-2016
 */
public class NeurophTrain implements ITrain<Layer[]> {

    class LearningListener implements LearningEventListener {

        long start = System.currentTimeMillis();

        public void handleLearningEvent(final LearningEvent event) {
            BackPropagation bp = (BackPropagation) event.getSource();
            System.out.println("Current iteration: " + bp.getCurrentIteration());
            System.out.println("Error: " + bp.getTotalNetworkError());
            System.out.println("Calculation time: " + (double) (System.currentTimeMillis() - this.start) / 1000.0D);
            this.start = System.currentTimeMillis();
        }
    }

    private DataSet trainingDataSet;
    private DataSet crossValidationDataSet;

    NeurophTrain(final DataSet trainingDataSet, final DataSet crossValidationDataSet) {
        this.trainingDataSet = trainingDataSet;
        this.crossValidationDataSet = crossValidationDataSet;
    }

    @Override
    public Layer[] train() throws IOException {
        Layer2D.Dimensions inputDimension = new Layer2D.Dimensions(32, 32);
        Kernel convolutionKernel = new Kernel(5, 5);
        Kernel poolingKernel = new Kernel(2, 2);

        MomentumBackpropagation backPropagation = new MomentumBackpropagation();
        backPropagation.setLearningRate(1.0E-4D);
        backPropagation.setMaxError(1.0E-5D);
        backPropagation.setMaxIterations(500);
        backPropagation.addListener(new LearningListener());
        backPropagation.setErrorFunction(new MeanSquaredError());

        ConvolutionalNetwork convolutionNetwork = new ConvolutionalNetwork.Builder(inputDimension, 1)
                .withConvolutionLayer(convolutionKernel, 10)
                .withPoolingLayer(poolingKernel)
                .withConvolutionLayer(convolutionKernel, 1)
                .withPoolingLayer(poolingKernel)
                .withConvolutionLayer(convolutionKernel, 1)
                .withFullConnectedLayer(10)
                .createNetwork();
        convolutionNetwork.setLearningRule(backPropagation);

        System.out.println("Started training...");
        convolutionNetwork.learn(trainingDataSet);
        System.out.println("Done training!");

        CrossValidation crossValidation = new CrossValidation(convolutionNetwork, crossValidationDataSet, 6);
        crossValidation.run();
        // convolutionNetwork.save("/mnist.nnet");

        convolutionNetwork.setInput(crossValidationDataSet.getRowAt(0).getInput());
        convolutionNetwork.calculate();
        double[] output = convolutionNetwork.getOutput();
        System.out.println("Output : " + Arrays.toString(output));

        return convolutionNetwork.getLayers();
    }

    public boolean trainSimple() {
        double[] dataOne = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        double[] dataTwo = new double[]{0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0};

        Layer2D.Dimensions dimensions = new Layer2D.Dimensions(5, 5);

        Kernel kernel = new Kernel(3, 3);

        ConvolutionalNetwork convolutionalNetwork = new ConvolutionalNetwork.Builder(dimensions, 1)
                .withConvolutionLayer(kernel, 2)
                .withFullConnectedLayer(2)
                .createNetwork();

        convolutionalNetwork.setLearningRule(new MomentumBackpropagation());
        DataSet dataSet = new DataSet(25, 2);
        dataSet.addRow(
                dataOne,
                new double[]{1, 0});
        dataSet.addRow(
                dataTwo,
                new double[]{0, 1});
        convolutionalNetwork.getLearningRule().setLearningRate(0.00001);
        convolutionalNetwork.learn(dataSet);

        Layer2D featureMap1 = ((FeatureMapsLayer) convolutionalNetwork.getLayerAt(1)).getFeatureMap(0);
        Layer2D featureMap2 = ((FeatureMapsLayer) convolutionalNetwork.getLayerAt(1)).getFeatureMap(1);
        System.out.println("Feature map one : " + featureMap1);
        System.out.println("Feature map two : " + featureMap2);

        convolutionalNetwork.setInput(dataOne);
        convolutionalNetwork.calculate();
        double[] output = convolutionalNetwork.getOutput();
        System.out.println(Arrays.toString(output));

        return Boolean.TRUE;
    }

}
