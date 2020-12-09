import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Configuration
{
private String[] deviceNames;
private double[][] linkMatrix;
private ArrayList<Link> linkList;

    public ArrayList<Stream> getStreamList() {
        return streamList;
    }

    public void setStreamList(ArrayList<Stream> streamList) {
        this.streamList = streamList;
    }

    private ArrayList<Stream> streamList;

public String[] getDeviceNames() {
    return deviceNames;
}

public void setDeviceNames(String[] deviceNames) {
    this.deviceNames = deviceNames;
}

public double[][] getLinkMatrix() {
    return linkMatrix;
}

public void setLinkMatrix(double[][] linkMatrix) {
    this.linkMatrix = linkMatrix;
}

public ArrayList<Link> getLinkList() {
    return linkList;
}

public void setLinkList(ArrayList<Link> linkList) {
    this.linkList = linkList;
}

public Configuration(String[] deviceNames, double[][] linkMatrix, ArrayList<Link> linkList,ArrayList<Stream> streams) {
    this.deviceNames = deviceNames;
    this.linkMatrix = linkMatrix;
    this.linkList = linkList;
    this.streamList = streams;
}


}