#!/bin/bash
DIR=$(pwd)
docker rm genome_feature_comparator_1
docker run --name genome_feature_comparator_1 -v $DIR/work:/kb/deployment/services/GenomeFeatureComparator/work kbase/genome_feature_comparator_test:1433804926692
