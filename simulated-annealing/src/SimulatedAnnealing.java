import java.util.*;

public class SimulatedAnnealing {


    private List<Stream> streams;
    private int totalNumberOfLinks;
    private boolean inExchange;

    /**
     * Method to calculate the cost of the current configuration
     * @param weightLength
     * @param weightOverlap
     * @param weightUtilisation
     * @return cost of current configuration
     */
    int cost(int weightLength, int weightOverlap, int weightUtilisation)
    {
        int cost = 0;

        //Bandwidth used per link
        HashMap<Link, Double> utilisedRate = new HashMap<>();

        //Storing the unique links in the current configuration
        Set<Link> uniqueLinks = new HashSet<>();

        int totalOverlap = 0;
        double distanceFromMaximum = 0; //how far we are from "perfect" utilization rate (0)
        int totalLinks = 0;

        for(Stream stream : streams)
        {
                List<List<Link>> chosenRoutes = stream.getChosenRoutes();
                //Used links for this stream
                HashMap<Link, Integer> usedLinks = new HashMap<>();
                //Bandwidth consumed by this link
                HashMap<Link, Double> utilisedRateForThisStream = new HashMap<>();
                for(List<Link> route : chosenRoutes)
                {
                    for(Link link : route)
                    {
                        totalLinks++;
                        if(!usedLinks.containsKey(link))
                        {
                            usedLinks.put(link, 1);
                        } else {
                            usedLinks.put(link, usedLinks.get(link)+1);
                        }

                        //For each redundant copy of the stream, we consider that it is independent and goes
                        //through this link
                        if(!utilisedRateForThisStream.containsKey(link))
                        {
                            utilisedRateForThisStream.put(link, stream.getUsedBw());
                        } else {
                            utilisedRateForThisStream.put(link, utilisedRateForThisStream.get(link)+ stream.getUsedBw());
                        }
                        uniqueLinks.add(link);
                    }
                }
                //Since we can't iterate over a map
                for(Link link : uniqueLinks)
                {
                    if(usedLinks.containsKey(link))
                    {
                        //Calculate the total overlap caused by this stream, the explanation of this formula is in the report
                        int currentOverlap = usedLinks.get(link)*(usedLinks.get(link)-1);

                        totalOverlap += currentOverlap;

                        //Merge and split. If there is only one stream that uses the link then we will divide by one,
                        //i.e nothing happens
                        //otherwise we divide by a number equal to the number of redundant copies that go through this stream
                        //say that number is equal to n.
                        //previously we had added n*streamspeed; Here we divide n*streamspeed by n. So we only count
                        //one redundant copy of the stream.
                        utilisedRateForThisStream.put(link, utilisedRateForThisStream.get(link)/usedLinks.get(link));
                        if(!utilisedRate.containsKey(link))
                        {
                            utilisedRate.put(link, utilisedRateForThisStream.get(link));
                        } else {
                            utilisedRate.put(link, utilisedRate.get(link) + utilisedRateForThisStream.get(link));
                        }
                    }
                }

        }
        for(Link link : uniqueLinks)
        {
            utilisedRate.put(link, utilisedRate.get(link)/link.getSpeed());
            //If the link limit is exceeded we don't want this solution, we git it the highest cost possible
            if(utilisedRate.get(link)>1)
            {
                return Integer.MAX_VALUE;
            }

            distanceFromMaximum += utilisedRate.get(link)-1;

        }
        //calculating the final cost
        //the explanation for each part of the cost is in the report
        cost = (int) (weightLength*totalLinks/totalNumberOfLinks + weightOverlap*totalOverlap + weightUtilisation*distanceFromMaximum);
        return cost;
    }

