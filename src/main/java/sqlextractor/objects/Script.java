package sqlextractor.objects;

import javax.xml.bind.annotation.XmlAttribute;

public class Script {

  private String location;
  private String version;

  public Script() { }

  public String getLocation() {
    return location;
  }

  @XmlAttribute(name = "location", required = true)
  public void setLocation(String location) {
    this.location = location;
  }

  public String getVersion() {
    return version;
  }

  @XmlAttribute(name = "version", required = true)
  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return new StringBuilder("Script [")
        .append("location=").append(location)
        .append(", version=").append(location)
        .append("]").toString();
  }

}
