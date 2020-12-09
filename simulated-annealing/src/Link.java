public class Link {

    private final String src;
    private final String dest;
    private final double speed;

    public Link(String src, String dest, double speed){
        this.src = src;
        this.dest = dest;
        this.speed = speed; //to convert from B/US to Mbit/s
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

    @Override
    public int hashCode() {
        return this.src.hashCode() + this.getDest().hashCode() + (int)this.getSpeed();
    }

    @Override
    public String toString(){
        return "Src : " + this.src + " Dst: " + this.dest + " Speed: " + this.speed;
    }

}
