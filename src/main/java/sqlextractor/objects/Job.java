package sqlextractor.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class Job {

  private String catalog;
  private String name;
  private boolean enabled;
  private Connection connection;
  private List<Dump> dumps = new ArrayList<Dump>();
  private List<Generate> generates;
  List<FileOutput> fileOutputs = new ArrayList<>();
  List<DatabaseOutput> databaseOutputs = new ArrayList<>();

  public Job() {}

  public String getCatalog() {
    return catalog;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public Connection getConnection() {
    return connection;
  }

  @XmlElement(name = "connection", required = true)
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public List<Dump> getDumps() {
    return dumps;
  }

  @XmlElementWrapper(name="datadumps")
  @XmlElement(name = "dump")
  public void setDumps(List<Dump> dumps) {
    this.dumps = dumps;
  }

  public List<Generate> getGenerates() {
    return generates;
  }

  public void setGenerates(List<Generate> generates) {
    this.generates = generates;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @XmlAttribute(name = "enabled", required=true)
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getName() {
    return name;
  }

  @XmlAttribute(name = "name")
  public void setName(String name) {
    this.name = name;
  }

  public List<FileOutput> getFileOutputs() {
    return fileOutputs;
  }

  @XmlElementWrapper(name = "fileoutputs")
  @XmlElement(name = "fileouput")
  public void setFileOutputs(List<FileOutput> fileOutputs) {
    this.fileOutputs = fileOutputs;
  }

  public List<DatabaseOutput> getDatabaseOutputs() {
    return databaseOutputs;
  }

  @XmlElementWrapper( name = "databaseoutputs")
  @XmlElement(name = "databaseoutput")
  public void setDatabaseOutputs(List<DatabaseOutput> databaseOutputs) {
    this.databaseOutputs = databaseOutputs;
  }

  public List<Fabricate> getFabrications(String table) {
    List<Fabricate> fabricateList = new ArrayList<Fabricate>();
    Iterator<Dump> dumpIterator = dumps.iterator();
    while (dumpIterator.hasNext()) {
      Dump dump = dumpIterator.next();
      if(dump.getTable().equalsIgnoreCase(table)) {
        fabricateList = dump.getFabricateList();
      }
    }
    return fabricateList;
  }

  @Override
  public String toString() {
    return new StringBuilder("Job [")
            .append("catalog=").append(catalog)
            .append(", enabled=").append(enabled)
            .append(", connection=").append(connection)
            .append(", dumps=").append(dumps)
            .append(", generates=").append(generates)
            .append("]").toString();
  }


}
