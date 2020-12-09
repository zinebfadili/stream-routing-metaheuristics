package GA;

import java.util.*;
import java.util.stream.Collectors;

public class GAHelper {

    public static Random random;
    /**
     * Constructs a list of neighbours for each device from the adj matrix.
     * @param adjMatrix
     * @param exclude list of indexes in the adjMatrix that should not be included as neighbours
     * @return a list of list containing neighbours for each link
     */
    public static List<List<Integer>> neighbourAsLists(double[][] adjMatrix, List<Integer> exclude){
        List<List<Integer>> neigh = new ArrayList<List<Integer>>();
        List<Integer> adj;
        int n = adjMatrix.length;
        for(int i = 0; i < n; i++){
            adj = new ArrayList<>();
            if(exclude.contains(i)) {
                neigh.add(adj);
                continue;
            }
            for(int j = 0; j < n; j++){
                if(exclude.contains(j))
                    continue;
                if(adjMatrix[i][j] > 0){
                    adj.add(j);
                }
            }
            neigh.add(adj);
        }
        return neigh;
    }

    /**
     * Constructs a random path from the given source to the given destination
     * using the neighbours provided.
     * @param src starting idx
     * @param dest destination idx
     * @param neighbours
     * @return sequence of idx of the taken route
     */
    public static List<Integer> getRandomPath(int src, int dest, List<List<Integer>> neighbours){
        int i = src;
        int next;
        List<Integer> path = new ArrayList<Integer>();
        int neigh[];
        path.add(i);
        while(i != dest){
            if(neighbours.get(i).size() > 0) {
                next = neighbours.get(i).indexOf(dest);
                if (next == -1) {
                    next = random.nextInt(neighbours.get(i).size());
                    int indexOfNext = path.indexOf(neighbours.get(i).get(next));
                }
                next = neighbours.get(i).remove(next);
                i = next;
                path.add(i);
            }else{
                path.remove(path.size()-1);
                i = path.get(path.size()-1);
            }
        }
        Set <Integer>set = new HashSet<Integer>(path);
        while(set.size() != path.size()){
            path = repair(path);
            set = new HashSet<Integer>(path);
        }
        return path;
    }
    public static void updatePopulationFitness(List<StreamCollection> population){
        population.parallelStream().forEach(i -> {
            i.setRank(-1);
            i.setCrowdingDistance(0);
        } );
        normalizeFitnessValues(population);
        List<StreamCollection> notRanked = new ArrayList<>(population);
        List<StreamCollection> ranked;

        int rank = 1;
        while (!notRanked.isEmpty()){
            ranked = new ArrayList<>();

            ranked.addAll(notRanked.parallelStream().filter(individual ->
                notDominated(individual, notRanked)
            ).collect(Collectors.toList()));
            ranked.stream().forEach(notRanked::remove);
            rank++;
        }

        Map<Integer, List<StreamCollection>> rankGroup = population.stream().collect(Collectors.groupingBy(i -> {
            return i.getRank();
        }));

        rankGroup.forEach((i,sc) -> calculateCrowdingDistance(i, sc));
    }

    private static boolean notDominated(StreamCollection individualA, List<StreamCollection> notRanked) {
        boolean dominated = notRanked.parallelStream().anyMatch(individualB ->(
                    individualA != individualB &&
                    individualB.getOverlapFitness() <= individualA.getOverlapFitness() &&
                    individualB.getLengthFitness() <= individualA.getLengthFitness() &&
                    individualB.getCriticalLinkFitness() <= individualA.getCriticalLinkFitness() &&
                    individualB.getNormalizedIStreams() <= individualA.getNormalizedIStreams() &&
                    //individualB.getNormalizedCT() <= individualA.getNormalizedCT() &&
                    System.identityHashCode(individualB) < System.identityHashCode(individualA)

            )
        );
        return !dominated;

    }

    public static double calcCycleTime(int[][] lMatrix, double[][] adjMatrix){
        double cTime = 0;



        return cTime;
    }

    //Maybe this should be rewritten as a single loop
    public static void normalizeFitnessValues(List<StreamCollection> population) {
        double maxIStreams = population
                .stream()
                .max(
                        Comparator.comparing(StreamCollection::getIdenticalStreams)
                )
                .orElseThrow(NoSuchElementException::new)
                .getIdenticalStreams();
        double maxOLFitness = population
                .stream()
                .max(
                        Comparator.comparing(StreamCollection::getOverlapFitness)
                )
                .orElseThrow(NoSuchElementException::new)
                .getOverlapFitness();

        double maxLFitness = population.stream()
                .max(
                        Comparator.comparing(StreamCollection::getLengthFitness)
                ).orElseThrow(NoSuchElementException::new)
                .getLengthFitness();

        double maxCLFitness = population.stream()
                .max(
                        Comparator.comparing(StreamCollection::getCriticalLinkFitness)
                ).orElseThrow(NoSuchElementException::new)
                .getCriticalLinkFitness();
        /*double maxCT = population.stream()
                .max(
                        Comparator.comparing(StreamCollection::getCycleTime)
                ).orElseThrow(NoSuchElementException::new)
                .getCriticalLinkFitness();*/
        population.parallelStream().forEach(i->
        {
            i.setNormalizedLFitness(i.getLengthFitness()/maxLFitness);
            i.setNormalizedOLFitness(i.getOverlapFitness()/maxOLFitness);
            i.setNormalizedCLFitness(i.getCriticalLinkFitness()/maxCLFitness);
            i.setNormalizedIStreams(i.getIdenticalStreams()/maxIStreams);
            //i.setNormalizedCT(i.getIdenticalStreams()/maxCT);
        });
    }

