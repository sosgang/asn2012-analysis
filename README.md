# Predicting the Results of Evaluation Procedures of Academics

This GitHub repository contains the code of the analyses performed on the 2012 session of the ASN.

## Running the code

1. Clone the repository and move to the main directory
```
~$ git clone https://github.com/fpoggi/asnanalysis.git
~$ cd asnanalysis/
```
2. (Optional) Compile the maven project and package the JAR files
```
~$ mvn clean compile package
```
3. Run the analyses described in Section "Analysis of the Recruitment Fields and Areas"
```
~$ java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/dataset-ASN-2012.arff -o data/output/RFs_Level1.csv -op RF -l 1
~$ java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/dataset-ASN-2012.arff -o data/output/RFs_Level2.csv -op RF -l 2
~$ java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/dataset-ASN-2012.arff -o data/output/Areas_Level1.csv -op Area -l 1
~$ java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/dataset-ASN-2012.arff -o data/output/Areas_Level2.csv -op Area -l 2
```
4. Run the analyses described in Section "Analysis of the Quantitative Indicators of Applicants"
```
~$ java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/data/input/dataset-ASN-2012.arff -o output/FeatureSel_Level1.csv -op FeatureSel -l 1
~$ java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/data/input/dataset-ASN-2012.arff -o output/FeatureSel_Level2.csv -op FeatureSel -l 2
```
5. Run the experiments described in Section "Evaluation"
```
~$ java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/experiment1 -o data/output/Experiment1.csv -op Experiment1
~$ java -jar target/asn-analysis-1.0-jar-with-dependencies.jar -i data/input/experiment2 -o data/output/Experiment2.csv -op Experiment2
```
## Help
The developed Java application accepts the following options:
```
-i <FILE-OR-FOLDER>
      specify the file or folder to use as input 
-o <FILE>
      specify the file where the output of the analyses has to be stored 
-op <OPERATION>
      specify the operation to perform. Allowed <OPERATION> values are:
      * RF (analysis of the Recruitment Fields)
      * Area (analysis of the scientific Areas)
      * FeatureSel (analysis of the Recruitment Fields using the top 15 indicators)
      * Experiment1 (experiment #1)
      * Experiment2 (experiment #2)
-l <LEVEL>
      specify the academic level of interest (i.e. '1' for Full Professor, '2' for Associate Professor)
```

