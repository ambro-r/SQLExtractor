package sqlextractor.dbutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ExtractFileHelper {

    private final Logger LOG = LoggerFactory.getLogger(ExtractFileHelper.class);

    private String tmpDirectory;

    private ExtractFileHelper() { }

    public ExtractFileHelper(String tmpDirectory) {
        this.tmpDirectory = tmpDirectory + System.getProperty("file.separator") + "dbtools";
    }

    public File getTempFile(String fileName) {
        String tmpFileString = tmpDirectory + System.getProperty("file.separator") + fileName;
        return new File(tmpFileString);
    }

    public boolean initaliseTempDirectory() {
        boolean success = Boolean.FALSE;
        try {
            tearDownTempDirectory();
            File theDir = new File(tmpDirectory);
            theDir.mkdir();
        } catch (Exception e) {
            LOG.error("Exception occurred on initialising temp directory : {}", e);
            success = Boolean.FALSE;
        }
        return success;
    }

    public boolean tearDownTempDirectory() {
        boolean success = Boolean.FALSE;
        try {
            File theDir = new File(tmpDirectory);
            if (theDir.exists()) {
                deleteFolder(theDir);
            }
        } catch (Exception e) {
            LOG.error("Exception occurred on initialising temp directory : {}", e);
            success = Boolean.FALSE;
        }
        return success;
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}
