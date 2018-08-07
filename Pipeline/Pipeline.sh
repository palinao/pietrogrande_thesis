#!/bin/sh

echo 'Building annotation files from .txt'
cd neji-2.0.0
./neji.sh -i ../input-files -if RAW -o ../output-files/a-files -of A1 -m resources/models/models_genes/model_h -noids

cd ../output-files/a-files

#changing extension of files to .ann
for file in *.a1
do
  mv "$file" "${file%.a1}.ann"
done

#cd ../../ann2rdf-master

#echo 'Converting to .rdf file'
#python main.py --input ../output-files/a-files --output ../output-files/graph/a-graph
