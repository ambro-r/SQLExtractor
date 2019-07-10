package sqlextractor.objects;

import javax.xml.bind.annotation.XmlAttribute;

public class Connection {

  public Connection() {}

  private String url;
  private String driverClass;
  private String userName;
  private String password;
  private String quotechar;
  private String schema;

  public String getUrl() {
    return url;
  }

  @XmlAttribute(name = "url")
  public void setUrl(String url) {
    this.url = url;
  }

  public String getDriverClass() {
    return driverClass;
  }

  @XmlAttribute(name = "driverclass")
  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }

  public String getUserName() {
    return userName;
  }

  @XmlAttribute(name = "username")
  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  @XmlAttribute(name = "password")
  public void setPassword(String password) {
    this.password = password;
  }

  public String getQuotechar() {
    return quotechar;
  }

  @XmlAttribute(name = "quotechar")
  public void setQuotechar(String quotechar) {
    this.quotechar = quotechar;
  }

  public String getSchema() {
    return schema;
  }

  @XmlAttribute(name = "schema")
  public void setSchema(String schema) {
    this.schema = schema;
  }

  @Override
  public final String toString() {
    return new StringBuilder("Connection [")
            .append("url=").append(url)
            .append(", schema=").append(schema)
            .append(", driverClass=").append(driverClass)
            .append(", userName=").append(userName)
            .append(", password=").append(password)
            .append("]").toString();
  }

  public boolean isValid() {
    if(url != null && !url.equals("")) {
      if(driverClass != null && !driverClass.equals("")) {
        if(userName != null && !userName.equals("")) {
          if(password != null && !password.equals("")) {
            return Boolean.TRUE;
          }
        }
      }
    }
    return Boolean.FALSE;
  }

}
