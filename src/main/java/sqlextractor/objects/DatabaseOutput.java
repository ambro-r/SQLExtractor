package sqlextractor.objects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class DatabaseOutput {

  private String environment;
  private String type;
  private Connection connection;
  private boolean enabled;
  List<Appender> appends = new ArrayList<Appender>();
  List<Appender> prepends = new ArrayList<Appender>();

  public String getEnvironment() {
    return environment;
  }

  @XmlAttribute(name = "environment")
  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getType() {
    return type;
  }

  @XmlAttribute(name = "type", required=true)
  public void setType(String type) {
    this.type = type;
  }

  public Connection getConnection() {
    return connection;
  }

  @XmlElement(name = "connection")
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @XmlAttribute(name = "enabled", required=true)
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public List<Appender> getAppends() {
    return appends;
  }

  @XmlElement(name="append")
  public void setAppends(List<Appender> appends) {
    this.appends = appends;
  }

  public List<Appender> getPrepends() {
    return prepends;
  }

  @XmlElement(name="prepend")
  public void setPrepends(List<Appender> prepends) {
    this.prepends = prepends;
  }
}