    /**
     * Method to get the index of the route in chosenRoutes in the entires routes attribute
     * @param streamNumber
     * @param routeNumberInChosenRoutes
     * @return index of route in chosen route amongst all possible routes for a stream
     */
    int getIndexInAllRoutes(int streamNumber, int routeNumberInChosenRoutes)
    {
        Stream currentStream = streams.get(streamNumber);
        List<List<Link>> routes = currentStream.getRoutes();
        List<Link> chosenRoute = currentStream.getChosenRoutes().get(routeNumberInChosenRoutes);
        int routeIndex = 0;

        for(List<Link> route : routes)
        {
            if(route.equals(chosenRoute))
            {
                break;
            }
            routeIndex++;
        }

        return routeIndex;
    }

    /**
     * Method to exchange route (visit a neighbor solution)
     * @param streamNumber
     * @param routeNumber
     * @return
     */
    int exchangeRoutes(int streamNumber, int routeNumber) {
        Stream currentStream = streams.get(streamNumber);

        int formerRouteIndex = getIndexInAllRoutes(streamNumber, routeNumber);

        int newRoute = (int) (Math.random()*streams.get(streamNumber).getRoutes().size());

        while(streams.get(streamNumber).getRoutes().size()>1 && formerRouteIndex==newRoute)
        {
            newRoute = (int) (Math.random()*streams.get(streamNumber).getRoutes().size());
        }

        currentStream.removeRouteFromChosen(routeNumber);

        currentStream.addRouteToChosen(newRoute);

        return formerRouteIndex;
    }

    /**
     * Method to undo exchange of routes (if we didn't keep the neighbor solution explored)
     * @param streamNumber
     * @param routeToAddBack
     */
    void undoExchange(int streamNumber, int routeToAddBack) {
        Stream currentStream = streams.get(streamNumber);

        int routeToRemove = currentStream.getChosenRoutes().size()-1;

        currentStream.removeRouteFromChosen(routeToRemove);

        currentStream.addRouteToChosen(routeToAddBack);

    }

    /**
     * Step method : visiting neighbor solutions and evaluating them
     * @param currentCost
     * @param temperature
     * @param weightLength
     * @param weightOverlap
     * @param weightUtilisation
     * @return cost of the current configuration
     */
    double step(double currentCost, double temperature, int weightLength, int weightOverlap, int weightUtilisation) {

        int randomStream, randomRoute;
        double newCost;

        randomStream = (int) (Math.random()*streams.size());

        randomRoute = (int) (Math.random()*streams.get(randomStream).getChosenRoutes().size());

        int removedRoute = exchangeRoutes(randomStream, randomRoute);

        newCost = cost(weightLength, weightOverlap, weightUtilisation);

        double costDiff = newCost - currentCost;

        //If the new solution has a lower cost, we choose it
        if (costDiff < 0)
        {
            currentCost = newCost;
        } else
            //otherwise we only choose it if the acceptance rate is high enough
        {
            if (Math.random() < Math.exp(-costDiff / temperature))
            {
                currentCost = newCost;
            } else {
                undoExchange(randomStream, removedRoute);
            }

        }
        return currentCost;
    }


