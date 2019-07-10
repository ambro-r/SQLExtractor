package sqlextractor.generators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSQLGenerator {

  private final Logger LOG = LoggerFactory.getLogger(UserSQLGenerator.class);

  protected UserSQLGenerator() {}

  protected String getCell(Row row, int position) {
    String cellValue = "";
    try {
      Cell cell = row.getCell(position);
      if(cell != null) {
        cellValue = cell.toString();
      }
    } catch (NullPointerException e) {
      if(LOG.isTraceEnabled()) {
        LOG.warn("Exception occurred on getting cell string at position={}", position, e);
      }
    }
    return cellValue;
  }

  protected File getOutputFile(String outputDirectory, String system, String sheetName) throws IOException {
    StringBuilder fileName = new StringBuilder();
    fileName.append(system).append("_").append(sheetName.toUpperCase()).append("_USERS.sql");
    File file = new File(outputDirectory, fileName.toString());
    if (file.exists()) {
      file.delete();
    }
    if(!file.createNewFile()) {
      throw new IOException("Unable to create file with fileName=" + fileName);
    }
    return file;
  }

  protected void writeToFile(File outputFile, String toOutput) {
    try {
      FileWriter fileWriter = new FileWriter(outputFile, Boolean.TRUE);
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write(toOutput + "\n");
      bufferedWriter.close();
    } catch (IOException e) {
      if(LOG.isDebugEnabled()) {
        LOG.error("Exception occurred on outputting to file={}:", outputFile.getName(), e);
      } else {
        LOG.error("Exception occurred on outputting to file={}: {}", outputFile.getName(), e.getMessage());
      }
    }
  }

}
