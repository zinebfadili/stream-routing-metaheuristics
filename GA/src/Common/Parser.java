package Common;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import Common.Configuration;
import Common.Link;
import org.w3c.dom.*;

public class Parser {
    /**
     * createTasksFromXml method that allows us to create the different task objects from the provided xml file
     * @param pathToXml path to the file that contains the environment description
     * @return an ArrayList with the tasks of the xml document
     */
    public static Configuration createConfigFromXML(String pathToXml){
        // the list of tasks that are described in the xml file
        Configuration configuration = null;
        try {
            String[] deviceNames = {};
            ArrayList<Link> links = new ArrayList<>();
            double[][] linkMatrix = null;
            ArrayList<Stream> streamList = new ArrayList<Stream>();
            // we open the xml file with the data
            File fXmlFile = new File(pathToXml);

            // we create the objects needed for the parser
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            // we get all the Task nodes
            NodeList devices = doc.getElementsByTagName("device");
            int n = devices.getLength();
            deviceNames = new String[n];
            linkMatrix = new double[n][n];
            NodeList linkNodes = doc.getElementsByTagName("link");
            NodeList streamNodes = doc.getElementsByTagName("stream");
            // we loop through the tasks
            for (int i = 0; i < devices.getLength(); i++){
                Element device = (Element)devices.item(i);
                String deviceName = device.getAttribute("name");
                int deviceId = i;
                String type = device.getAttribute("type");
                //on diagonale of linkMatrix set -1 if link is endsystem otherwise 0
                double type_no = 0;
                if(type.equals("EndSystem"))
                    type_no = -1;
                linkMatrix[deviceId][deviceId] =type_no;
                deviceNames[deviceId] = deviceName;
                // we create the task objects using the values inside the xml
                /*
                Task tsk = new Task(Integer.parseInt(e.getAttribute("Id")),
                        Integer.parseInt(e.getAttribute("WCET")),
                        Long.parseLong(e.getAttribute("Deadline")),
                        Long.parseLong(e.getAttribute("Period"))
                );*/
                // we add the task to the list of tasks
            }
            for (int i = 0; i < linkNodes.getLength(); i++) {
                Element linkNode = (Element)linkNodes.item(i);
                //src dst speed
                String src = linkNode.getAttribute("src");
                String dst = linkNode.getAttribute("dest");
                double speed = Double.parseDouble(linkNode.getAttribute("speed"));
                Link link = new Link(src,dst,speed);
                links.add(link);
                linkMatrix[getIdFromName(src,deviceNames)][getIdFromName(dst,deviceNames)] = speed;
            }
            for (int i = 0; i < streamNodes.getLength(); i++) {
                Element strEl = (Element) streamNodes.item(i);
                Stream stream = new Stream(strEl.getAttribute("id"),
                        strEl.getAttribute("src"),
                        strEl.getAttribute("dest"),
                        Double.parseDouble(strEl.getAttribute("size")),
                        Double.parseDouble(strEl.getAttribute("period")),
                        Double.parseDouble(strEl.getAttribute("deadline")),
                        Integer.parseInt(strEl.getAttribute("rl")));
                streamList.add(stream);
            }
            configuration = new Configuration(deviceNames,linkMatrix,links,streamList);
        } catch (Exception e){
            e.printStackTrace();
        }
        return configuration;
    }

    private static int getIdFromName(String deviceName,String[] devices) {
        for (int i = 0; i < devices.length; i++) {
            if(deviceName.equals(devices[i]))
                return i;
        }
        return -1;
    }


    public static void writeOutputToXML(Configuration config,String pathToXML) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("Solution");
            document.appendChild(root);

            // we loop through the different streams
            for (Stream stream : config.getStreamList()) {
                String streamId = stream.getId();
                Element strNode = document.createElement("Stream");
                root.appendChild(strNode);
                strNode.setAttribute("id", streamId);
                // we loop through the different routes of the stream
                int i = 0;
                double wcd = stream.getStreamWCD();
                for (ArrayList<Link> route : stream.getRoutes()) {

                    i++;
                    Element routeNode = document.createElement("Route");
                    strNode.appendChild(routeNode);
                    // we finally loop over the tasks of the core
                    for (Link link : route) {

                        // creating the task node
                        Element linkNode = document.createElement("Link");
                        linkNode.setAttribute("src", link.getSrc());
                        linkNode.setAttribute("dest", link.getDest());
                        routeNode.appendChild(linkNode);

                    }
                }
                strNode.setAttribute("WCD", String.valueOf(wcd));
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(pathToXML));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, streamResult);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        public static Configuration updateConfigFromSolutionXML(String pathToXml, Configuration config){
        // the list of tasks that are described in the xml file
        try {
            String[] deviceNames = {};
            ArrayList<Link> links = new ArrayList<>();
            double[][] linkMatrix = null;
            ArrayList<Stream> streamList = new ArrayList<Stream>();
            // we open the xml file with the data
            File fXmlFile = new File(pathToXml);

            // we create the objects needed for the parser
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList streamNodes = doc.getElementsByTagName("Stream");
            for (int i = 0; i < streamNodes.getLength(); i++) {
                Element strEl = (Element) streamNodes.item(i);
                String id = strEl.getAttribute("id");
                System.out.println(id);
                //Stream s = config.getStream(id);
                List<ArrayList<Link>> routeList = new ArrayList<>();
                NodeList routeNodes = strEl.getElementsByTagName("Route");
                for (int j = 0; j < routeNodes.getLength(); j++) {
                    Element routeEl = (Element) streamNodes.item(j);
                    ArrayList<Link> route = new ArrayList<>();
                    NodeList linkNodes = routeEl.getElementsByTagName("Link");
                    for (int k = 0; k < linkNodes.getLength(); k++) {
                        Element linkEl = (Element) linkNodes.item(k);
                        Link link = config.findLinkBySrcAndDest(linkEl.getAttribute("src"),linkEl.getAttribute("dest"));
                        route.add(link);
                    }
                    routeList.add(route);
                }
                config.setRoutesForStream_V2(id,routeList);
                //streamList.add(stream);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return config;
    }

}