#!/bin/bash

nodes=(20 40 70 100)
speeds=(5 10 15 20)
packets_per_second=(100 200 300 400)

for n in "${nodes[@]}"; do
  echo "Running with NumberOfNodes=$n"
  ./ns3 run "scratch/manet-routing-compare --NumberOfNodes=$n --NumberOfPacketsPerSecond=300 --SpeedOfNodes=15"
done

for s in "${speeds[@]}"; do
  echo "Running with SpeedOfNodes=$s"
  ./ns3 run "scratch/manet-routing-compare --SpeedOfNodes=$s --NumberOfPacketsPerSecond=300 --NumberOfNodes=70"
done

for pps in "${packets_per_second[@]}"; do
  echo "Running with NumberOfPacketsPerSecond=$pps"
  ./ns3 run "scratch/manet-routing-compare --NumberOfPacketsPerSecond=$pps --SpeedOfNodes=15 --NumberOfNodes=40"
done
