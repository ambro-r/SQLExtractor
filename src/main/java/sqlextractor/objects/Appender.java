package sqlextractor.objects;

import javax.xml.bind.annotation.XmlAttribute;

public class Appender {

  private String file;

  public final String getFile() {
    return file;
  }

  @XmlAttribute(name = "file")
  public final void setFile(String file) {
    this.file = file;
  }
}
