package Simulations

import Simulations.cloudSimulatorFinal2.{config, logger}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import org.cloudbus.cloudsim.core.CloudSim
import HelperUtils.{CreateLogger, ObtainConfigReference}
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

class BasicCloudSimPlusExampleTestSuite extends AnyFlatSpec with Matchers {
  behavior of "configuration parameters module"

  it should "obtain the utilization ratio" in {
    config.getDouble("finalCloudSim2.utilizationRatio") shouldBe 0.5E0
  }

  it should "obtain the MIPS capacity" in {
    config.getLong("finalCloudSim2.vm.mipsCapacity") shouldBe 10000
  }

  it should "obtain the VMS" in {
    config.getLong("finalCloudSim2.vm.VMs") shouldBe 10
  }
  it should "obtain number of DataCenters" in {
    config.getLong("finalCloudSim2.datacenters.numDataCenters") shouldBe 2
  }
  it should "obtain cloudlet size" in {
    config.getLong("finalCloudSim2.cloudlet.size") shouldBe 10000
  }

}
