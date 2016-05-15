package discover.analyze.train;

import org.neuroph.core.Layer;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TODO: JavaDoc
 *
 * @author Michael Couck
 * @version 01.00
 * @since 31-01-2016
 */
public class NeurophMultiLayerPerceptronTrain implements ITrain<Layer[]> {

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
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private DataSet crossValidationDataSet;

    NeurophMultiLayerPerceptronTrain(final DataSet trainingDataSet, final DataSet crossValidationDataSet) {
        this.trainingDataSet = trainingDataSet;
        this.crossValidationDataSet = crossValidationDataSet;
    }

    @Override
    public Layer[] train() throws IOException {
        MultiLayerPerceptron multiLayerPerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, 1024, 10, 10);

        MomentumBackpropagation backPropagation = new MomentumBackpropagation();
        backPropagation.setMomentum(0.1);
        backPropagation.setLearningRate(0.01);
        backPropagation.setMaxError(0.1);
        backPropagation.setMaxIterations(10);
        backPropagation.addListener(new LearningListener());
        backPropagation.setErrorFunction(new MeanSquaredError());

        multiLayerPerceptron.setLearningRule(backPropagation);

        logger.info("Started training...");
        multiLayerPerceptron.learn(trainingDataSet);
        logger.info("Done training!");

        // Error here, does this really work? Seems we can't add the evaluators
        // and there is a null pointer because the hard coded evaluator is not in the map,
        // weird...
        // CrossValidation crossValidation = new CrossValidation(multiLayerPerceptron, crossValidationDataSet, 5);
        // crossValidation.run();

        return multiLayerPerceptron.getLayers();
    }

}