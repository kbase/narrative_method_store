#!/bin/bash
docker rm $(docker ps -a | grep genome_feature_comparator | awk '{print $1}')
docker rmi $(docker images | grep genome_feature_comparator | awk '{print $3}')
docker rm $(docker ps -a | grep $(docker images -q --filter "dangling=true") | awk '{print $1}')
docker rmi $(docker images -q --filter "dangling=true")
docker build -rm -t kbase/genome_feature_comparator_temp:1433804926692 .