    /**
     * Simuated annealing algorithm : visiting neighbor solutions, updating the time and the temperature
     * @param T0
     * @param BETA0
     * @param MAXTIME
     * @param BETA
     * @param ALPHA
     * @param weightLength
     * @param weightOverlap
     * @param weightUtilisation
     */
    void simulatedAnnealing(double T0, double BETA0, int MAXTIME, double BETA, double ALPHA, int weightLength, int weightOverlap, int weightUtilisation) {

        boolean solutionFound = false; // the solution has not been found yet
        double currentCost = cost(weightLength, weightOverlap, weightUtilisation); // the current cost is the one of the initial state
        double bestCost = currentCost; // the best cost is the current cost
        double temperature = T0;
        int elapsed = 0;

        int spent = (int) Math.floor(BETA0 * MAXTIME); // BETA0<1, we spend a fraction of the maximum time for our

        int timer = spent; // our timer
        /*long startTime = System.nanoTime();
        long maxTime = startTime + duration;*/

        while (elapsed < MAXTIME && !solutionFound) // while we haven't spent the whole time we allow ourselves
        // (MAXTIME), and the solution hasn't been found
        {
            System.out.format("%d, out of %d spent \n", elapsed, MAXTIME);
            bestCost = currentCost; // the best cost is the current cost
            while (timer != 0) { // we still have time at this temperature
                currentCost = step(currentCost, temperature, weightLength, weightOverlap, weightUtilisation); // we calculate the currentcost
                if (currentCost == 0) { // if the cost calculated is equal to zero, it means that we have found the best
                    // solution we can stop
                    bestCost = currentCost; // the best cost is 0
                    solutionFound = true; // the solution is found
                    break; // we stop
                } else if (currentCost < bestCost) {
                    bestCost = currentCost; // we have found a solution for which the cost is lowest to this point so we
                    // keep it as best cost 
                }
                timer -= 1; // decrease the timer by 1
            }
            elapsed += spent; // elapsed is the total time spent, so we add to it the time we just spent at
            // the possible solution
           /* System.out.println("At temperature T=" + temperature + ", time spent : " + spent + " out of " + MAXTIME
                    + ". Total time spent so far :" + elapsed + ". Best cost so far :" + bestCost + "\n");*/

            spent = (int) Math.floor(BETA * spent); // we spend more time at a lower temperature (BETA>1)
            timer = spent;
            temperature = temperature * ALPHA; // we decrease the temperature (ALPHA<1)
        }

    }

    /**
     * Find a starting solution
     */
    void initialAssignation()
    {
        for(Stream stream : streams)
        {
            //to make sure that we don't add the same route twice, if the redundancy level >2
            Set<Integer> indexesOfRoutes = new HashSet<Integer>();
            List<List<Link>> chosenRoutes = new ArrayList<>();
            int numberOfPossibleRoutes = stream.getRoutes().size();
            for(int i=0; i<stream.getRl(); i++)
            {
                int aRoute = (int) (Math.random()*numberOfPossibleRoutes);

                chosenRoutes.add(stream.getRoutes().get(aRoute));
            }

            stream.setChosenRoutes(chosenRoutes);
        }
        System.out.println("exited");
    }

    /**
     * Printing configuration
     */
    void printConfig()
    {
        for(Stream stream : streams)
        {
            System.out.println("----------------------");
            System.out.println("Stream : " + stream.getId() + " with redundancy level " + stream.getRl());
            System.out.println("Chosen routes :");
            System.out.println();
            for(List<Link> route : stream.getChosenRoutes())
            {
                System.out.println("Route : ");
                for(Link link : route)
                {
                    System.out.println(link.getSrc() + " - " + link.getDest());
                }
                System.out.println();
            }
        }
    }

    /**
     * Get total number of links in the provided xml
     */
    void getTotalNumberOfLinks()
    {
        Set<Link> links = new HashSet<Link>();

        for(Stream stream : streams)
        {
            for(List<Link> route : stream.getRoutes())
            {
                for(Link link : route)
                {
                    links.add(link);
                }
            }
        }

        totalNumberOfLinks = links.size();
    }

    /**
     * Getting all links that go to each destination
     * @param links
     * @return Destination and the link that go to it
     */
    HashMap<String, List<Link>> constructConnections(List<Link> links)
    {
        HashMap<String, List<Link>> connections = new HashMap<>();

        for (Link link : links)
        {
            if(!connections.containsKey(link.getSrc()))
            {
                List<Link> linkedLinks = new ArrayList<>();
                linkedLinks.add(link);
                connections.put(link.getSrc(), linkedLinks);
            } else {
                connections.get(link.getSrc()).add(link);
            }
        }


        return connections;
    }