    private static void calculateCrowdingDistance(Integer idx, List<StreamCollection> sc) {
        List<StreamCollection> overlap = new ArrayList<>();
        List<StreamCollection> length = new ArrayList<>();
        List<StreamCollection> criticalLinks = new ArrayList<>();
        List<StreamCollection> identicalStreams = new ArrayList<>();
        //List<StreamCollection> cycleTime = new ArrayList<>();
        sc.sort(Comparator.comparing(streamCollection -> streamCollection.getNormalizedOLFitness()));
        overlap.addAll(sc);
        sc.sort(Comparator.comparing(streamCollection -> streamCollection.getNormalizedLFitness()));
        length.addAll(sc);
        sc.sort(Comparator.comparing(streamCollection -> streamCollection.getNormalizedCLFitness()));
        criticalLinks.addAll(sc);
        sc.sort(Comparator.comparing(streamCollection -> streamCollection.getNormalizedIStreams()));
        identicalStreams.addAll(sc);
        //sc.sort(Comparator.comparing(streamCollection -> streamCollection.getNormalizedCT()));
        //cycleTime.addAll(sc);

        for(int i = 0; i < sc.size()-1; i++){
            if(i == 0 /*|| i == sc.size()-1*/) {
                overlap.get(i).setCrowdingDistance(Double.MAX_VALUE);
                length.get(i).setCrowdingDistance(Double.MAX_VALUE);
                criticalLinks.get(i).setCrowdingDistance(Double.MAX_VALUE);
                identicalStreams.get(i).setCrowdingDistance(Double.MAX_VALUE);
                //cycleTime.get(i).setCrowdingDistance(Double.MAX_VALUE);
            }

            else{
                //messy, but I tried to do this:
                //http://citeseerx.ist.psu.edu/viewdoc/download;jsessionid=0C941CF35DBAE3BB32C2E1BA9DAA5F6A?doi=10.1.1.542.385&rep=rep1&type=pdf

                overlap.get(i).
                        setCrowdingDistance(
                                overlap.get(i).getCrowdingDistance() +
                                overlap.get(i-1).getNormalizedOLFitness() -
                                overlap.get(i+1).getNormalizedOLFitness());

                length.get(i).
                        setCrowdingDistance(
                                length.get(i).getCrowdingDistance() +
                                length.get(i-1).getNormalizedLFitness() -
                                 length.get(i+1).getNormalizedLFitness());

                criticalLinks.get(i).
                        setCrowdingDistance(
                                criticalLinks.get(i).getCrowdingDistance() +
                                criticalLinks.get(i-1).getNormalizedCLFitness() -
                                criticalLinks.get(i+1).getNormalizedCLFitness());
                identicalStreams.get(i)
                        .setCrowdingDistance(
                                identicalStreams.get(i).getCrowdingDistance() +
                                identicalStreams.get(i-1).getNormalizedIStreams() -
                                identicalStreams.get(i+1).getNormalizedIStreams());
                /*cycleTime.get(i)
                        .setCrowdingDistance(
                                cycleTime.get(i).getCrowdingDistance() +
                                cycleTime.get(i-1).getNormalizedCT() -
                                cycleTime.get(i+1).getNormalizedCT());*/
            }
        }
    }

    public static List<Integer> repair(List<Integer> stream){
        for(int i = stream.size()-1; i >= 0; i--){
            int first = stream.indexOf(stream.get(i));
            if(first != i){
                List<Integer> newStream = new ArrayList<>();
                newStream.addAll(stream.subList(0, first));
                newStream.addAll(stream.subList(i, stream.size()));
                return newStream;
            }
        }
        return stream;
    }

    //Double and triple check this
    private static double calculateDistance(Vector<Double> currentPosition, Vector<Double> leftPosition) {
        double yDiff = Math.abs(currentPosition.get(0) - leftPosition.get(0));
        double xDiff = Math.abs(currentPosition.get(1) - leftPosition.get(1));
        return Math.sqrt(Math.pow(yDiff, 2)+Math.pow(xDiff, 2));
    }
    public static void checkWCD(GeneticAlgorithm ga) {
        List<StreamCollection> unshedulable = new ArrayList<>();
        ga.population.parallelStream().forEach(i -> {
            i.calculateWCD(ga.adjMatrix);
            if(!i.schedulable)
                unshedulable.add(i);
        });
        ga.population.removeAll(unshedulable);
    }
}
