package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.BasicCloudSimPlusExample.config
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.network.switches.AggregateSwitch
import org.cloudbus.cloudsim.network.switches.EdgeSwitch
import org.cloudbus.cloudsim.network.switches.RootSwitch
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared

import java.util
import collection.JavaConverters.*

class BasicCloudSimPlusExample

object BasicCloudSimPlusExample:
  val config = ObtainConfigReference("cloudSimulator") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  val logger = CreateLogger(classOf[BasicCloudSimPlusExample])

  def Start() =
    //Creates a CloudSim object to initialize the simulation.
    val cloudsim = new CloudSim();
    /*Creates a Broker that will act on behalf of a cloud user (customer).*/
    val broker0 = new DatacenterBrokerSimple(cloudsim);

    //Creates a list of Hosts, each host with a specific list of CPU cores (PEs).
    //Uses a PeProvisionerSimple by default to provision PEs for VMs
    //val hostPes : List[Pe] = List(new PeSimple(config.getDouble("cloudSimulator.host.mipsCapacity")))
    /*var i: Int = 0
    while ( {
      i < config.getLong("cloudSimulator.host.PEs")
    }) { //Uses a PeProvisionerSimple by default to provision PEs for VMs
      val thisPe =
      hostPes.asJava.add(0, thisPe)
      i += 1
      logger.info(s"i = $i")
      logger.info(s"$hostPes")
    }*/
    //new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity"))
    //logger.info(s"Created one processing element: $hostPes")

    //Uses ResourceProvisionerSimple by default for RAM and BW provisioning
    //Uses VmSchedulerSpaceShared by default for VM scheduling
    //val hostList = List(new HostSimple(config.getLong("cloudSimulator.host.RAMInMBs"),
      //config.getLong("cloudSimulator.host.StorageInMBs"),
      //config.getLong("cloudSimulator.host.BandwidthInMBps"),
      //hostPes.asJava))

    //logger.info(s"Created one host: $hostList")
    //Creates a Datacenter with a list of Hosts.
    //Uses a VmAllocationPolicySimple by default to allocate VMs
    //val dc0 = new DatacenterSimple(cloudsim, hostList.asJava);
    val dc0 = createDatacenter(cloudsim)

    //Creates VMs to run applications.
    /*val vmList : List[Vm] = List(new VmSimple(config.getLong("cloudSimulator.vm.mipsCapacity"), hostPes.length)
      .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
      .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
      .setSize(config.getLong("cloudSimulator.vm.StorageInMBs")))
    var id = 0*/
    /*= List(
      //Uses a CloudletSchedulerTimeShared by default to schedule Cloudlets
      new VmSimple(config.getLong("cloudSimulator.vm.mipsCapacity"), hostPes.length)
        .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
        .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
        .setSize(config.getLong("cloudSimulator.vm.StorageInMBs"))
    )*/
    //logger.info(s"Created one virtual machine: $vmList")
    //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
    val utilizationModel = new UtilizationModelDynamic(config.getDouble("cloudSimulator.utilizationRatio"));
    //Creates Cloudlets that represent applications to be run inside a VM.
    val cloudletList = new CloudletSimple(config.getLong("cloudSimulator.cloudlet.size"), config.getInt("cloudSimulator.cloudlet.PEs"), utilizationModel) ::
      new CloudletSimple(config.getLong("cloudSimulator.cloudlet.size"), config.getInt("cloudSimulator.cloudlet.PEs"), utilizationModel) :: Nil

    logger.info(s"Created a list of cloudlets: $cloudletList")

    //broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);

    logger.info("Starting cloud simulation...")
    cloudsim.start();


    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();

protected def createDatacenter(cloudsim : CloudSim) = {
  val numberOfHosts = EdgeSwitch.PORTS * AggregateSwitch.PORTS * RootSwitch.PORTS
  val peList = new util.ArrayList[Pe]()
  val hostList = new util.ArrayList[Host](numberOfHosts)
  for (i <- 0 until numberOfHosts) {
    peList.add(new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")))
    val host = new NetworkHost(config.getLong("cloudSimulator.host.RAMInMBs"), config.getLong("cloudSimulator.host.BandwidthInMBps"), config.getLong("cloudSimulator.host.StorageInMBs"), peList).setRamProvisioner(new ResourceProvisionerSimple).setBwProvisioner(new ResourceProvisionerSimple).setVmScheduler(new VmSchedulerTimeShared)
    hostList.add(host)
  }
  val dc = new NetworkDatacenter(cloudsim, hostList, new VmAllocationPolicySimple)
  dc.setSchedulingInterval(1)
  dc.getCharacteristics.setCostPerSecond(0.01).setCostPerMem(0.02).setCostPerStorage(0.001).setCostPerBw(0.005)
  dc
}