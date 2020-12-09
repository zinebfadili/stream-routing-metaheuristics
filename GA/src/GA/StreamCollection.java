package GA;

import Common.Link;
import Common.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//The Population
public class StreamCollection {
    List<StreamConfig> streams = new ArrayList<StreamConfig>();
    boolean schedulable = true;
    boolean bandwidthOk;
    double fitness = 0;
    private double bandwidthFitness = 0;
    private double overlapFitness = 0;
    private double lengthFitness = 0;
    private double identicalStreams = 0;
    private double normalizedBWFitness = 0;
    private double cycleTime = 0;
    private int rank;
    private double crowdingDistance;
    private double criticalLinks = 0;
    private double normalizedIStreams = 0;
    private double normalizedLFitness = 0;
    private double normalizedCLFitness = 0;
    private double normalizedCT = 0;
    private double smallestPeriod = Double.MAX_VALUE;


    public double getIdenticalStreams(){
        return identicalStreams;
    }

    public void setNormalizedIStreams(double normalizedIStreams) {
        this.normalizedIStreams = normalizedIStreams;
    }

    double normalizedOLFitness = 0;

    public double getNormalizedIStreams() {
        return normalizedIStreams;
    }


    public double getNormalizedCLFitness() {
        return normalizedCLFitness;
    }

    public void setNormalizedCLFitness(double normalizedCLFitness) {
        this.normalizedCLFitness = normalizedCLFitness;
    }

