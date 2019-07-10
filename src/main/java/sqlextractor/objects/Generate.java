package sqlextractor.objects;

public class Generate {

    private String type;
    private String directory;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return new StringBuilder("Generate [")
                .append("type=").append(type)
                .append(", directory=").append(directory)
                .append("]").toString();
    }

}
