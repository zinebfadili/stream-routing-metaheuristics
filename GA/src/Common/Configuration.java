package Common;


import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class Configuration
{
    private String[] deviceNames;
    private double[][] linkMatrix;
    private ArrayList<Link> linkList;

    public Link findLink(Integer src_id, Integer dest_id)
    {
        String src = deviceNames[src_id];
        String dest = deviceNames[dest_id];
        for (Link link: linkList) {
            if(link.getSrc().equals(src) && link.getDest().equals(dest))
            return link;
        }
        //System.out.println("issue with findLink");
        return null;
    }
    public Link findLinkBySrcAndDest(String src, String dest)
    {
        for (Link link: linkList) {
            if(link.getSrc().equals(src) && link.getDest().equals(dest))
                return link;
        }
        //System.out.println("issue with findLink");
        return null;
    }
    public Stream getStream(String streamId)
    {
        for (Stream s: streamList) {
            if(s.getId().equals(streamId))
                return s;
        }
        //System.out.println("issue with getStream");
        return null;
    }
    public void setRoutesForStream(String streamId, List<List<Integer>> routes)
    {
        Stream stream = getStream(streamId);
        streamList.remove(stream);
        List<ArrayList<Link>> final_routes = new ArrayList<>();
        for (List<Integer> r:
             routes) {
            ArrayList<Link> r_l = new ArrayList<>();
            for (int i = 0; i < r.size()-1; i++) {
                r_l.add(findLink(r.get(i),r.get(i+1)));
            }
            final_routes.add(r_l);
        }
        stream.setRoutes(final_routes);
        streamList.add(stream);
    }
    public void setRoutesForStream_V2(String streamId, List<ArrayList<Link>> routes){
        Stream stream = getStream(streamId);
        stream.setRoutes(routes);
    }
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

    public Configuration(String[] deviceNames, double[][] linkMatrix, ArrayList<Link> linkList, ArrayList<Stream> streams) {
        this.deviceNames = deviceNames;
        this.linkMatrix = linkMatrix;
        this.linkList = linkList;
        this.streamList = streams;
    }
    public double calculateAvgWCD()
    {
        double avg_WCD = 0;
        for (Stream s:
             streamList) {
            avg_WCD+=s.getStreamWCD();
        }
        return avg_WCD/streamList.size();
    }
    //Extension: Calculating the stream WCDs for Cyclic queuing and forwarding
    public void calculateWCD()
    {
        //wcd(fi) = (h + 1) · C  where h = no. of hops(or switches according to the book) and C = cycle time
        //Each node k in the network has a number of egress ports (one for each connection to another node).
        // Let the set of all egress ports in all nodes be denoted by P.
        // An egress port p ∈ P is connected to a link p.l, which has a certain speed p.l.speed.
        // Let the set of streams passing through a port p be denoted Fp.

        HashMap<String,Double> sums_Fp = new HashMap<String, Double>();
        for (Stream s: streamList) {
            double size = s.getSize();
            ArrayList<String> linksFound = new ArrayList<String>();
            for(ArrayList<Link> route: s.getRoutes())
                for (Link link : route) {
                    boolean skip = false;
                    if(!linksFound.contains(link.getName()))
                    {
                        linksFound.add(link.getName());
                    }
                    else {
                        //System.out.println("Should skip here...");
                        //skip = true;
                        continue;
                    }
                    double val = size/link.getSpeed();
                    if(sums_Fp.containsKey(link.getName()))
                    {
                        val += sums_Fp.get(link.getName());
                    }
                    sums_Fp.put(link.getName(),val);
                    //if(skip)
                    //    System.out.println("!!!!Should have skipped!!!");
                }
        }
        double cycle_time = 0;

        for (String key: sums_Fp.keySet()) {
            //System.out.println("Port "+key + " value "+ sums_Fp.get(key));
            if(sums_Fp.get(key)>cycle_time)
            {
                cycle_time = sums_Fp.get(key);
            }
        }
        //System.out.println("Cycle: "+cycle_time);
        for (Stream stream: streamList)
        {

            double wcd = 0;
            int i = 0;
            for (ArrayList<Link> route : stream.getRoutes()) {
                wcd = (route.size()) * cycle_time;
                stream.addWCDValue(i,wcd);
                i++;
                if(wcd>stream.getDeadline())
                {
                    System.out.println("BAD RESULT: WCD " + wcd + " is bigger than deadline: "+stream.getDeadline());
                }
            }
            //the cycle time must be smaller than the period of the smallest stream
            //if(stream.getPeriod()<=cycle_time)
                //System.out.println("Period " + stream.getPeriod() +" smaller than cycle time for " + stream.getId());
        }
    }
}