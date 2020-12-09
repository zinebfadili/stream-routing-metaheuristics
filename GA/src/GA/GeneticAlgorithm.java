package GA;


import java.util.*;
import java.util.stream.Collectors;
import Common.Configuration;
import Common.Stream;

public class GeneticAlgorithm {

    private int populationCount;

    private final int MUTATION_CHANCE = 20;

    List<StreamCollection> population = new ArrayList<StreamCollection>();
    Random random;
    Configuration config;
    double[][] adjMatrix;
    List<Integer> endpoints;
    Map<String, Integer> deviceNameToInt;
    double bestFitness = Double.MAX_VALUE;
    StreamCollection bestIndividual;
    StreamCollection bestOLIndividual;
    StreamCollection bestCLIndividual;
    StreamCollection bestLIndividual;
    int bestFitnessCount = 0;
    int bestOLFitnessCount = 0;
    int bestLFitnessCount = 0;
    int bestCLFitnessCount = 0;
    boolean usePareto = true;


    public void setBestOLIndividual(StreamCollection bestOLIndividual) {
        if(this.bestOLIndividual == bestOLIndividual){
            bestOLFitnessCount++;
        }else{
            this.bestOLIndividual = bestOLIndividual;
            bestOLFitnessCount = 0;
        }


    }

    public void setBestCLIndividual(StreamCollection bestCLIndividual) {
        if(this.bestCLIndividual == bestCLIndividual){
            bestCLFitnessCount++;
        }else{
            this.bestCLIndividual = bestCLIndividual;
            bestCLFitnessCount = 0;
        }
    }

    public void setBestLIndividual(StreamCollection bestLIndividual) {
        if(this.bestLIndividual == bestLIndividual){
            bestLFitnessCount++;
        }else{
            this.bestLIndividual = bestLIndividual;
            bestLFitnessCount = 0;
        }
    }


    public GeneticAlgorithm(int populationCount, Configuration config) {
        this.populationCount = populationCount;
        this.config = config;
        random = GAHelper.random;
        this.adjMatrix = config.getLinkMatrix();
        endpoints = new ArrayList<>();
        String[] devices = config.getDeviceNames();
        deviceNameToInt = new HashMap<>();

        //creating the device to index map.
        //this assumes that the devices have the same index in the device name list
        //as they have in the adjacency matrix
        //
        //List endpoints is generated. Endpoints have -1 on the diagonal
        for(int i = 0; i < adjMatrix.length; i++) {
            deviceNameToInt.put(devices[i], i);
            if (adjMatrix[i][i] < 0)
                endpoints.add(i);
        }
    }

    /**
     * Generate "populationCount" number of stream collections
     * @param streamDefinitions
     */
    public void initialize(List<Stream> streamDefinitions) {
        for (int i = 0; i < populationCount; i++) {
            StreamCollection sCollection = new StreamCollection();
            sCollection.generateCollection(streamDefinitions, deviceNameToInt, adjMatrix, endpoints);
            sCollection.repair();
            population.add(sCollection);
        }
        //set best individual to a value
        bestIndividual = population.get(0);
    }

    public int getBestFitnessCount(){
        return bestFitnessCount;
    }

    private void setBestIndividual(StreamCollection individual) {
        if (bestIndividual == individual) {
            bestFitnessCount++;
        } else {
            bestIndividual = individual;
            bestFitness = bestIndividual.getFitness();
            bestFitnessCount = 0;
        }
    }

    /**
     * calculate the fitness of each individual of the population
     */
    public void calcFitness() {
        population.parallelStream().forEach(individual -> {
            individual.fitness(adjMatrix, 1, 0.5, 1);
        });
    }

    /**
     * increment the generation by spawning "populationCount" children
     * and pick the fittest of the population
     */
    public void doGeneration(){
        List<StreamCollection> offspring = new ArrayList<StreamCollection>();
        //Selection
        StreamCollection parent1;
        StreamCollection parent2;

        if(usePareto && bestOLFitnessCount > 200 && bestLFitnessCount > 200 && bestCLFitnessCount > 200)
            usePareto = false;
        while(offspring.size() < populationCount) {
            // Selection
            parent1 = getParentTournament();
            parent2 = getParentTournament();
            while (parent1 == parent2) {
                parent2 = getParentTournament();
            }

            // Crossover
            StreamCollection offspring1 = crossover(parent1, parent2);
            StreamCollection offspring2 = crossover(parent2, parent1);

            // Mutation
            int mutate = random.nextInt(100);
            if(mutate < MUTATION_CHANCE){
                int offspring_to_mutate = random.nextInt(2);
                if(offspring_to_mutate == 1) {
                    mutate(offspring1);
                }
                else {
                    mutate(offspring2);
                }
            }
            offspring1.repair();
            offspring2.repair();
            offspring.add(offspring1);
            offspring.add(offspring2);

        }
        population.addAll(offspring);
        //long selectionTime = System.nanoTime() - selectionStart;

        //long fitnessStart = System.nanoTime();
        calcFitness();

        //long fitnessEnd = System.nanoTime() - fitnessStart;
        //new and fancier fitness
        if(usePareto) {
            GAHelper.updatePopulationFitness(population);


            //only pick the fittest "populationCount" number of individuals
            population = population.stream()
                    .sorted(
                            Comparator.comparing(
                                    StreamCollection::getRank
                            ).thenComparing(
                                    StreamCollection::getCrowdingDistance
                            ).reversed()
                    )
                    .collect(Collectors.toList())
                    .subList(0, populationCount);
            setBestCLIndividual(population.stream()
                    .min(Comparator.comparing(StreamCollection::getNormalizedCLFitness))
                    .orElseThrow(NoSuchElementException::new));
            setBestOLIndividual(population.stream()
                    .min(Comparator.comparing(StreamCollection::getNormalizedOLFitness))
                    .orElseThrow(NoSuchElementException::new));
            setBestLIndividual(population.stream()
                    .min(Comparator.comparing(StreamCollection::getNormalizedLFitness))
                    .orElseThrow(NoSuchElementException::new));

        }else{
            GAHelper.normalizeFitnessValues(population);
            population = population.stream()
                    .sorted(
                            Comparator.comparing(
                                    StreamCollection::getFitness
                            )
                    ).collect(Collectors.toList())
                    .subList(0, populationCount);

            setBestIndividual(population.stream()
                    .filter(i ->
                            i.satisfiesCTime()
                    )
                    .min(
                            Comparator.comparing(StreamCollection::getFitness)
                    ).orElseThrow(NoSuchElementException::new));
        }
    }

