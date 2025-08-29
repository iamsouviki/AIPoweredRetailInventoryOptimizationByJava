package com.ai.retail.service;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;

@Service
public class LstmForecastService {

    private MultiLayerNetwork buildModel(int inputSize, int hiddenSize) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .updater(new Adam(0.01))
                .list()
                .layer(new LSTM.Builder().nIn(inputSize).nOut(hiddenSize)
                        .activation(Activation.TANH).build())
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY).nIn(hiddenSize).nOut(1).build())
                .build();
        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        return net;
    }

    public double[] forecast(double[] sales, int horizon) {
        int window = Math.min(7, sales.length); // simple window
        MultiLayerNetwork net = buildModel(1, 16);

        // Fake minimal training: one pass over sequence windows
        for (int epoch = 0; epoch < 5; epoch++) {
            for (int t = window; t < sales.length; t++) {
                double x = sales[t-1];
                double y = sales[t];
                INDArray in = Nd4j.create(new double[]{x}, new long[]{1,1,1});
                INDArray out = Nd4j.create(new double[]{y}, new long[]{1,1,1});
                net.fit(in, out);
            }
        }

        double last = sales[sales.length-1];
        double[] preds = new double[horizon];
        double prev = last;
        for (int i=0;i<horizon;i++) {
            INDArray in = Nd4j.create(new double[]{prev}, new long[]{1,1,1});
            INDArray p = net.output(in);
            prev = p.getDouble(0);
            preds[i] = prev;
        }
        return preds;
    }
}