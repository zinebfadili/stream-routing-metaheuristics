package Common;

public class Link {

    private final String src;
    private final String dest;
    private final double speed;

    public Link(String src, String dest, double speed){
        this.src = src;
        this.dest = dest;
        this.speed = speed;
    }

    public String getName()
    {
        return src + dest;
    }
    public String getSrc() {
        return src;
    }

    public String getDest() {
        return dest;
    }

    public double getSpeed() {
        return speed;
    }

    public double getSpeedMbit() {
        return speed * 8;
    }

}
