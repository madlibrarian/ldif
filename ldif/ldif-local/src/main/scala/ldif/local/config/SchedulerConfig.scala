package ldif.local.config

import java.util.logging.Logger
import java.io.File
import ldif.util.ValidatingXMLReader
import xml.{Node, XML}
import java.util.Properties

case class SchedulerConfig (importJobsDir : File, integrationJob : File, dataSourcesDir : File, dumpLocationDir : File, properties : Properties)  {}

object SchedulerConfig
{
  private val log = Logger.getLogger(getClass.getName)

  private val schemaLocation = "xsd/SchedulerConfig.xsd"

  def empty = SchedulerConfig(null, null, null, null, new Properties)

  def load = new ValidatingXMLReader(fromFile, schemaLocation)

  def fromFile(configFile : File) = {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    // Read in properties
    val propertiesFile = getFile(xml, "properties", baseDir)
    var properties = new Properties
    if (propertiesFile != null)
      properties = ConfigProperties.loadProperties(propertiesFile)

    val dumpLocationDir = new File(baseDir + "/" + (xml \ "dumpLocation" text))
    if(!dumpLocationDir.exists && !dumpLocationDir.mkdirs)
      log.severe("Could not create local dump directory at: " + dumpLocationDir.getCanonicalPath)

    val importJobsDir = getFile(xml, "importJobs", baseDir)
    val integrationJobDir = getFile(xml, "integrationJob", baseDir)
    val datasourceJobDir = getFile(xml, "dataSources", baseDir)

    SchedulerConfig(
      importJobsDir,
      integrationJobDir,
      datasourceJobDir,
      dumpLocationDir,
      properties
    )
  }

  private def getFile (xml : Node, key : String, baseDir : String = null) : File = {
    val value : String = (xml \ key text)
    var file : File = null
    if (value != ""){
      var tmpFilePath = value
      if (baseDir != null)
        tmpFilePath = baseDir + "/" + tmpFilePath
      val tmpFile = new File(tmpFilePath)
      if (tmpFile.exists) {
        file = tmpFile
      }
      else {
        log.warning("\'"+key+"\' path not found: "+ tmpFile.getCanonicalPath)
      }
    }
    else{
      log.warning("\'"+key+"\' is not defined in the Scheduler config")
    }
    file
  }

}