    public void calculateWCD()
    {
        //wcd(fi) = (h + 1) · C  where h = no. of hops(or switches according to the book) and C = cycle time
        //Each node k in the network has a number of egress ports (one for each connection to another node).
        // Let the set of all egress ports in all nodes be denoted by P.
        // An egress port p ∈ P is connected to a link p.l, which has a certain speed p.l.speed.
        // Let the set of streams passing through a port p be denoted Fp.

        HashMap<String,Double> sums_Fp = new HashMap<String, Double>();
        for (Stream s: streams) {
            double size = s.getSize();
            ArrayList<String> linksFound = new ArrayList<String>();
            for(List<Link> route: s.getChosenRoutes())
                for (Link link : route) {
                    if(!linksFound.contains(link.getName()))
                    {
                        linksFound.add(link.getName());
                    }
                    else {
                        continue;
                    }
                    double val = size/link.getSpeed();
                    if(sums_Fp.containsKey(link.getName()))
                    {
                        val += sums_Fp.get(link.getName());
                    }
                    sums_Fp.put(link.getName(),val);
                }
        }
        double cycle_time = 0;

        for (String key: sums_Fp.keySet()) {
            if(sums_Fp.get(key)>cycle_time)
            {
                cycle_time = sums_Fp.get(key);
            }
        }
        for (Stream stream: streams)
        {

            double wcd = 0;
            int i = 0;
            for (List<Link> route : stream.getChosenRoutes()) {
                wcd = route.size() * cycle_time;
                stream.addWCDValue(i,wcd);
                i++;
            }
        }
    }

    public static void main(String[] args)
    {
        //Get input and output file paths from user
        if(args.length!=2)
        {
            System.out.println("Not enough arguments");
            return;
        }

        String inputPath = args[0];
        String outputPath = args[1];



        SimulatedAnnealing algo = new SimulatedAnnealing();

        //Simuation Annealing parameters
        double T0=120;
        double ALPHA=0.95;
        double BETA=1.1;
        double BETA0=0.001;
        int MAXTIME=3000000;
        int weightLength = 60;
        int weightOverlap = 50;
        int weightUtilisation = 20;

        //Creating configuration
        Configuration config = Parser.createConfigFromXML(inputPath);
        algo.streams = config.getStreamList();

        ArrayList<Link> linkList = config.getLinkList();

        //Construct list of all possible routes for each stream
        HashMap<String, List<Link>> connections = algo.constructConnections(linkList);
        for(Stream stream : algo.streams)
        {
            ArrayList<Route> result = new ArrayList<>();
            Routes.getAllRoutes(stream.getSrc(), stream.getDest(), connections, result, new Route(), new HashSet<String>());
            List<List<Link>> possibleRoutes = new ArrayList<>();
            for(Route route : result)
            {
                possibleRoutes.add(route.getRoute());
            }
            stream.setRoutes(possibleRoutes);
        }

       // Create an initial solution
        algo.getTotalNumberOfLinks();
        algo.initialAssignation();
        while(algo.cost(weightLength, weightOverlap, weightUtilisation)==Integer.MAX_VALUE) {
            algo.initialAssignation();
        }
        System.out.println("Initial configuration :");
        algo.printConfig();
        System.out.println();

        //System.out.println("Start of simulated annealing, with initial cost : " + algo.cost(weightLength, weightOverlap, weightUtilisation));

        //Launch the simulation annealing
        long startTime = System.nanoTime();
        algo.simulatedAnnealing(T0, BETA0, MAXTIME, BETA, ALPHA, weightLength, weightOverlap, weightUtilisation);
        long endTime = System.nanoTime();
        System.out.println("Simulated annealing duration: " + (endTime-startTime) + " on thread: " + Thread.currentThread().getName());
        System.out.println("Final configuration");
        algo.printConfig();

        //Output to xml file
        algo.calculateWCD();
        Parser.writeOutputToXML(algo.streams, outputPath);
        System.out.println("end of algorithm");
    }



}

