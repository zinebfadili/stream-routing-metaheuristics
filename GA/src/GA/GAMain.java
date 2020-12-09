package GA;

import Common.Configuration;
import Common.Parser;
import Common.Link;
import Common.Stream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GAMain {

    public static void main(String[] args) {
        Integer maxGenerations = 1000;
        Integer popSize = 100;
        Integer sameIndividual = 200;
        Integer seed = 1234;
        String input_name = "TC3_medium.";
        if(args.length < 4){
            System.err.println("Program needs the following arguments:");
            System.err.println("maxGenerations, popSize, sameIndividual, seed, input_name");
            return;
        }
        maxGenerations = Integer.parseInt(args[0]);
        popSize = Integer.parseInt(args[1]);
        sameIndividual = Integer.parseInt(args[2]);
        seed = Integer.parseInt(args[3]);
        input_name = args[4];
        double time = System.currentTimeMillis();
        Configuration config = Parser.createConfigFromXML("./input/" +input_name+
                "app_network_description");

        GAHelper.random = new Random(seed);
        GeneticAlgorithm ga = new GeneticAlgorithm(popSize, config);

        ga.initialize(config.getStreamList());

        int generation = 0;
        while(ga.getBestFitnessCount() < sameIndividual && generation < maxGenerations) {
            generation++;
            ga.doGeneration();

        }
        GAHelper.checkWCD(ga);

        ga.bestIndividual.repair();


        int valid;
        if(ga.eval(ga.bestIndividual))
            valid = 1;
        else
            valid = 0;

        //System.out.println("Cost for solution: " + ga.bestIndividual.getCost());

        ga.bestIndividual.repair();
        //System.out.println("Fitness: " + ga.bestIndividual.getFitness());
        for (StreamConfig sc : ga.bestIndividual.streams) {
            String streamID   = sc.getId();
            config.setRoutesForStream(streamID,sc.streams);
        }
        config.calculateWCD();
        Parser.writeOutputToXML(config,"./output/"+input_name+"solution.xml");
        double avg_wcd = config.calculateAvgWCD();
        System.out.println("Average wcd is: " + avg_wcd);
        ga.bestIndividual.checkBw(ga.adjMatrix);
        double bestSolution = ga.bestIndividual.getCost();
        double lFitness = ga.bestIndividual.getLengthFitness();
        double olFitness = ga.bestIndividual.getOverlapFitness();
        double clFitness = ga.bestIndividual.getCriticalLinkFitness();
        time = System.currentTimeMillis() - time;
        System.out.printf("Valid Solution = %d Time(ms) = %f Steps = %d BestSolution = %f Length = %f Overlap = %f CriticalLinks = %f\n",
                valid, time, generation, bestSolution, lFitness, olFitness, clFitness);
    }
}
