/*
 * Copyright (c) 2009 IITP RAS
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * This is an example script for raodv manet routing protocol.
 *
 * Authors: Pavel Boyko <boyko@iitp.ru>
 */

#include "ns3/raodv-module.h"
#include "ns3/core-module.h"
#include "ns3/internet-module.h"
#include "ns3/mobility-module.h"
#include "ns3/network-module.h"
#include "ns3/ping-helper.h"
#include "ns3/point-to-point-module.h"
#include "ns3/yans-wifi-helper.h"

#include <cmath>
#include <iostream>

using namespace ns3;

/**
 * \defgroup raodv-examples raodv Examples
 * \ingroup raodv
 * \ingroup examples
 */

/**
 * \ingroup raodv-examples
 * \ingroup examples
 * \brief Test script.
 *
 * This script creates 1-dimensional grid topology and then ping last node from the first one:
 *
 * [10.0.0.1] <-- step --> [10.0.0.2] <-- step --> [10.0.0.3] <-- step --> [10.0.0.4]
 *
 * ping 10.0.0.4
 *
 * When 1/3 of simulation time has elapsed, one of the nodes is moved out of
 * range, thereby breaking the topology.  By default, this will result in
 * stopping ping replies reception after sequence number 33. If the step size is reduced
 * to cover the gap, then also the following pings can be received.
 */
class RaodvExample
{
  public:
    RaodvExample();
    /**
     * \brief Configure script parameters
     * \param argc is the command line argument count
     * \param argv is the command line arguments
     * \return true on successful configuration
     */
    bool Configure(int argc, char** argv);
    /// Run simulation
    void Run();
    /**
     * Report results
     * \param os the output stream
     */
    void Report(std::ostream& os);

  private:
    // parameters
    /// Number of nodes
    uint32_t size;
    /// Distance between nodes, meters
    double step;
    /// Simulation time, seconds
    double totalTime;
    /// Write per-device PCAP traces if true
    bool pcap;
    /// Print routes if true
    bool printRoutes;

    // network
    /// nodes used in the example
    NodeContainer nodes;
    /// devices used in the example
    NetDeviceContainer devices;
    /// interfaces used in the example
    Ipv4InterfaceContainer interfaces;

  private:
    /// Create the nodes
    void CreateNodes();
    /// Create the devices
    void CreateDevices();
    /// Create the network
    void InstallInternetStack();
    /// Create the simulation applications
    void InstallApplications();
};

int
main(int argc, char** argv)
{
    RaodvExample test;
    if (!test.Configure(argc, argv))
    {
        NS_FATAL_ERROR("Configuration failed. Aborted.");
    }

    test.Run();
    test.Report(std::cout);
    return 0;
}

//-----------------------------------------------------------------------------
RaodvExample::RaodvExample()
    : size(10),
      step(50),
      totalTime(100),
      pcap(true),
      printRoutes(true)
{
}

bool
RaodvExample::Configure(int argc, char** argv)
{
    // Enable raodv logs by default. Comment this if too noisy
    // LogComponentEnable("RaodvRoutingProtocol", LOG_LEVEL_ALL);

    SeedManager::SetSeed(12345);
    CommandLine cmd(__FILE__);

    cmd.AddValue("pcap", "Write PCAP traces.", pcap);
    cmd.AddValue("printRoutes", "Print routing table dumps.", printRoutes);
    cmd.AddValue("size", "Number of nodes.", size);
    cmd.AddValue("time", "Simulation time, s.", totalTime);
    cmd.AddValue("step", "Grid step, m", step);

    cmd.Parse(argc, argv);
    return true;
}

void
RaodvExample::Run()
{
    //  Config::SetDefault ("ns3::WifiRemoteStationManager::RtsCtsThreshold", UintegerValue (1)); //
    //  enable rts cts all the time.
    CreateNodes();
    CreateDevices();
    InstallInternetStack();
    InstallApplications();

    std::cout << "Starting simulation for " << totalTime << " s ...\n";

    Simulator::Stop(Seconds(totalTime));
    Simulator::Run();
    Simulator::Destroy();
}

void
RaodvExample::Report(std::ostream&)
{
}

void
RaodvExample::CreateNodes()
{
    std::cout << "Creating " << (unsigned)size << " nodes " << step << " m apart.\n";
    nodes.Create(size);
    // Name nodes
    for (uint32_t i = 0; i < size; ++i)
    {
        std::ostringstream os;
        os << "node-" << i;
        Names::Add(os.str(), nodes.Get(i));
    }
    // Create static grid
    MobilityHelper mobility;
    mobility.SetPositionAllocator("ns3::GridPositionAllocator",
                                  "MinX",
                                  DoubleValue(0.0),
                                  "MinY",
                                  DoubleValue(0.0),
                                  "DeltaX",
                                  DoubleValue(step),
                                  "DeltaY",
                                  DoubleValue(0),
                                  "GridWidth",
                                  UintegerValue(size),
                                  "LayoutType",
                                  StringValue("RowFirst"));
    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    mobility.Install(nodes);
}

void
RaodvExample::CreateDevices()
{
    WifiMacHelper wifiMac;
    wifiMac.SetType("ns3::AdhocWifiMac");
    YansWifiPhyHelper wifiPhy;
    YansWifiChannelHelper wifiChannel = YansWifiChannelHelper::Default();
    wifiPhy.SetChannel(wifiChannel.Create());
    WifiHelper wifi;
    wifi.SetRemoteStationManager("ns3::ConstantRateWifiManager",
                                 "DataMode",
                                 StringValue("OfdmRate6Mbps"),
                                 "RtsCtsThreshold",
                                 UintegerValue(0));
    devices = wifi.Install(wifiPhy, wifiMac, nodes);

    if (pcap)
    {
        wifiPhy.EnablePcapAll(std::string("raodv"));
    }
}

void
RaodvExample::InstallInternetStack()
{
    RaodvHelper raodv;
    // you can configure raodv attributes here using raodv.Set(name, value)
    InternetStackHelper stack;
    stack.SetRoutingHelper(raodv); // has effect on the next Install ()
    stack.Install(nodes);
    Ipv4AddressHelper address;
    address.SetBase("10.0.0.0", "255.0.0.0");
    interfaces = address.Assign(devices);

    if (printRoutes)
    {
        Ptr<OutputStreamWrapper> routingStream =
            Create<OutputStreamWrapper>("raodv.routes", std::ios::out);
        Ipv4RoutingHelper::PrintRoutingTableAllAt(Seconds(8), routingStream);
    }
}

void
RaodvExample::InstallApplications()
{
    PingHelper ping(interfaces.GetAddress(size - 1));
    ping.SetAttribute("VerboseMode", EnumValue(Ping::VerboseMode::VERBOSE));

    ApplicationContainer p = ping.Install(nodes.Get(0));
    p.Start(Seconds(0));
    p.Stop(Seconds(totalTime) - Seconds(0.001));

    // move node away
    Ptr<Node> node = nodes.Get(size / 2);
    Ptr<MobilityModel> mob = node->GetObject<MobilityModel>();
    Simulator::Schedule(Seconds(totalTime / 3),
                        &MobilityModel::SetPosition,
                        mob,
                        Vector(1e5, 1e5, 1e5));
}
