package sqlextractor.workers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Worker {

    private final Logger LOG = LoggerFactory.getLogger(Worker.class);

    protected Worker () {}

    protected void executeQuery(Connection outputConnection, String query) throws SQLException {
        Statement statement = outputConnection.createStatement();
        LOG.trace("Executing SQL String : {}", query);
        statement.executeUpdate(query);
    }

    protected void executeQueries(Connection outputConnection, List<String> queries) throws SQLException {
        Statement statement = outputConnection.createStatement();
        Iterator<String> iterator = queries.iterator();
        while (iterator.hasNext()) {
            String sql = iterator.next();
            try {
                statement.executeUpdate(sql);
            } catch (SQLException sqle) {
                throw new SQLException(sqle.getMessage() + " : SQL : " + sql);
            }
        }
    }

    protected void writeToFile(File file, String toOutput) {
        LOG.trace("Outputting to file {}", file.getName());
        try {
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), Boolean.TRUE);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(toOutput + "\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeToFile(File file, List<String> toOutput) {
        LOG.trace("Output to file {}", file.getName());
        try {
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), Boolean.TRUE);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            Iterator<String> iterator = toOutput.iterator();
            while (iterator.hasNext()) {
                bufferedWriter.write(iterator.next() + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<String> listFilesInFolder(String directory) {
        LOG.debug("Getting file list from directory \"{}\"", directory);
        File folder = new File(directory);
        List<String> fileList = new ArrayList<String>();
        for (File fileEntry : folder.listFiles()) {
            fileList.add(fileEntry.getAbsolutePath());
        }
        return fileList;
    }

    protected String readFile(String path) throws IOException {
        return readFile(path, Charset.defaultCharset());
    }

    protected String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
