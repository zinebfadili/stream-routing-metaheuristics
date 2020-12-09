package GA;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OverlapTest {

    @org.junit.Test
    public void testOverlap(){
        StreamConfig sc = new StreamConfig();
        List<List<Integer>> streams = new ArrayList<>();
        streams.add(new ArrayList<>(Arrays.asList(1,2,3,4,5)));
        streams.add(new ArrayList<>(Arrays.asList(1,2,3,4,5)));
        streams.add(new ArrayList<>(Arrays.asList(1,2,7,4,5)));

        sc.streams = streams;

        sc.updateFitness(8, 1, 1);
        Assert.assertEquals(16, sc.getOverlapFitness(), 1);

    }
}
