package ikube.discover.analyze.train;

import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.DataSet;

public class Nd4jTain implements ITrain {

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean train() {

        final int numRows = 4;
        final int numColumns = 1;
        int outputNum = 3;
        int numSamples = 150;
        int batchSize = 150;
        int iterations = 1000;
        int splitTrainNum = (int) (batchSize * .8);
        int seed = 123;
        int listenerFreq = iterations/5;

        // DataSetIterator iter = new IrisDataSetIterator(batchSize, numSamples);
        DataSet next = new org.nd4j.linalg.dataset.DataSet();
        next.normalizeZeroMeanZeroUnitVariance();

        SplitTestAndTrain testAndTrain = next.splitTestAndTrain(splitTrainNum);
        DataSet train = testAndTrain.getTrain();
        DataSet test = testAndTrain.getTest();

        //MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        //        .seed(seed) // Locks in weight initialization for tuning
        //        .iterations(iterations) // # training iterations predict/classify & backprop
        //        .learningRate(1e-6f) // Optimization step size
        //        .optimizationAlgo(OptimizationAlgorithm.CONJUGATE_GRADIENT) // Backprop to calculate gradients
        //        .l1(1e-1).regularization(true).l2(2e-4)
        //        .useDropConnect(true)
        //        .list(2) // # NN layers (doesn't count input layer)
        //        .layer(0, new RBM.Builder(RBM.HiddenUnit.RECTIFIED, RBM.VisibleUnit.GAUSSIAN)
        //                        .nIn(numRows * numColumns) // # input nodes
        //                        .nOut(3) // # fully connected hidden layer nodes. Add list if multiple layers.
        //                        .weightInit(WeightInit.XAVIER) // Weight initialization
        //                        .k(1) // # contrastive divergence iterations
        //                        .activation("relu") // Activation function type
        //                        .lossFunction(LossFunctions.LossFunction.RMSE_XENT) // Loss function type
        //                        .updater(Updater.ADAGRAD)
        //                        .dropOut(0.5)
        //                        .build()
        //        ) // NN layer type

        return false;
    }
}
