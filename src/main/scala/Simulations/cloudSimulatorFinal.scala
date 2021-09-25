package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.cloudSimulatorFinal.{config, logger}
import org.cloudbus.cloudsim.brokers.{DatacenterBrokerFirstFit, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudbus.cloudsim.allocationpolicies.*
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.network.switches.AggregateSwitch
import org.cloudbus.cloudsim.network.switches.EdgeSwitch
import org.cloudbus.cloudsim.network.switches.RootSwitch
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.vms.VmCost

import java.util
import collection.JavaConverters.*

class cloudSimulatorFinal

object cloudSimulatorFinal:
  val config = ObtainConfigReference("cloudSimulator") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  val logger = CreateLogger(classOf[cloudSimulatorFinal])

  def Start() =
    val totalCost1 = 0
    //Creates the cloud simulation
    val cloudsim = new CloudSim();
    //Creates a broker, who implements the policies for selecting a VM to run a cloudlet and a Datacenter to run the submitted VMs
    val broker0 = new DatacenterBrokerFirstFit(cloudsim);
    //Creates a DataCenterSimple,
    val dc0 = createDatacenter(cloudsim)

    val vmList = createVms

    val cloudletList = createCloudlets
    logger.info(s"Created a list of cloudlets: $cloudletList")

    broker0.submitVmList(vmList);
    broker0.submitCloudletList(cloudletList);

    logger.info("Starting cloud simulation...")
    cloudsim.start();

    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
    printTotalVmsCost(broker0, dc0, totalCost1)
    val totalCost = cost(totalCost1)

  /*
Helper Functions:
----------------------
createHost: A function to create a list of the PE's (CPU Cores) for each host (Machine) and adds them
*/
  private def createHost: Host = {
    val peList = List.fill(config.getInt("finalCloudSim.host.PEs"))(new PeSimple(config.getInt("finalCloudSim.host.mipsCapacity")))

    return new HostSimple(config.getInt("finalCloudSim.host.RAMInMBs"), config.getInt("finalCloudSim.host.BandwidthInMBps"), config.getInt("finalCloudSim.host.StorageInMBs"), peList.asJava)
}

  /*
  createDatacenter: A function to create a NetworkDataCenter with a fixed amount of hosts (Machines) which
  uses VMAllocationPolicyFirstFit to allocate the VMs to the first host which has the suitable resources
  */
    def createDatacenter(cloudsim : CloudSim) : DatacenterSimple = {
      val hostList = List.fill(config.getInt("finalCloudSim.host.hosts"))(createHost)
      logger.info(s"Created a list of Hosts: $hostList")

      val dc = new DatacenterSimple(cloudsim, hostList.asJava, new VmAllocationPolicySimple)
      dc.setSchedulingInterval(config.getInt("finalCloudSim.scheduling.SchedulingInterval"))
      // Those are monetary values. Consider any currency you want (such as Dollar)
      dc.getCharacteristics.setCostPerSecond(config.getDouble("costs.costPerSecond")).setCostPerMem(config.getDouble("costs.costPerMem")).setCostPerStorage(config.getDouble("costs.costPerStorage")).setCostPerBw(config.getDouble("costs.costPerBW"))
      dc
    }
  /*
    createVms: A function to create a list of VMs that will be used in this simulation.
    This function uses VmSimple to set the various resources/usage for each VM created for
    usage.
    ----------
    NOTE: Was not able to use List.fill(n)(type) as it made my output differ by a lot and
    did not give the results I expect when using util.List/While
    */
  private def createVms: util.List[Vm] = {
    val list: util.List[Vm] = new util.ArrayList[Vm](config.getInt("finalCloudSim.vm.VMs"))
    var i = 0
    while ( {
      i < config.getInt("finalCloudSim.vm.VMs")
    }) {
      val vm = new VmSimple(config.getInt("finalCloudSim.vm.mipsCapacity"), config.getInt("finalCloudSim.host.PEs"))
        .setRam(config.getLong("finalCloudSim.vm.RAMInMBs"))
        .setBw(config.getLong("finalCloudSim.vm.BandwidthInMBps"))
        .setSize(config.getLong("finalCloudSim.vm.StorageInMBs"))
      list.add(vm)
      i += 1
    }
    return list
  }

/*
createCloudlets: A function to create the list of cloudlets which use the UtilizaionModelDynamic
which allows the increase of utilization of a related resource throughout the simulation
----
NOTE: Could not use List.fill(size)(type) as it was causing errors in my code
*/
private def createCloudlets: util.List[Cloudlet] = {
  val list: util.List[Cloudlet] = new util.ArrayList[Cloudlet](config.getInt("finalCloudSim.cloudlet.cloudlets"))
  //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
  val utilizationModel = new UtilizationModelDynamic(config.getDouble("finalCloudSim.utilizationRatio"))
  var cloudletId: Long = 0
  var i: Int = 0
  while ( {
    i < config.getInt("finalCloudSim.cloudlet.cloudlets")
  }) {
    val cloudlet = new CloudletSimple(config.getLong("finalCloudSim.cloudlet.size"), config.getInt("finalCloudSim.cloudlet.PEs"), utilizationModel)
    list.add(cloudlet)
    i += 1
  }
  return list
}

/*
printTotalVmsCost: A function that gets the info/resource usage from the broker and calculates the estimated
total cost based on the prices defined within the DataCenterCharacteristics
*/
private def printTotalVmsCost(broker0 : DatacenterBrokerFirstFit, dc0 : DatacenterSimple, totalCost : Int) = {
  var totalCost : Double = 0.0
  var totalNonIdleVms : Double = 0.0
  var processingTotalCost : Double = 0.0
  var memoryTotaCost : Double = 0.0
  var storageTotalCost : Double = 0.0
  var bwTotalCost : Double = 0.0
  //broker0.getVmCreatedList.asScala.foreach(vm => val cost = new VmCost(vm))
  for (vm <- broker0.getVmCreatedList.asScala) {
    val cost = new VmCost(vm)
    processingTotalCost += cost.getProcessingCost
    memoryTotaCost += cost.getMemoryCost
    storageTotalCost += cost.getStorageCost
    bwTotalCost += cost.getBwCost
    totalCost += cost.getTotalCost
    if (vm.getTotalExecutionTime > 0)
      totalNonIdleVms += 1
    logger.info(s"$cost")
  }
  logger.info("------------------------------------------------------")
  logger.info(f"Total cost for " + totalNonIdleVms + " created VMs in DC " + dc0.getId + " :                " + "%1.2f".format(processingTotalCost) + "$         " + memoryTotaCost + "$              " + storageTotalCost + "$         " + bwTotalCost + "$           " + totalCost + "$")
}

def cost(cost : Int) : Int = {
cost
}