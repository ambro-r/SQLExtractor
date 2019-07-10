package sqlextractor.objects;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class Table implements Comparable<Table> {

  private String tableName = "";
  private String createStatement = "";
  private Set<String> constraints = new HashSet<String>();

  private Table () {}

  public Table(String tableName) {
    this.tableName = tableName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getCreateStatement() {
    return createStatement;
  }

  public void setCreateStatement(String createStatement) {
    this.createStatement = createStatement;
    String references = "REFERENCES";
    String constraint = "CONSTRAINT";
    String tmpCreateStatement = createStatement.replace("`", "");
    while(tmpCreateStatement.toUpperCase().contains(constraint)) {
      tmpCreateStatement = tmpCreateStatement.substring(tmpCreateStatement.indexOf(constraint) + constraint.length());
      if (tmpCreateStatement.contains(references)) {
        tmpCreateStatement = tmpCreateStatement.substring(tmpCreateStatement.indexOf(references) + references.length());
        StringTokenizer tokenizer = new StringTokenizer(tmpCreateStatement);
        String table = ((String) tokenizer.nextElement()).toUpperCase();
        constraints.add(table);
      }
    }
  }

  public Set<String> getConstraints() {
    return constraints;
  }

  public void setConstraints(Set<String> constraints) {
    this.constraints = constraints;
  }

  @Override
  public int compareTo(Table o) {
    return tableName.compareTo(o.getTableName());
  }

  @Override
  public String toString() {
    return new StringBuilder("Table [")
            .append("tableName=").append(tableName)
            .append(", createStatement=").append(createStatement)
            .append(", constraints=").append(constraints)
            .append("]").toString();
  }

}
