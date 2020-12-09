import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Routes {

    public static void main(String[] args){
        HashMap<String, List<Link>> connections = new HashMap<>();

        List<Link> e1 = new ArrayList<>();
        e1.add(new Link("E1", "E3", 1));
        e1.add(new Link("E1", "E4", 1));

        List<Link> e3 = new ArrayList<>();
        e3.add(new Link("E3", "E2", 1));

        List<Link> e4 = new ArrayList<>();
        e4.add(new Link("E4", "E2", 1));
        e4.add(new Link("E4", "E5", 1));

        List<Link> e2 = new ArrayList<>();
        e2.add(new Link("E2", "E4", 1));

        List<Link> e5 = new ArrayList<>();
        e5.add(new Link("E5", "E4", 1));

        connections.put("E1", e1);
        connections.put("E3", e3);
        connections.put("E4", e4);
        connections.put("E2", e2);
        connections.put("E5", e5);

        ArrayList<Route> result = new ArrayList<>();
        getAllRoutes("E1", "E2", connections, result, new Route(), new HashSet<String>());

        System.out.println(result);

    }

    /**
     * Algorithm used to return all the routes from current to destination if such a Route exists
     * @param current the current device (set to root at the beginning)
     * @param destination the destination we want to reach
     * @param connections an hashmap containing the different links from each destination
     * @param results the list of all the routes
     * @param currentRoute the current route on which we are during the calls
     * @param visited hashset to keep track of which links have been visited
     */
    public static void getAllRoutes(String current,
                                    String destination,
                                    HashMap<String, List<Link>> connections,
                                    List<Route> results,
                                    Route currentRoute,
                                    HashSet<String> visited){
        // if we are at the destination we are done
        if(current.equals(destination)){
            results.add(currentRoute);
            return;
        } else {
            // if we can reach an other device from the current one
            if(connections.containsKey(current)){
                // we get all the connections
                List<Link> links = connections.get(current);
                for(Link link : links){
                    // if we haven't yet visited the link
                    if(!visited.contains(link.getDest())){
                        //we prepare the recursive call
                        Route r2 = new Route(currentRoute);
                        r2.addLink(link);
                        visited.add(link.getDest());
                        getAllRoutes(link.getDest(), destination, connections, results, r2, new HashSet<>(visited));
                    }
                }
            }

        }

    }
}
