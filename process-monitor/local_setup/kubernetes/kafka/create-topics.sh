#!/bin/bash

for var in "$@"
do
    kafka-topics --bootstrap-server bpm-kafka:9092 --create --topic $var --partitions 2
done