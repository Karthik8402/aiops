package com.aiops.platform.anomaly.ai;

import org.tribuo.anomaly.AnomalyFactory;
import org.tribuo.anomaly.libsvm.LibSVMAnomalyModel;
import org.tribuo.anomaly.libsvm.LibSVMAnomalyTrainer;
import org.tribuo.anomaly.libsvm.SVMAnomalyType;
import org.tribuo.common.libsvm.KernelType;
import org.tribuo.common.libsvm.SVMParameters;
import org.tribuo.MutableDataset;
import org.tribuo.impl.ArrayExample;
import org.tribuo.provenance.DataSourceProvenance;
import org.tribuo.provenance.SimpleDataSourceProvenance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Random;

@Primary
@Component
public class TribuoAnomalyDetector implements AnomalyDetector {

    private static final int FEATURE_DIM = 6;

    @Value("${aiops.anomaly.threshold:0.6}")
    private double threshold;

    private LibSVMAnomalyModel model;
    private final Random rng = new Random(42);

    @PostConstruct
    public void init() {
        var params = new SVMParameters<>(new SVMAnomalyType(SVMAnomalyType.SVMMode.ONE_CLASS), KernelType.RBF);
        params.setNu(0.1); // expected fraction of outliers
        params.setGamma(0.1);

        var trainer = new LibSVMAnomalyTrainer(params);

        DataSourceProvenance provenance = new SimpleDataSourceProvenance(
                "synthetic-normal", new AnomalyFactory());
        var dataset = new MutableDataset<>(provenance, new AnomalyFactory());

        String[] featureNames = { "mean", "stddev", "min", "max", "p95", "errorCount" };
        for (int i = 0; i < 500; i++) {
            double[] vals = {
                    50 + rng.nextGaussian() * 10, // mean latency ~50ms
                    5 + rng.nextGaussian() * 2, // stddev
                    30 + rng.nextGaussian() * 5, // min
                    80 + rng.nextGaussian() * 10, // max
                    75 + rng.nextGaussian() * 10, // p95
                    rng.nextInt(3) // 0–2 errors per window
            };
            dataset.add(new ArrayExample<>(AnomalyFactory.EXPECTED_EVENT, featureNames, vals));
        }

        this.model = (LibSVMAnomalyModel) trainer.train(dataset);
    }

    @Override
    public double score(double[] features) {
        if (features == null || features.length != FEATURE_DIM) {
            return 0.0;
        }

        String[] featureNames = { "mean", "stddev", "min", "max", "p95", "errorCount" };
        var example = new ArrayExample<>(AnomalyFactory.UNKNOWN_EVENT, featureNames, features);
        var prediction = model.predict(example);

        var output = prediction.getOutput();
        if (output.getType() == org.tribuo.anomaly.Anomaly.AnomalyType.ANOMALOUS) {
            double rawScore = output.getScore();
            return Math.max(0.7, 1.0 / (1.0 + Math.exp(-rawScore)));
        } else {
            double rawScore = output.getScore();
            return Math.min(0.3, 1.0 / (1.0 + Math.exp(-rawScore)));
        }
    }

    @Override
    public double threshold() {
        return threshold;
    }

    @Override
    public String methodName() {
        return "ONE_CLASS_SVM";
    }
}
