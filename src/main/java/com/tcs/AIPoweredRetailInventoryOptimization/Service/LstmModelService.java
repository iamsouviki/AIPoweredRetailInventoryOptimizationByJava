package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.SaleHistory;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.SaleHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LstmModelService
 *
 * Simple DL4J LSTM training and inference service.
 *
 * Notes:
 * - This implementation expects you to aggregate sales into a daily time series per product (sales per day).
 * - For production, add normalization, windowing, scaling, validation, and model versioning.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LstmModelService {

    private final SaleHistoryRepository saleHistoryRepository;

    // directory to store model files
    private final File modelsDir = new File("models");
    {
        if (!modelsDir.exists()) modelsDir.mkdirs();
    }

    /**
     * Train an LSTM model for a product using historical sales aggregated by day.
     * This is a simplified example. It creates sequences of length 'windowSize' to predict the next day.
     */
    public void trainModelForProduct(String productId, int windowSize, int epochs) throws Exception {
        // 1) Load sales and aggregate by day (simple)
        List<SaleHistory> sales = saleHistoryRepository.findByProductId(productId);
        if (sales == null || sales.isEmpty()) {
            log.warn("No sales found for product {}", productId);
            return;
        }

        // aggregate into date->quantity map (day granularity using epoch day)
        Map<Long, Integer> daily = new TreeMap<>();
        for (SaleHistory s : sales) {
            long day = s.getSaleDate().getEpochSecond() / 86400L;
            daily.put(day, daily.getOrDefault(day, 0) + s.getQuantity());
        }

        // build sorted list of values
        List<Integer> values = daily.values().stream().collect(Collectors.toList());
        if (values.size() <= windowSize) {
            log.warn("Not enough data points for product {}: {} points", productId, values.size());
            return;
        }

        // create sequences and labels
        int nSamples = values.size() - windowSize;
        INDArray features = Nd4j.create(new int[]{nSamples, 1, windowSize}, 'f');
        INDArray labels = Nd4j.create(new int[]{nSamples, 1, 1}, 'f');

        for (int i = 0; i < nSamples; i++) {
            double[] seq = new double[windowSize];
            for (int j = 0; j < windowSize; j++) {
                seq[j] = values.get(i + j);
            }
            INDArray seqArr = Nd4j.create(seq);
            features.getRow(i).putScalar(0, 0); // placeholder; we'll set via put
            // set sequence
            for (int t = 0; t < windowSize; t++) {
                features.putScalar(new int[]{i, 0, t}, seq[t]);
            }
            // label is next value
            labels.putScalar(new int[]{i, 0, 0}, values.get(i + windowSize));
        }

        // build network config
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.01))
                .weightInit(org.deeplearning4j.nn.weights.WeightInit.XAVIER)
                .list()
                .layer(0, new LSTM.Builder().nIn(1).nOut(50)
                        .activation(Activation.TANH).build())
                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY).nIn(50).nOut(1).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(20));

        // create DataSet and train
        DataSet ds = new DataSet(features, labels);

        for (int e = 0; e < epochs; e++) {
            net.fit(ds);
            log.info("Epoch {} complete for product {}", e+1, productId);
        }

        // save model
        File modelFile = new File(modelsDir, productId + "_lstm.zip");
        org.deeplearning4j.util.ModelSerializer.writeModel(net, modelFile, true);
        log.info("Saved model to {}", modelFile.getAbsolutePath());
    }

    /**
     * Predict next N days for a product using the saved model.
     */
    public double[] predictNextNDays(String productId, int windowSize, int daysAhead) throws Exception {
        File modelFile = new File(modelsDir, productId + "_lstm.zip");
        if (!modelFile.exists()) {
            throw new IllegalStateException("Model not found for product " + productId);
        }
        MultiLayerNetwork net = org.deeplearning4j.util.ModelSerializer.restoreMultiLayerNetwork(modelFile);

        // recreate latest window from data
        List<SaleHistory> sales = saleHistoryRepository.findByProductId(productId);
        Map<Long, Integer> daily = new TreeMap<>();
        for (SaleHistory s : sales) {
            long day = s.getSaleDate().getEpochSecond() / 86400L;
            daily.put(day, daily.getOrDefault(day, 0) + s.getQuantity());
        }
        List<Integer> values = new ArrayList<>(daily.values());
        if (values.size() < windowSize) throw new IllegalStateException("Insufficient data");

        double[] preds = new double[daysAhead];
        // sliding window predict iteratively
        LinkedList<Double> window = new LinkedList<>();
        int start = values.size() - windowSize;
        for (int i = start; i < values.size(); i++) window.add((double) values.get(i));

        for (int d = 0; d < daysAhead; d++) {
            INDArray input = Nd4j.create(new int[]{1,1,windowSize}, 'f');
            for (int t = 0; t < windowSize; t++) input.putScalar(new int[]{0,0,t}, window.get(t));
            INDArray out = net.rnnTimeStep(input);
            double pred = out.getDouble(0);
            preds[d] = pred;
            // slide window
            window.removeFirst();
            window.add(pred);
        }
        return preds;
    }
}
