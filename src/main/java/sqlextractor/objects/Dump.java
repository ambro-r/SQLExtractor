package sqlextractor.objects;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Dump {

  private String table;
  private List<Fabricate> fabricateList;

  public Dump() {}

  public String getTable() {
    return table;
  }

  @XmlAttribute(name = "table")
  public void setTable(String table) {
    this.table = table;
  }

  public List<Fabricate> getFabricateList() {
    return fabricateList;
  }

  @XmlElement(name = "fabricate")
  public void setFabricateList(List<Fabricate> fabricateList) {
    this.fabricateList = fabricateList;
  }

  @Override
  public String toString() {
    return new StringBuilder("Dump [")
            .append("table=").append(table)
            .append(", fabricateList=").append(fabricateList)
            .append("]").toString();
  }

  public boolean equals(Dump other) {
    if(other.getTable().equalsIgnoreCase(table)) {
       return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }

  public boolean isValid() {
    if(table != null && !table.equals("")) {
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }

}
