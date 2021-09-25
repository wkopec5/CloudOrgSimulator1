package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.BasicCloudSimPlusExample.config
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.Vm
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.datacenters.Datacenter

import java.util.Comparator
import java.util
import collection.JavaConverters.*

class cloudSimExample1

object cloudSimExample1:
  //val cloudsim = new CloudSim();

  val config = ObtainConfigReference("cloudSimulator") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  val logger = CreateLogger(classOf[cloudSimExample1])

  def Start() =

    //Creates a CloudSim object to initialize the simulation.
    val cloudsim = new CloudSim()

    //Creates a Datacenter with a list of Hosts.
    //Uses a VmAllocationPolicySimple by default to allocate VMs
    val dc0 = createDatacenter(cloudsim)
    val broker0 = new DatacenterBrokerSimple(cloudsim)

    val vmList = createVms
    val cloudlets = createCloudlets
    broker0.submitVmList(vmList.asJava)
    broker0.submitCloudletList(cloudlets)

    cloudsim.start()

    val finishedCloudlets = broker0.getCloudletFinishedList
    finishedCloudlets.sort(Comparator.comparingLong((cloudlet: Cloudlet) => cloudlet.getVm.getId))
    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build()


private def createHost: Host = {
  val peList = List.fill(config.getInt("testCloud1.host.PEs"))(new PeSimple(config.getDouble("testCloud1.host.mipsCapacity")))
  //List of Host's CPUs (Processing Elements, PEs)
  /*
          Uses ResourceProvisionerSimple by default for RAM and BW provisioning
          and VmSchedulerSpaceShared for VM scheduling.
          */ return new HostSimple(config.getLong("testCloud1.host.RAMInMBs"),
                                   config.getLong("testCloud1.host.BandwidthInMBps"),
                                   config.getLong("testCloud1.host.StorageInMBs"),
                                   peList.asJava,
                                   false)
}

private def createDatacenter(cloudSim: CloudSim) = {
  val hostList = List.fill(config.getInt("testCloud1.host.Hosts"))(createHost)
  new DatacenterSimple(cloudSim, hostList.asJava, new VmAllocationPolicySimple)
}

private def createVms = {
  val vm = new VmSimple(config.getLong("testCloud1.host.mipsCapacity"),
    config.getInt("testCloud1.vm.PES"))
  vm.setRam(config.getLong("testCloud1.vm.RAMInMBs")).setBw(config.getLong("testCloud1.vm.BandwidthInMBps")).setSize(config.getLong("testCloud1.host.StorageInMBs"))
  val list = List.fill(config.getInt("testCloud1.vm.VMS"))(vm)
  list
}

private def createCloudlets = {
  val list = new util.ArrayList[Cloudlet]()
  //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
  val utilizationModel = new UtilizationModelDynamic(0.5)
  var i = 0
  while ( {
    i < config.getLong("testCloud1.cloudlet.CLOUDLETS")
  }) {
    val cloudlet = new CloudletSimple(config.getInt("testCloud1.cloudlet.size"), config.getInt("testCloud1.cloudlet.PEs"), utilizationModel)
    cloudlet.setSizes(1024)
    list.add(cloudlet)

    i += 1
  }
  list
}