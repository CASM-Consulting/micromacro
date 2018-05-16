#!/bin/bash

java -mx500m -cp stanford-ner-with-classifier.jar edu.stanford.nlp.ie.NERServer -tokenizerFactory edu.stanford.nlp.process.WhitespaceTokenizer -tokenizerOptions "tokenizeNLs=true" -port 9191 -loadClassifier ../ob-place-v0.2.0.ser.gz 
