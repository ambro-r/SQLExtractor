package sqlextractor.dbutils;

import sqlextractor.objects.Dump;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import fabricator.Fabricator;
import sqlextractor.objects.Job;
import sqlextractor.objects.Fabricate;
import sqlextractor.objects.Table;
import sqlextractor.util.TableComparator;

public class DB2SQL {

  private final Logger LOG = LoggerFactory.getLogger(DB2SQL.class);

  private final SQLHelper sqlHelper = new SQLHelper();

  public DB2SQL() { }

  private List<Table> sortTables(Set<Table> tableSet) {
    List<Table> noConstraints = new ArrayList<Table>();
    List<Table> constraints = new ArrayList<Table>();
    for(Table table : tableSet) {
      if(table.getConstraints().isEmpty()) {
        noConstraints.add(table);
      } else {
        constraints.add(table);
      }
    }
    Collections.sort(noConstraints, (a, b) -> a.getTableName().compareToIgnoreCase(b.getTableName()));
    Collections.sort(constraints, (a, b) -> a.getTableName().compareToIgnoreCase(b.getTableName()));
    constraints.sort(new TableComparator());
    List<Table> sortedList = new ArrayList<>(tableSet.size());
    sortedList.addAll(noConstraints);
    sortedList.addAll(constraints);
    return sortedList;
  }

  public Set<String> getTablesToDump(Connection connection, Job job) {
    LOG.info("Generating list of tables to dump for schema \"{}\".", job.getConnection().getSchema());
    Set<String> tablesToDump = new HashSet<String>();
    Iterator<Dump> iterator = job.getDumps().iterator();
    while (iterator.hasNext()) {
      String dumpTable= iterator.next().getTable();
      if (dumpTable.endsWith("*")) {
        String pattern = dumpTable.substring(0, dumpTable.indexOf("*"));
        Iterator<String> dbTableIterator = sqlHelper.getTableList(job, connection).iterator();
        while (dbTableIterator.hasNext()) {
          String dbTable = dbTableIterator.next();
          if (dbTable.startsWith(pattern)) {
            tablesToDump.add(dbTable);
          }
        }
      } else if (dumpTable.startsWith("*")) {
      } else {
        tablesToDump.add(dumpTable);
      }
    }
    LOG.info("{} tables to dump.", tablesToDump.size());
    return tablesToDump;
  }

  public String dumpSchema(Job job) {
    String schema = job.getConnection().getSchema();
    LOG.info("Generating DROP and CREATE statements for schema={}.", schema);
    StringBuilder sqlDump = new StringBuilder();
    if(schema != null && !schema.equalsIgnoreCase("")) {
      sqlDump.append("\n-- SQL to drop and create schema ").append(schema);
      sqlDump.append("\nDROP DATABASE ").append(schema).append(";");
      sqlDump.append("\nCREATE DATABASE ").append(schema).append(";");
      sqlDump.append("\nUSE ").append(schema).append(";");
    }
    return sqlDump.toString();
  }

  public List<String> dumpStructure(Connection connection, Job job) {
    LOG.info("Generating DROP and CREATE statements for TABLES in schema \"{}\".", job.getConnection().getSchema());
    List<String> tableList = sqlHelper.getTableList(job, connection);
    Iterator<String> iterator = tableList.iterator();
    Set<Table> tables = new HashSet<Table>(tableList.size());
    while (iterator.hasNext()) {
      Table table = new Table(iterator.next());
      try {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.executeQuery("SHOW CREATE TABLE " + table.getTableName());
        ResultSet rs = statement.getResultSet();
        while (rs.next()) {
          table.setCreateStatement(rs.getString("Create Table"));
          tables.add(table);
        }
      } catch (SQLException e) { }
    }
    List<Table> sortedList = sortTables(tables);

    List<String> createStatements = new ArrayList<String>(sortedList.size());
    Iterator<Table> tableIterator = sortedList.iterator();
    while(tableIterator.hasNext()) {
      StringBuilder sqlDump = new StringBuilder();
      Table table = tableIterator.next();
      sqlDump.append("\n-- SQL to drop and create table ").append(table.getTableName());
      sqlDump.append("\nDROP TABLE IF EXISTS `" + table.getTableName() + "`;");
      sqlDump.append("\n").append(table.getCreateStatement()).append(";");
      createStatements.add(sqlDump.toString());
    }
    LOG.info("{} CREATE TABLE statements generated for schema.", sortedList.size());
    return createStatements;
  }

