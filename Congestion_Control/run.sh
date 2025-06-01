clear
./ns3 configure --enable-examples --enable-tests
ID=2005055
tracing=true
num_flows=$((1+$ID*$ID*$ID%7))
bandwidth=$((1+$ID%3))
transport_prot="TcpNewReno"
transport_prot="TcpScalable"
./ns3 run "scratch/tcp-variants-comparison --tracing=$tracing --num_flows=$num_flows --bandwidth=$bandwidth"Mbps" --transport_prot=$transport_prot"
echo "$transport_prot done"
# transport_prot="TcpIllinois"
# ./ns3 run "scratch/tcp-variants-comparison --tracing=$tracing --num_flows=$num_flows --bandwidth=$bandwidth"Mbps" --transport_prot=$transport_prot"
# echo "$transport_prot done"