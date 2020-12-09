package GA;

import Common.Stream;

import java.util.*;

public class StreamConfig {

    private final String id;
    private final int rl;
    private final int src;
    private final int dest;
    private final double size;
    private final double period;
    private final double cost;

    private double lengthFitness = 0;
    private double overlapFitness = 0;
    private double criticalLink = 0;
    private double identicalStreams = 0;
    private double wcd;
    private double deadline;

    Random random ;
    List<List<Integer>> streams = new ArrayList<>();

    /**
     *
     * @param sd
     * @param deviceNameToIdx a map that maps device names to integers
     */
    public StreamConfig(Stream sd, Map<String, Integer> deviceNameToIdx){
        id = sd.getId();
        rl = sd.getRl();
        src = deviceNameToIdx.get(sd.getSrc());
        dest = deviceNameToIdx.get(sd.getDest());
        size = sd.getSize();
        period = sd.getPeriod();
        deadline = sd.getDeadline();
        cost = size/period; //in B/us
        random = GAHelper.random;
    }

    public StreamConfig(StreamConfig streamConfig) {
        id = streamConfig.id;
        rl = streamConfig.rl;
        src = streamConfig.src;
        dest = streamConfig.dest;
        size = streamConfig.size;
        period = streamConfig.period;
        cost = size/period;
        random = GAHelper.random;
    }

    //empty constructor for testing
    public StreamConfig(){
        id = "test";
        rl = 3;
        src = 1;
        dest = 1;
        size = 1;
        period = 100000;
        cost = size/period;
    }

    public double getWcd() {
        return wcd;
    }

    public void setWcd(double wcd) {
        this.wcd = wcd;
    }
    public double getLengthFitness() {
        return lengthFitness;
    }

    public double getOverlapFitness() {
        return overlapFitness;
    }
    public double getIdenticalStreams(){
        return identicalStreams;
    }
    public double getCost(){
        return cost;
    }
    public double getCriticalLink(){
        return criticalLink;
    }


    /**
     * generate streams paths
     * @param linkMatrix
     * @param endPoints indexes of the end systems
     */
    public void generateStreams(double[][] linkMatrix, List<Integer> endPoints){
        List<Integer> exclude = new ArrayList<Integer>(endPoints);
        exclude.remove(exclude.indexOf(src));
        exclude.remove(exclude.indexOf(dest));
        List<List<Integer>> neighbours;

        for(int i = 0; i < rl; i++){
            neighbours = new ArrayList<List<Integer>>(
                    GAHelper.neighbourAsLists(linkMatrix,
                            exclude)
            );
            streams.add(GAHelper.getRandomPath(src, dest, neighbours));
        }

    }

    /**
     * calculate fitness using the provided weights
     * @param dimension number of devices
     * @param overlappingWeight
     * @param lengthWeight
     * @return sum of fitness calculated
     */
    public double updateFitness(int dimension, double overlappingWeight, double lengthWeight) {
        int[][] linkCount = new int[dimension][dimension];
        double fitness = 0;
        overlapFitness = 0;
        criticalLink = 0;
        lengthFitness = 0;
        identicalStreams = 0;
        //trying desperately to get the GA to balance the streams
        for(int i = 0; i < streams.size(); i++){
            for (int j = 0; j < streams.size(); j++){
                if(i == j || streams.get(i).size() != streams.get(j).size())
                    continue;
                int it = 0;
                boolean match = true;
                while (it < streams.get(i).size() && it < streams.get(j).size()){
                    if(!streams.get(i).get(it).equals(streams.get(j).get(it))) {
                        match = false;
                        break;
                    }
                    it++;
                }
                if(match)
                    identicalStreams++;
            }
        }
        for(List<Integer> stream : streams){
            lengthFitness += stream.size()*lengthWeight-1;
            for(int i = 1; i < stream.size(); i++){
                linkCount[stream.get(i-1)][stream.get(i)] += 1;
            }
        }
        for(List<Integer> stream : streams){
            for(int i = 1; i < stream.size(); i++){
                if(linkCount[stream.get(i-1)][stream.get(i)] > 1){
                    overlapFitness += linkCount[stream.get(i-1)][stream.get(i)]-1;
                }
                if(linkCount[stream.get(i-1)][stream.get(i)] == rl && rl > 1){
                    criticalLink++;
                };
            }
        }

        return fitness;

    }

