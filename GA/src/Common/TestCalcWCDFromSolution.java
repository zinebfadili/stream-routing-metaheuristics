package Common;

public class TestCalcWCDFromSolution {
    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        String input_name = "TC5_large1.";
        Configuration config = Parser.createConfigFromXML("./input/" + input_name +
                "app_network_description");
        /*
        for (Link link:
                config.getLinkList()) {
            System.out.println("Src: " + link.getSrc() + " , dest:"+link.getDest() + " with speed " + link.getSpeed() );
        }
        for (int i = 0; i < config.getDeviceNames().length; i++) {
            for (int j = 0; j < config.getDeviceNames().length; j++) {

                System.out.print(config.getLinkMatrix()[i][j] + " ");
            }
            System.out.println();
        }
        for (Stream stream:
                config.getStreamList()) {
            System.out.println(stream.getId());

        }
*/
        Parser.updateConfigFromSolutionXML("./output/"+input_name+"solution.xml",config);
        config.calculateWCD();
        double avg_wcd = config.calculateAvgWCD();
        System.out.println("Average wcd is: " + avg_wcd);
    }
}
