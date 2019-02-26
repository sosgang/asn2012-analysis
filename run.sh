#!/bin/bash
java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/dataset-ASN-2012.arff -o data/output/RFs-Level1.csv -op RF -l 1
java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/dataset-ASN-2012.arff -o data/output/RFs-Level2.csv -op RF -l 2
java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/dataset-ASN-2012.arff -o data/output/Areas-Level1.csv -op Area -l 1
java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/dataset-ASN-2012.arff -o data/output/Areas-Level2.csv -op Area -l 2
java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/dataset-ASN-2012.arff -o data/output/FeatureSel-Level1.csv -op FeatureSel -l 1
java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/dataset-ASN-2012.arff -o data/output/FeatureSel-Level2.csv -op FeatureSel -l 2
java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/experiment1/ -o data/output/Experiment1.csv -op Experiment1
java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/experiment2/ -o data/output/Experiment2.csv -op Experiment2
