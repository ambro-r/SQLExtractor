package sqlextractor.util;

import java.util.Comparator;

import sqlextractor.objects.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableComparator implements Comparator<Table> {

  private final Logger LOG = LoggerFactory.getLogger(TableComparator.class);

  @Override
  public int compare(Table table1, Table table2) {
    for(String constraint : table1.getConstraints()){
      if(constraint.equalsIgnoreCase(table2.getTableName())) {
        return 1;
      }
    }
    return -1;
  }

}
