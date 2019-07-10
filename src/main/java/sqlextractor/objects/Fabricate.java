package sqlextractor.objects;

import javax.xml.bind.annotation.XmlAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fabricate {

  private final Logger LOG = LoggerFactory.getLogger(Fabricate.class);

  private String column;
  private String type;
  private String pattern;

  public Fabricate() {}

  public String getColumn() {
    return column;
  }

  @XmlAttribute(name = "column")
  public void setColumn(String column) {
    this.column = column;
  }

  public String getType() {
    return type;
  }

  @XmlAttribute(name = "type")
  public void setType(String type) {
    this.type = type.toLowerCase();
  }

  public String getPattern() {
    return pattern;
  }

  @XmlAttribute(name = "pattern")
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  @Override
  public String toString() {
    return new StringBuilder("Fabricate [")
            .append("column=").append(column)
            .append(", type=").append(type)
            .append(", pattern=").append(pattern)
            .append("]").toString();
  }

  public boolean isValid() {
    if(column != null && !column.equals("")) {
      if(type != null && !type.equals("")) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

}
