import org.junit.Assert;

import static org.junit.Assert.*;

public class LinkTest {

    Link link;
    @org.junit.Before
    public void setup(){
        link = new Link("ES1", "ES2", 1.25);
    }


    @org.junit.Test
    public void getSrc() {
        Assert.assertEquals("ES1", link.getSrc());
    }

    @org.junit.Test
    public void getDest() {
        Assert.assertEquals("ES2", link.getDest());
    }

    @org.junit.Test
    public void getSpeed() {
        Assert.assertEquals(1.25, link.getSpeed(), 0);
    }

    @org.junit.Test
    public void getSpeedMbit() {
        Assert.assertEquals(1.25, link.getSpeedMbit(), 0);
    }
}