  private Object getFabricatedData(List<Fabricate> fabricateList, String columnName) {
    if(fabricateList != null) {
      Iterator<Fabricate> fabricateIterator = fabricateList.iterator();
      while (fabricateIterator.hasNext()) {
        Fabricate fabricate = fabricateIterator.next();
        if (fabricate.getColumn().equalsIgnoreCase(columnName)) {
          if (fabricate.getType().equals("contact")) {
            String pattern = fabricate.getPattern();
            if (pattern.contains("email")) {
              String email = Fabricator.contact().eMail();
              email = email.substring(0, email.indexOf("@")) + "@noreply.fabrication";
              pattern = pattern.replace("email", email);
            }
            if (pattern.contains("firstname")) {
              pattern = pattern.replace("firstname", Fabricator.contact().firstName());
            }
            if (pattern.contains("lastname")) {
              pattern = pattern.replace("lastname", Fabricator.contact().lastName());
            }
            return pattern;
          } else if (fabricate.getType().equals("number")) {
            return Fabricator.alphaNumeric().numerify(fabricate.getPattern());
          } else if (fabricate.getType().equals("text")) {
            String pattern = fabricate.getPattern();
            if (pattern.contains("sentence")) {
              pattern = Fabricator.words().sentence();
            } else if (pattern.contains("paragraph")) {
              pattern = Fabricator.words().paragraph();
            }
            return pattern;
          } else {
            LOG.debug("Fabricate type \"{}\" is not supported.", fabricate.getType());
          }
        }
      }
    }
    return null;
  }

  // TO DO : Need to fix the way this SQL is generated
  public List<String> dumpTableData(Connection connection, Job job, String tableName, long start, long rowLimit) {
    List<String> insertStatements = new ArrayList<String>();
    try {
      String column = sqlHelper.getPrimaryColumn(connection, job.getConnection().getSchema(), tableName);
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT * FROM ").append(job.getConnection().getSchema()).append(".").append(tableName);
      if(!StringUtils.isEmpty(column)) {
        sql.append(" ORDER BY ").append(column);
      }
      sql.append(" LIMIT ").append(start).append(",").append(rowLimit);
      PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
      ResultSet resultSet = preparedStatement.executeQuery();
      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();
      List<Fabricate> fabricateList = job.getFabrications(tableName);
      // Now we can output the actual data
      while (resultSet.next()) {
        StringBuilder insertSQL = new StringBuilder();
        insertSQL.append("INSERT INTO ").append(tableName).append(" VALUES (");
        for (int i=0; i< columnCount; i++) {
          if (i > 0) {
            insertSQL.append(", ");
          }
          Object value = getFabricatedData(fabricateList, resultSet.getMetaData().getColumnName(i+1));
          if(value == null) {
            value = resultSet.getObject(i+1);
          }
          if (value == null) {
            insertSQL.append("NULL");
          } else {
            String outputValue = value.toString();
            outputValue = outputValue.replace("\\", "\\\\");
            outputValue = outputValue.replace("'", "\\\'");
            if("true".equalsIgnoreCase(outputValue) || "false".equalsIgnoreCase(outputValue)) {
              String columnType = metaData.getColumnTypeName(i+1).toUpperCase();
              boolean isBoolean = Boolean.FALSE;
              if(columnType.contains("TINYINT"))  {
                isBoolean = Boolean.TRUE;
              } else if(columnType.contains("BIT") ) {
                isBoolean = Boolean.TRUE;
              }
              if(isBoolean) {
                if("true".equalsIgnoreCase(outputValue)) {
                  insertSQL.append("1");
                } else {
                  insertSQL.append("0");
                }
              }
            } else {
              insertSQL.append("'").append(outputValue).append("'");
            }
          }
        }
        insertSQL.append(");");
        insertStatements.add(insertSQL.toString());
      }
      resultSet.close();
      preparedStatement.close();
    } catch (SQLException e) {
      LOG.error("Unable to dump table {} because: {}", tableName, e);
    }
    return insertStatements;
  }

  public List<String> dumpTableList(Connection connection, Job job) {
    return sqlHelper.getTableList(job, connection);
  }

}
