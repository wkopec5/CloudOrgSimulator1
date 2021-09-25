import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.BasicCloudSimPlusExample
import Simulations.cloudSimExample1
import Simulations.cloudSimulatorFinal
import Simulations.cloudSimulatorFinal2
import Simulations.cloudSimulatorFinal3
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =

    logger.info("Constructing a cloud model #1...")
    cloudSimulatorFinal.Start()
    logger.info("Finished cloud simulation #1...")

    //logger.info("Constructing a cloud model #2...")
    //cloudSimulatorFinal2.Start()
    //logger.info("Finished cloud simulation #2...")

    //logger.info("Constructing a cloud model...")
    //cloudSimulatorFinal3.Start()
    //logger.info("Finished cloud simulation...")

    //logger.info("Constructing a cloud model...")
    //BasicCloudSimPlusExample.Start()
    //logger.info("Finished cloud simulation...")

class Simulation