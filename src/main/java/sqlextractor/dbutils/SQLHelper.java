package sqlextractor.dbutils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import sqlextractor.exceptions.DB2SQLException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sqlextractor.objects.Job;

public class SQLHelper {

    private final Logger LOG = LoggerFactory.getLogger(SQLHelper.class);

    public SQLHelper () { }

    public String getPrimaryColumn(Connection connection, String schema, String tableName) {
        String primaryColumn = "";
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE (TABLE_SCHEMA = '")
                .append(schema).append("')");
            sql.append(" AND (TABLE_NAME = '").append(tableName)
                .append("') AND (`COLUMN_KEY` = 'PRI') LIMIT 1");
            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            primaryColumn = resultSet.getString(1);
        } catch (SQLException sqle) {
            LOG.error("Exception occurred on trying to get primary column for schema={}, tableName={} : {}", schema, tableName, sqle.getMessage());
        }
        return primaryColumn;
    }

    public long getRowCount(Job job, Connection connection, String tableName) {
        try {
            StringBuffer sql = new StringBuffer();
            String columnName = getPrimaryColumn(connection, job.getConnection().getSchema(), tableName);
            sql.append("SELECT COUNT(");
            if(!StringUtils.isEmpty(columnName)) {
                sql.append(columnName);
            } else {
                sql.append("*");
            }
            sql.append(") FROM ").append(job.getConnection().getSchema()).append(".").append(tableName);
            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (Exception e) {
            LOG.error("Unable to get row count for tableName={} in schema={} : {}", tableName, job.getConnection().getSchema(), e.getMessage());
            return -1;
        }
    }

    public long getRowCount(Job job, String tableName) {
        try {
            return getRowCount(job, getConnection(job), tableName);
        } catch (Exception e) {
            LOG.error("Unable to get row count for tableName={} in schema={} : {}", tableName, job.getConnection().getSchema(), e.getMessage());
            return -1;
        }
    }

    public List<String> getTableList(Job job, Connection connection) {
        LOG.debug("Generating table list for schema={}.", job.getConnection().getSchema());
        Set<String> dbTables = new HashSet<String>();
        try {
            DatabaseMetaData databaseMetaData  = connection.getMetaData();
            try {
                ResultSet resultSet = databaseMetaData.getTables(job.getCatalog(), job.getConnection().getSchema(), null, null);
                if (!resultSet.next()) {
                    LOG.error("Unable to find any tables in schema \"{}\".", job.getConnection().getSchema());
                } else {
                    do {
                        String tableName = resultSet.getString("TABLE_NAME");
                        String tableType = resultSet.getString("TABLE_TYPE");
                        if ("TABLE".equalsIgnoreCase(tableType)) {
                            dbTables.add(tableName);
                        }
                    } while (resultSet.next());
                }
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException ex) {
            LOG.error("Unable to get list of tables in schema={} : {}", job.getConnection().getSchema(), ex.getMessage());
        }
        List<String> tableList = new ArrayList<String>(dbTables);
        Collections.sort(tableList);
        LOG.info("... {} tables found.", tableList.size());
        return tableList;
    }

    public Connection getConnection(Job job) throws DB2SQLException {
        return getConnection(job.getConnection());
    }

    public Connection getConnection(sqlextractor.objects.Connection connection) throws DB2SQLException {
        try {
            LOG.debug("Attempting to create connection to schema={} on URL={}, with username={}", connection.getSchema(), connection.getUrl(), connection.getUserName());
            Class.forName(connection.getDriverClass());
            Properties connectionProperties = new Properties();
            connectionProperties.setProperty("user", connection.getUserName());
            connectionProperties.setProperty("password", connection.getPassword());
            Connection dbConnection = DriverManager.getConnection(connection.getUrl(), connectionProperties);
            LOG.debug("Connection to \"{}\" successful.", connection.getUrl());
            return dbConnection;
        } catch (Exception e) {
            throw new DB2SQLException("Unable to connect to \"" + connection.getSchema() + "\" with URL \"" + connection.getUrl() + "\".");
        }
    }

}
