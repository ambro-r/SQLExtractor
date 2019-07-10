package sqlextractor.objects;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "extracts")
public class Extract {

    private List<Job> jobs;
    private String driverClass = "com.mysql.jdbc.Driver";
    private String baseDirectory;
    private String userName;
    private String password;
    private String quotechar = "`";

    public Extract() {}

    public List<Job> getJobs() {
        return jobs;
    }

    @XmlElement(name = "job")
    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public String getDriverClass() {
        return driverClass;
    }

    @XmlAttribute(name = "driverclass")
    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    @XmlAttribute(name = "baseDirectory")
    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
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
}