    public int[][] localLinkCount(int dim){
        int[][] localLinks = new int[dim][dim];
        for(List<Integer> stream : streams){
            for(int i = 1; i < stream.size(); i++){
                if(localLinks[stream.get(i-1)][stream.get(i)] == 0){
                    localLinks[stream.get(i-1)][stream.get(i)] = 1;
                }
            }
        }
        return localLinks;
    }


    /**
     * Used by the stream collection class to calculate the total bandwidth used on links
     * @param mtx current usage
     * @return mtx with the used bandwidth of this Stream
     */
    public double[][] usedBandwidth(double[][] mtx){
        double[][] streamUsage = new double[mtx.length][mtx.length];
        for(List<Integer> stream : streams){
            for(int i = 1; i < stream.size(); i++){
                streamUsage[stream.get(i-1)][stream.get(i)] += cost;
            }
        }
        for(int i = 0; i < streamUsage.length; i++)
            for (int j = 0; j < streamUsage.length; j++){
                if(streamUsage[i][j] > 0) {
                    mtx[i][j] += cost;
                }
        }
        return mtx;
    }

    public double[][] updateCycleTime(double[][] mtx, double[][] adjMatrix){
        double[][] streamUsage = new double[mtx.length][mtx.length];
        for(List<Integer> stream : streams){
            for(int i = 1; i < stream.size(); i++){
                if(streamUsage[stream.get(i-1)][stream.get(i)] == 0){
                    streamUsage[stream.get(i-1)][stream.get(i)] =
                            size / adjMatrix[stream.get(i-1)][stream.get(i)];
                }
            }
        }
        for(int i = 0; i < streamUsage.length; i++)
            for (int j = 0; j < streamUsage.length; j++){
                if(streamUsage[i][j] > 0) {
                    mtx[i][j] += streamUsage[i][j];
                }
            }
        return mtx;
    }

    /**
     *
     * @param adjMatrix
     * @return true if the stream is valid, false otherwise
     */
    public boolean eval(double[][] adjMatrix){
        int it = 0;
        for(List<Integer> stream : streams){
            for(int i = 1; i < stream.size(); i++){
                if(adjMatrix[stream.get(i-1)][stream.get(i)] < 1) {
                    /*System.out.printf("link %d-%d is invalid in %s %d",
                            stream.get(i-1),
                            stream.get(i),
                            id,
                            it);*/
                    return false;
                }
            }
            it++;
        }
        return true;
    }

    /**
     * choosing a random node in a random stream, and generate a new route to the destination.
     * This is done to keep diversity in the population
     * @param adjMatrix
     * @param endpoints
     */
    public void mutate(double[][] adjMatrix, List<Integer> endpoints){
        try {
            List<Integer> exclude = new ArrayList<>(endpoints);
            List<Integer> listToMutate = streams.remove(random.nextInt(streams.size()));
            int idx = random.nextInt(listToMutate.size() - 2) + 1;
            int src = listToMutate.get(idx);
            listToMutate = listToMutate.subList(0, idx);
            exclude.remove(exclude.indexOf(this.dest));
            List<List<Integer>> neighbours = new ArrayList<>(
                    GAHelper.neighbourAsLists(adjMatrix,
                            exclude)
            );
            listToMutate.addAll(GAHelper.getRandomPath(src, dest, neighbours));
            streams.add(listToMutate);
        }catch (Exception e){
            //System.out.println("Mutating error");
            e.printStackTrace();
        }
    }

    public void repair(){
        for(List<Integer> stream : streams){
            while(allDifferent(stream)){
                stream = GAHelper.repair(stream);
            }
        }
    }

    public boolean allDifferent(List<Integer> stream){
        Set<Integer> set = new HashSet<Integer>(stream);
        return set.size() != stream.size();
    }

    public double getPeriod() {
        return period;
    }

    public double getSize() {
        return size;
    }

    public List<List<Integer>> getStreams(){
        return streams;
    }

    public void setStream(List<List<Integer>> newStreams) {
        streams = new ArrayList<>(newStreams);
    }

    public String getId() {
        return id;
    }

    public void print(){
        System.out.println(id);
        for(List<Integer> s : streams){
            s.forEach((Integer link) -> System.out.printf("%d ", link));
            System.out.println();
        }
    }

    public double getDeadline() {
        return deadline;
    }

    public void setDeadline(double deadline) {
        this.deadline = deadline;
    }

}
