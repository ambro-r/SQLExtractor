package sqlextractor.workers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sqlextractor.objects.Appender;
import sqlextractor.objects.FileOutput;
import sqlextractor.objects.Job;
import sqlextractor.util.Constants;

public class FileOutputWorker extends OutputWorker {

  private final Logger LOG = LoggerFactory.getLogger(FileOutputWorker.class);

  public FileOutputWorker() {}

  private File getOutputFile(FileOutput fileOutput, String baseDirectory, String schema) throws IOException {
    String outputDirectory;
    if(!StringUtils.isEmpty(fileOutput.getDirectory())) {
      outputDirectory = baseDirectory + fileOutput.getDirectory();
    } else {
      StringBuilder output = new StringBuilder(baseDirectory).append(System.getProperty("file.separator"));
      if(!StringUtils.isEmpty(schema)) {
        output.append(output).append(System.getProperty("file.separator"));
      }
      output.append("output");
      outputDirectory = output.toString();
    }
    outputDirectory = outputDirectory.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));
    StringBuilder fileName = new StringBuilder(20);
    fileName.append(schema.toUpperCase()).append("_");
    fileName.append(fileOutput.getEnvironment().toUpperCase()).append("_");
    fileName.append(fileOutput.getType().toUpperCase()).append(".sql");
    File file = new File(outputDirectory, fileName.toString());
    if (file.exists()) {
      file.delete();
    } else {
      new File(outputDirectory).mkdirs();
      file.createNewFile();
    }
    return file;
  }

  private void dumpAppenders(File outputFile, String baseDirectory, List<Appender> appenders, String type) {
    for(Appender appender : appenders) {
      File appendFile = getFile(baseDirectory, appender.getFile());
      LOG.debug("{} file \"{}\" to \"{}\"", type, appendFile.getName(), outputFile.getName());
      try{
        // Open the file that is the first
        // command line parameter
        FileInputStream fileInputStream = new FileInputStream(appendFile);
        // Get the object of DataInputStream
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(appendFile));
        BufferedReader br = new BufferedReader(new InputStreamReader(dataInputStream));
        String strLine;
        //Read File Line By Line
        FileWriter filestream = new FileWriter(outputFile,Boolean.TRUE);
        BufferedWriter out = new BufferedWriter(filestream);
        out.write("\n\n-- " + type + " File :" + appendFile.getName() + "\n");
        while ((strLine = br.readLine()) != null)   {
          // Write to the new file
          out.write(strLine + "\n");
          //Close the output stream
        }
        out.close();
        //Close the input stream
        dataInputStream.close();
      }catch (Exception e){//Catch exception if any
        System.err.println("Error: " + e.getMessage());
      }
    }
  }

  private void writeData(Job job, File outputFile) {
    writeToFile(outputFile, "\nSET FOREIGN_KEY_CHECKS=0;");
    dumpData(job, null, outputFile);
    writeToFile(outputFile, "SET FOREIGN_KEY_CHECKS=1;");
  }

  private void writeStructure(Job job, File outputFile) {
    writeToFile(outputFile, structure(job));
  }

  private void writeTableList(Job job, File outputFile) {
    writeToFile(outputFile, tableList(job));
  }

  public void work(Job job, String baseDirectory) {
      for(FileOutput fileOutput : job.getFileOutputs()) {
        if (fileOutput.isEnabled()) {
          LOG.info("Output type \"{}\" for \"{}\" enabled. Processing.", fileOutput.getType(), fileOutput.getEnvironment());
          try {
            File outputFile = getOutputFile(fileOutput, baseDirectory, job.getConnection().getSchema());
            if (Constants.OUTPUT_TYPE_DATA.equalsIgnoreCase(fileOutput.getType())) {
              dumpAppenders(outputFile, baseDirectory, fileOutput.getPrepends(), "Prepend");
              writeData(job, outputFile);
              dumpAppenders(outputFile, baseDirectory, fileOutput.getAppends(), "Append");
            } else if (Constants.OUTPUT_TYPE_FULL.equalsIgnoreCase(fileOutput.getType())) {
              dumpAppenders(outputFile, baseDirectory, fileOutput.getPrepends(), "Prepend");
              writeStructure(job, outputFile);
              writeData(job, outputFile);
              dumpAppenders(outputFile, baseDirectory, fileOutput.getAppends(), "Append");
            } else if (Constants.OUTPUT_TYPE_STRUCTURE.equalsIgnoreCase(fileOutput.getType())) {
              writeStructure(job, outputFile);
            } else if (Constants.OUTPUT_TYPE_TABLELIST.equalsIgnoreCase(fileOutput.getType())) {
              writeTableList(job, outputFile);
            } else {
              LOG.warn("Output type \"{}\" not supported.", fileOutput.getType());
            }
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
        } else {
          LOG.info("Output type \"{}\" for \"{}\" not enabled. Skipped.", fileOutput.getType(), fileOutput.getEnvironment());
        }
      }
  }

}