    public double getNormalizedCT() { return normalizedCT; }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        if(this.crowdingDistance < Double.MAX_VALUE)
            this.crowdingDistance = crowdingDistance;
    }

    public int getRank() {
        return rank;
    }

    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public double getCycleTime() { return cycleTime; }

    public void setNormalizedBWFitness(double normalizedBWFitness) {
        this.normalizedBWFitness = normalizedBWFitness;
    }

    public void setNormalizedOLFitness(double normalizedOLFitness) {
        this.normalizedOLFitness = normalizedOLFitness;
    }

    public void setNormalizedLFitness(double normalizedLFitness) {
        this.normalizedLFitness = normalizedLFitness;
    }

    public double getBandwidthFitness() {
        return bandwidthFitness;
    }

    public double getOverlapFitness() {
        return overlapFitness;
    }

    public double getLengthFitness() {
        return lengthFitness;
    }

    public double getNormalizedBWFitness() {
        return normalizedBWFitness;
    }

    public double getNormalizedOLFitness() {
        return normalizedOLFitness;
    }

    public double getNormalizedLFitness() {
        return normalizedLFitness;
    }

    public boolean satisfiesCTime() { return cycleTime < smallestPeriod; }

    /**
     *
     * @param def the streams in the input xml
     * @param devices map from device name to index in adj matrix
     * @param adjMatrix the adjacency matrix
     * @param endpoints list of indexes of the end devices
     */
    public void generateCollection(List<Stream> def, Map<String, Integer> devices, double[][] adjMatrix, List<Integer> endpoints){

        for(Stream definition : def){
            if(definition.getPeriod() < smallestPeriod)
                smallestPeriod = definition.getPeriod();
            StreamConfig sc = new StreamConfig(definition, devices);
            sc.generateStreams(adjMatrix, endpoints);
            streams.add(sc);
        }

    }

    /**
     * TODO: make fitness multi objective
     * calculate the sum of fitness for each stream, and calculate if
     * bandwidth is exceeded
     * @param adjMatrix
     * @param bandwidthWeight
     * @param overlappingWeight
     * @param lengthWeight
     * @return
     */
    public double fitness(double[][] adjMatrix, double bandwidthWeight, double overlappingWeight, double lengthWeight){
        int dim = adjMatrix.length;
        double fitness = 0;
        overlapFitness = 0;
        lengthFitness = 0;
        criticalLinks = 0;
        identicalStreams = 0;
        int[][] linkCount = new int[dim][dim];
        double[][] cTMatrix = new double[dim][dim];

        int[][] locallinks;


        for(StreamConfig sc: streams){
            locallinks = sc.localLinkCount(dim);
            for(int i = 0; i < dim; i++){
                for( int j = 0; j < dim; j++){
                    if(locallinks[i][j] == 1){
                        linkCount[i][j] += 1;
                    }
                }
            }
            fitness += sc.updateFitness(dim,overlappingWeight,lengthWeight);
            overlapFitness += sc.getOverlapFitness();
            lengthFitness += sc.getLengthFitness();
            criticalLinks += sc.getCriticalLink();
            identicalStreams += sc.getIdenticalStreams();
            cTMatrix = sc.updateCycleTime(cTMatrix, adjMatrix);
        }
        bandwidthOk = checkBw(adjMatrix);
        for(int i = 0; i < dim; i++){
            for( int j = 0; j < dim; j++){
                if(cTMatrix[i][j] > cycleTime){
                    cycleTime = cTMatrix[i][j];
                }
            }
        }

        this.fitness = fitness;
        return fitness;
    }

    public double[][] getUsedBW(double[][] bwMatrix){
        for(StreamConfig stream : streams){
            bwMatrix = stream.usedBandwidth(bwMatrix);
        }
        return bwMatrix;
    }
    public boolean checkBw(double[][] adjMatrix)
    {
        double[][] bwMatrix = new double[adjMatrix.length][adjMatrix.length];
        bwMatrix= getUsedBW(bwMatrix);
        for (int i = 0; i < bwMatrix.length; i++) {
            for (int j = 0; j < bwMatrix.length; j++) {
                if(i!=j && bwMatrix[i][j]>adjMatrix[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
    public List<StreamConfig> getStreams(){
        return streams;
    }

    public double getFitness(){
        fitness = 0;
        if(!Double.isNaN(normalizedOLFitness)){
            fitness += normalizedOLFitness*0.25;
        }
        if(!Double.isNaN(normalizedLFitness)){
            fitness += normalizedLFitness*0.25;
        }
        if(!Double.isNaN(normalizedCLFitness)){
            fitness += normalizedCLFitness*0.25;
        }
        if(!Double.isNaN(normalizedIStreams)){
            fitness += normalizedIStreams*0.25;
        }
        if(!bandwidthOk)
            fitness+=1000;
        return fitness;
    }

    /**
     * calculate if stream collection is using valid links
     * @param adjMatrix
     * @return false if any stream is using an invalid link, true otherwise
     */
    public boolean eval(double[][] adjMatrix){
        for(StreamConfig sc : streams){
            if(!sc.eval(adjMatrix))
                return false;
        }
        return true;
    }

    public void repair(){
        for(StreamConfig stream : streams){
            stream.repair();
            if(stream.getPeriod() < smallestPeriod)
                smallestPeriod = stream.getPeriod();
        }
    }


    public void print(){
        for(StreamConfig sc : streams){
            sc.print();
        }
    }


    public double getCriticalLinkFitness() {
        return criticalLinks;
    }

    public void calculateWCD(double[][] adjMatrix)
    {

        HashMap<String,Double> sums_Fp = new HashMap<String, Double>();
        for (StreamConfig s: streams) {
            double size = s.getSize();
            ArrayList<String> linksFound = new ArrayList<String>();
            for(List<Integer> route: s.getStreams())
                for (int i=0;i<route.size()-1;i++) {
                    boolean skip = false;
                    String linkName = route.get(i)+"_"+route.get(i+1);
                    if(!linksFound.contains(linkName))
                    {
                        linksFound.add(linkName);
                    }
                    else {
                        //System.out.println("Should skip here...");
                        //skip = true;
                        continue;
                    }
                    double val = size/adjMatrix[route.get(i)][route.get(i+1)];
                    if(sums_Fp.containsKey(linkName))
                    {
                        val += sums_Fp.get(linkName);
                    }
                    sums_Fp.put(linkName,val);
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
        for (StreamConfig stream: streams)
        {
            double wcd = 0;
            int i = 0;
            for (List<Integer> route : stream.getStreams()) {
                double wcd_temp = (route.size()-1) * cycle_time;
                if(wcd_temp>wcd)
                    wcd = wcd_temp;
            }
            stream.setWcd(wcd);
            if(wcd>stream.getDeadline())
            {
                schedulable = false;
            }
            //the cycle time must be smaller than the period of the smallest stream
            //if(stream.getPeriod()<=cycle_time)
            //    System.out.println("Period " + stream.getPeriod() +" smaller than cycle time for " + stream.getId());
        }
    }

    public double getSmallestPeriod() {
        return smallestPeriod;
    }

    public double getCost(){
        return lengthFitness + overlapFitness + criticalLinks;
    }
}
