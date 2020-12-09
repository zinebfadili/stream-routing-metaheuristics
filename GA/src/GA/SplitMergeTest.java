package GA;

import Common.Configuration;
import Common.Parser;
import org.junit.Assert;

public class SplitMergeTest {

    @org.junit.Test
    public void splitMergeTest(){
        Configuration config = Parser.createConfigFromXML("./input/TC4_split_and_merge." +
                "app_network_description");

        GeneticAlgorithm ga = new GeneticAlgorithm(100, config);
        ga.initialize(config.getStreamList());

        int generation = 0;
        while(ga.getBestFitnessCount() < 20 && generation < 100) {
            if(generation % 10 == 0) {
                System.out.printf("\n Generation: ");
            }
            generation++;
            System.out.printf("%6d", generation);
            ga.doGeneration();

        }
        System.out.println();

        System.out.println("solution is valid: " + ga.eval(ga.bestIndividual));


        double[][] am = config.getLinkMatrix();
        double[][] bwMatrix = new double[am.length][am.length];
        System.out.printf("      ");
        for(int i = 0; i < am.length; i++)
            System.out.printf("%5d", i);
        System.out.println();
        for(int i = 0; i < am.length; i++){
            System.out.printf("%5d| ", i);
            for(int j = 0; j  < am.length; j++)
                System.out.printf("%5.1f", am[i][j]);
            System.out.println();
        }

        bwMatrix = ga.bestIndividual.getUsedBW(bwMatrix);

        for(int i = 0; i < bwMatrix.length; i++)
            System.out.printf("%5d", i);
        System.out.println();
        for(int i = 0; i < bwMatrix.length; i++){
            System.out.printf("%5d| ", i);
            for(int j = 0; j  < bwMatrix.length; j++)
                System.out.printf("%5.1f", bwMatrix[i][j]);
            System.out.println();
        }
        ga.print(ga.bestIndividual);
        //Assert that the cost from SW2 to ES2 is counted once for stream0
        //but still counted for stream1
        Assert.assertEquals(
                ga.bestIndividual.streams.get(0).getCost() +
                ga.bestIndividual.streams.get(1).getCost(),
                bwMatrix[2][4],
                0.0002);
    }

}
