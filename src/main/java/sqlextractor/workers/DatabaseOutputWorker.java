package sqlextractor.workers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sqlextractor.objects.Appender;
import sqlextractor.objects.DatabaseOutput;
import sqlextractor.objects.Job;
import sqlextractor.util.Constants;

public class DatabaseOutputWorker extends OutputWorker {

  private final Logger LOG = LoggerFactory.getLogger(DatabaseOutputWorker.class);

  private void dumpAppenders(Connection outputConnection, List<Appender> appenders, String type) throws SQLException{
    // TO DO
  }

  private void writeData(Job job, Connection outputConnection) throws SQLException {
    executeQuery(outputConnection, "SET FOREIGN_KEY_CHECKS=0;");
    dumpData(job, outputConnection, null);
    executeQuery(outputConnection, "SET FOREIGN_KEY_CHECKS=1;");
  }

  private void writeStructure(Job job, Connection outputConnection) throws SQLException {
    executeQueries(outputConnection, structure(job));
   // statement.executeUpdate(structure(job).stream().map(String::trim).collect(Collectors.joining("\n")));
  }

  public final void work(Job job) {
    for(DatabaseOutput databaseOutput : job.getDatabaseOutputs()) {
      if (databaseOutput.isEnabled()) {
        LOG.info("Output type \"{}\" for \"{}\" enabled. Processing.", databaseOutput.getType(), databaseOutput.getEnvironment());
        Connection outputConnection;
        try {
          outputConnection = getConnection(databaseOutput.getConnection());
          if (Constants.OUTPUT_TYPE_DATA.equalsIgnoreCase(databaseOutput.getType())) {
            dumpAppenders(outputConnection, databaseOutput.getPrepends(), "Prepend");
            writeData(job, outputConnection);
            dumpAppenders(outputConnection, databaseOutput.getAppends(), "Append");
          } else if (Constants.OUTPUT_TYPE_FULL.equalsIgnoreCase(databaseOutput.getType())) {
            dumpAppenders(outputConnection, databaseOutput.getPrepends(), "Prepend");
            writeStructure(job, outputConnection);
            writeData(job, outputConnection);
            dumpAppenders(outputConnection, databaseOutput.getAppends(), "Append");
          } else if (Constants.OUTPUT_TYPE_STRUCTURE.equalsIgnoreCase(databaseOutput.getType())) {
            writeStructure(job, outputConnection);
          } else {
            LOG.warn("Output type \"{}\" not supported.", databaseOutput.getType());
          }
        } catch (SQLException sqle) {
          sqle.printStackTrace();
        }
        } else {
          LOG.info("Output type \"{}\" for \"{}\" not enabled. Skipped.", databaseOutput.getType(),
              databaseOutput.getEnvironment());
        }
      }
  }

}
