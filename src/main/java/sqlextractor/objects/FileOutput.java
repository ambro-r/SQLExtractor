package sqlextractor.objects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class FileOutput {

  private String environment;
  private String type;
  private boolean enabled;
  private String directory;
  List<Appender> appends = new ArrayList<Appender>();
  List<Appender> prepends = new ArrayList<Appender>();

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

  @XmlAttribute(name = "type")
  public void setType(String type) {
    this.type = type;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @XmlAttribute(name = "enabled", required=true)
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getDirectory() {
    return directory;
  }

  @XmlAttribute(name = "directory")
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public boolean equals(FileOutput fileOutput) {
    if (fileOutput.getEnvironment().equalsIgnoreCase(environment)) {
      if (fileOutput.getType().equalsIgnoreCase(type)) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }
}
