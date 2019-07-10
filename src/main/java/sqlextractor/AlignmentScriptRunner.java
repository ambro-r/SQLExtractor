package sqlextractor;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import sqlextractor.objects.Connection;
import sqlextractor.objects.Extract;
import sqlextractor.workers.DatabaseOutputWorker;
import sqlextractor.workers.FileOutputWorker;
import sqlextractor.workers.GenerateWorker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sqlextractor.objects.Job;

public class AlignmentScriptRunner {

  private final Logger LOG = LoggerFactory.getLogger(AlignmentScriptRunner.class);

  public AlignmentScriptRunner() {}

  private void extract(String jobfile, String userName, String password, String baseDirectory) {
    try {
      File xmlExtract = new File(jobfile);
      JAXBContext jaxbContext = JAXBContext.newInstance(Extract.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      Extract extract = (Extract) jaxbUnmarshaller.unmarshal(xmlExtract);
      extract.setUserName(userName);
      extract.setPassword(password);
      extract.setBaseDirectory(baseDirectory);
      GenerateWorker generateWorker = new GenerateWorker();
      for (Job job : extract.getJobs()) {
        if (job.isEnabled()) {
          LOG.info("PROCESSING JOB ({}) : {}.", job.getConnection().getSchema(), job.getName());
          job.setConnection(getConnection(extract, job));
          if (job.getFileOutputs().isEmpty() && job.getDatabaseOutputs().isEmpty()) {
            LOG.debug("JOB ({}) : No outputs defined. Skipping.", job.getConnection().getSchema());
          } else {
            FileOutputWorker fileOutputWorker = new FileOutputWorker();
            fileOutputWorker.work(job, extract.getBaseDirectory());
            DatabaseOutputWorker databaseOuputWorker = new DatabaseOutputWorker();
            databaseOuputWorker.work(job);
          }
          if ((job.getGenerates() == null) || job.getGenerates().isEmpty()) {
            LOG.debug("JOB ({}) : No generates defined. Skipping.", job.getConnection().getSchema());
          } else {
            generateWorker.work(job);
          }
          LOG.info("JOB ({}) : Completed.", job.getConnection().getSchema());
        } else {
          LOG.info("SKIPPING JOB ({}) : {}.", job.getConnection().getSchema(), job.getName());
        }
      } // while
    } catch (Exception e) {
      LOG.error("Error has occurred: ", e);
    }
  }

  private Connection getConnection(Extract extract, Job job) {
    Connection connection = job.getConnection();
    if(StringUtils.isEmpty(connection.getUserName())) {
      connection.setUserName(extract.getUserName());
    }
    if(StringUtils.isEmpty(connection.getPassword())) {
      connection.setPassword(extract.getPassword());
    }
    if(StringUtils.isEmpty(connection.getDriverClass())) {
      connection.setDriverClass(extract.getDriverClass());
    }
    if(StringUtils.isEmpty(connection.getQuotechar())) {
      connection.setQuotechar(extract.getQuotechar());
    }
    return connection;
  }

  public static void main(String[] args) {
    if(args.length < 4 || StringUtils.isEmpty(args[0])) {
      System.out.println("Please specify the following program arguments: <job file> <db username> <db password> <base directory>");
    } else {
      AlignmentScriptRunner alignmentScriptRunner = new AlignmentScriptRunner();
      alignmentScriptRunner.extract(args[0], args[1], args[2], args[3]);
    }
  }

}
