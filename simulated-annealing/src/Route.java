import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a route between two devices
 */
public class Route {
    /**
     * The list of all links between the routes
     */
    private List<Link> route;

    /*
     * Constructors
     */
    public Route(){
        this.route = new ArrayList<>();
    }

    public Route(List<Link> route){
        this.route = new ArrayList<>(route);
    }

    public Route(Route route){
        this.route = route.getRoute();
    }

    /**
     * Method to add a link to the route
     * @param link
     */
    public void addLink(Link link){
        this.route.add(link);
    }

    /*
     * Accessors & toString
     */

    public List<Link> getRoute(){
        return new ArrayList<>(route);
    }

    public void setRoute(List<Link> route){
        this.route = new ArrayList<>(route);
    }

    @Override
    public String toString(){
        return this.route.toString();
    }



}