    /**
     *
     * @return the fitter of the parents
     */
    public StreamCollection getParentTournament(){
        StreamCollection parent1 = population.get(random.nextInt(populationCount));
        StreamCollection parent2 = population.get(random.nextInt(populationCount));

        while (parent1 == parent2){
            parent2 = population.get(random.nextInt(populationCount));
        }

        if(parent1.getRank() < parent2.getRank()){
            return parent1;
        }
        if(parent1.getRank() > parent2.getRank()){
            return parent2;
        }
        if(parent1.getCrowdingDistance() > parent2.getCrowdingDistance()){
            return parent1;
        }
        if(parent1.getCrowdingDistance() < parent2.getCrowdingDistance()){
            return parent2;
        }
        if(parent1.fitness > parent2.fitness){
            return parent1;
        }

        return parent2;
    }

    /**
     * spawn a child from two parents
     *
     * @param parent1
     * @param parent2
     * @return a combination of the parents
     */
    public StreamCollection crossover(StreamCollection parent1, StreamCollection parent2){
        StreamCollection child1 = new StreamCollection();

        for(int i = 0; i < parent1.getStreams().size(); i++){
            //Create a child StreamConfig with src,dest,rl,size,period of parent
            StreamConfig childConfig = new StreamConfig(parent1.getStreams().get(i));
            List<List<Integer>> streams1= new ArrayList<>(parent1.getStreams().get(i).getStreams());
            List<List<Integer>> streams2= new ArrayList<>(parent2.getStreams().get(i).getStreams());
            List<List<Integer>> newStreams = new ArrayList<>();
            while(!streams1.isEmpty()){
                newStreams.add(
                        crossoverStreams(streams1.remove(random.nextInt(streams1.size())),
                                streams2.remove(random.nextInt(streams2.size())))
                );
            }
            childConfig.setStream(newStreams);
            child1.getStreams().add(childConfig);
        }
        return child1;
    }


    /**
     * do crossover of a single stream.
     * pick a device that is common to the parents and take the first part of the route
     * from parent1, and the second from parent2
     * @param parent1
     * @param parent2
     * @return combined stream of parent 1 and parent 2
     */
    public List<Integer> crossoverStreams(List<Integer> parent1, List<Integer> parent2){
        List<Integer> child = new ArrayList<Integer>();

        int crossoverPoint = random.nextInt(parent1.size()-2)+1;
        while(!parent2.contains(parent1.get(crossoverPoint))){
            crossoverPoint--;
        }
        for(int i =0; i < crossoverPoint; i++){
            child.add(parent1.get(i));
        }
        for(int i = parent2.lastIndexOf(parent1.get(crossoverPoint));i < parent2.size(); i++){
            child.add(parent2.get(i));
        }
        return child;
    }

    /**
     * mutate a random stream in given stream collection
     * @param sc
     */
    public void mutate(StreamCollection sc){

        int bound = random.nextInt(sc.streams.size());
        for(int i = 0; i < bound; i++) {
            sc.getStreams().get(
                    random.nextInt(
                            sc.getStreams().size()
                    )
            ).mutate(adjMatrix, endpoints);
        }
    }

    public void print(int i){
        population.get(i).print();
    }

    public void print(StreamCollection sc){
        sc.print();
    }

    public void eval(){

        for(StreamCollection sc : population){
            sc.eval(adjMatrix);
        }
    }

    public boolean eval(int i){
        return population.get(i).eval(adjMatrix);
    }

    public boolean eval(StreamCollection sc){
        if(!sc.bandwidthOk)
            return false;
        if(sc.getSmallestPeriod() > sc.getCycleTime())
            return sc.eval(adjMatrix);
        return false;
    }



}
