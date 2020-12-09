import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

public class XmlOutput {

    List<Stream> streams;

    public XmlOutput(List<Stream> streams){
        this.streams = streams;
    }

    public void writeXml(String path){
        try{
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("solution");
            document.appendChild(root);

            for(Stream stream : this.streams){
                String streamId = stream.getId();
                Element streamNode = document.createElement("stream");
                root.appendChild(streamNode);
                Attr xmlStreamId = document.createAttribute("id");
                xmlStreamId.setValue(streamId);
                streamNode.setAttributeNode(xmlStreamId);

                for(List<Link> route : stream.getChosenRoutes()){
                    Element routeNode = document.createElement("route");
                    streamNode.appendChild(routeNode);

                    for(Link link : route){
                        Element linkNode = document.createElement("link");
                        routeNode.appendChild(linkNode);

                        Attr linkSrc = document.createAttribute("src");
                        linkSrc.setValue(link.getSrc());

                        Attr linkDst = document.createAttribute("dst");
                        linkDst.setValue(link.getDest());

                        linkNode.setAttributeNode(linkSrc);
                        linkNode.setAttributeNode(linkDst);
                    }
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(path));
            transformer.transform(domSource, streamResult);

        } catch(Exception e){
            e.printStackTrace();
        }


    }

}
