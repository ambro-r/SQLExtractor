package sqlextractor.workers;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import sqlextractor.exceptions.DB2SQLException;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sqlextractor.dbutils.DB2SQL;
import sqlextractor.dbutils.SQLHelper;
import sqlextractor.objects.Job;

public class OutputWorker extends Worker {

    private final Logger LOG = LoggerFactory.getLogger(OutputWorker.class);

    private final DB2SQL db2SQL = new DB2SQL();
    private final SQLHelper sqlHelper = new SQLHelper();

    public OutputWorker() {
    }

    protected Set<String> getTablesToDump(Connection connection, Job job) {
        return db2SQL.getTablesToDump(connection, job);
    }

    protected List<String> getTableDump(Connection connection, Job job, String tableName, long start, long rowLimit) {
        return db2SQL.dumpTableData(connection, job, tableName, start, rowLimit);
    }

    // TO DO : Use Threading to handle this on a thread per table basis.
    protected void dumpData(Job job, Connection outputConnection, File outputFile) {
        Connection connection = getConnection(job);
        if(connection != null) {
            Iterator<String> tablesToDumpIterator = getTablesToDump(connection, job).iterator();
            while (tablesToDumpIterator.hasNext()) {
                String tableName = tablesToDumpIterator.next();
                long rowCount = sqlHelper.getRowCount(job, connection, tableName);
                String fullTableName = job.getConnection().getSchema() + "." + tableName;
                String progressBarText = "Generating INSERT statements for " + fullTableName;
                ProgressBar progressBar = new ProgressBar(progressBarText, rowCount);
                progressBar.start();
                try {
                    boolean processNext = Boolean.TRUE;
                    long startCount = 0;
                    long rowLimit = 10000;
                    if(rowCount > 0 && rowCount < rowLimit) {
                        rowLimit = rowCount + 1;
                    }
                    List<String> inserts = new ArrayList<>();
                    while (processNext) {
                        progressBar.setExtraMessage("Processing rows " + startCount + " to " + (startCount + rowLimit));
                        List<String> insertStatements = getTableDump(connection, job, tableName, startCount, rowLimit);
                        if (!insertStatements.isEmpty()) {
                            inserts.addAll(insertStatements);
                            startCount += rowLimit;
                            if (insertStatements.size() < rowLimit) {
                                processNext = Boolean.FALSE;
                            }
                        } else {
                            processNext = Boolean.FALSE;
                        }
                        if (outputFile != null && (!processNext || inserts.size() >= (rowLimit * 10))) {
                            writeToFile(outputFile, inserts.stream().map(String::trim).collect(Collectors.joining("\n")));
                            inserts = new ArrayList<>();
                        } else if(outputConnection != null) {
                            executeQueries(outputConnection, inserts);
                            inserts = new ArrayList<>();
                        } else {
                            LOG.warn("No output defined, not sure what to do here so exiting...");
                            processNext = Boolean.FALSE;
                        }
                        progressBar.stepBy(rowLimit);
                    }
                } catch (Exception e) {
                    LOG.error("Exception on dumping data for tableName={} : {}", tableName, e.getMessage());
                    LOG.error("Exception on dumping data for tableName={} : ", tableName, e);
                }
                progressBar.stepTo(progressBar.getMax());
                progressBar.stop();
            }
        }
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException sqlex) { }
        }
    }

    protected Connection getConnection(Job job) {
        return getConnection(job.getConnection());
    }

    protected Connection getConnection(sqlextractor.objects.Connection connection) {
        try {
            return sqlHelper.getConnection(connection);
        } catch (DB2SQLException ex) {
            LOG.error(ex.getMessage());
            return null;
        }
    }

    protected File getFile(String baseDirectory, String fileName) {
        String theFile = fileName;
        if(!StringUtils.isEmpty(baseDirectory)) {
            theFile = baseDirectory + System.getProperty("file.separator")+ fileName;
        }
        theFile = theFile.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));
        File file = new File(theFile);
        return file;
    }

    protected List<String> structure(Job job) {
        List<String> structure = new ArrayList<>();
        Connection connection = getConnection(job);
        if(connection != null) {
            structure = db2SQL.dumpStructure(connection, job);
            structure.add(0, db2SQL.dumpSchema(job));
            try {
                connection.close();
            } catch (SQLException sqlex) { }
        }
        return structure;
    }

    protected List<String> tableList(Job job) {
        List<String> tableList = new ArrayList<>();
        Connection connection = getConnection(job);
        if(connection != null) {
            tableList = db2SQL.dumpTableList(connection, job);
            try {
                connection.close();
            } catch (SQLException sqlex) { }
        }
        return tableList;
    }

}
