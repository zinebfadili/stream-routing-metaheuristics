package Common;

import Common.Configuration;
import Common.Link;
import Common.Parser;

public class ParserTest {

    public static void main(String[] args) {
    Configuration config = Parser.createConfigFromXML(".\\input\\TC0_example.app_network_description");
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
            System.out.println(stream.toString());

        }

    }
}
