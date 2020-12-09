package Common;

import Common.Link;

import java.util.*;


public class Stream {

    private String id; //id
    private String src; //source device
    private String dest; //destination device
    private double size; //size
    private double period; //period
    private double deadline; //deadline
    private int rl; //redundancy level
    private List<ArrayList<Link>> routes; //routes of the stream (a list of size rl of lists of links)
    private double usedBw; //used bandwidth
    private ArrayList<Double> route_WCD;
    private double WCD;

    public Stream(String id, String src, String dest, double size, double period, double deadline, int rl){
        this.id = id;
        this.src = src;
        this.dest = dest;
        this.size = size;
        this.period = period;
        this.deadline = deadline;
        this.rl = rl;
        this.usedBw = this.size/this.period; //in Mbits/sec
        this.routes = new ArrayList<>(this.rl);
        this.route_WCD = new ArrayList<Double>(this.rl);
    }
    public void addWCDValue(int index, double WCD)
    {
        route_WCD.add(index, WCD);
    }
    public Double getWCD(int route_index)
    {
        return route_WCD.get(route_index);
    }
    public double getStreamWCD()
    {
        return route_WCD.stream().max(Double::compare).get();
    }
    public ArrayList<Double> getRoute_WCD() {
        return route_WCD;
    }

    public String getId() {
        return id;
    }

    public String getSrc() {
        return src;
    }

    public String getDest() {
        return dest;
    }

    public double getSize() {
        return size;
    }

    public double getPeriod() {
        return period;
    }

    public double getDeadline() {
        return deadline;
    }

    public int getRl() {
        return rl;
    }

    public List<ArrayList<Link>> getRoutes() {
        return routes;
    }

    public double getUsedBw() {
        return usedBw;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void setPeriod(double period) {
        this.period = period;
    }

    public void setDeadline(double deadline) {
        this.deadline = deadline;
    }

    public void setRl(int rl) {
        this.rl = rl;
    }

    public void setRoutes(List<ArrayList<Link>> routes) {
        this.routes = routes;
    }

    public void setUsedBw(double usedBw) {
        this.usedBw = usedBw;
    }

}
