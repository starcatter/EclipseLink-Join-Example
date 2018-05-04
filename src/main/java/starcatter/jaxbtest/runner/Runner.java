package starcatter.jaxbtest.runner;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.xml.sax.*;
import starcatter.jaxbtest.ChildListType;
import starcatter.jaxbtest.ParentListType;
import starcatter.jaxbtest.UniverseType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.sun.org.apache.xerces.internal.jaxp.JAXPConstants.JAXP_SCHEMA_LANGUAGE;
import static com.sun.org.apache.xerces.internal.jaxp.JAXPConstants.JAXP_SCHEMA_SOURCE;
import static com.sun.org.apache.xerces.internal.jaxp.JAXPConstants.W3C_XML_SCHEMA;
import static java.lang.System.out;

public class Runner {
    public static void main(String args[]) throws JAXBException, IOException, ParserConfigurationException, SAXException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream iStream = classLoader.getResourceAsStream("metadata/xml-bindings.xml");

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, iStream);

        JAXBContext jaxbContext = JAXBContext.newInstance("starcatter.jaxbtest", classLoader, properties);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();


        testSeparate(jaxbUnmarshaller); // --- separate xml (doesn't join children)
        testUniverse(jaxbUnmarshaller); // --- all in one
    }

    public static void testUniverse(Unmarshaller jaxbUnmarshaller) throws SAXException, ParserConfigurationException, IOException, JAXBException {
        out.println("\n--- testUniverse");

        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        parserFactory.setXIncludeAware(true);
        parserFactory.setValidating(true);

        SAXParser saxParser = parserFactory.newSAXParser();
        saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);                         // set validation mode to xsd
        saxParser.setProperty(JAXP_SCHEMA_SOURCE, "src/main/resources/schema/universe.xsd"); // point to main xsd file

        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false); // prevent inserting xml-base attribute into x-included files

        File file = new File("src/main/resources/data/universe.xml");

        try(FileInputStream inputStream = new FileInputStream(file)){
            InputSource inputSource = new InputSource(inputStream);

            SAXSource saxSource = new SAXSource(xmlReader, inputSource);

            JAXBElement universeElem = (JAXBElement) jaxbUnmarshaller.unmarshal(saxSource);
            UniverseType universe = (UniverseType) universeElem.getValue();

            if(!(universe.getChildList().isEmpty() || universe.getParentList().isEmpty())){
                printResult(universe.getChildList().get(0), universe.getParentList().get(0));
            }
        }
    }

    public static void testSeparate(Unmarshaller jaxbUnmarshaller) throws JAXBException, IOException {
        out.println("\n--- testSeparate");

        File childFile = new File("src/main/resources/data/children.xml");
        File parFile = new File("src/main/resources/data/parents.xml");

        try(FileInputStream childFileStream = new FileInputStream(childFile); FileInputStream parFileStream = new FileInputStream(parFile) ){
            JAXBElement childElem = (JAXBElement) jaxbUnmarshaller.unmarshal(childFileStream);
            JAXBElement parElem = (JAXBElement) jaxbUnmarshaller.unmarshal(parFileStream);

            ChildListType childList = (ChildListType) childElem.getValue();
            ParentListType parentList = (ParentListType) parElem.getValue();

            printResult(childList, parentList);
        }
    }


    public static void printResult(ChildListType children, ParentListType parents) {
        children.getChild().forEach(out::println);

        parents.getParent().forEach(p -> {
            out.println("---");
            out.println("ParentName: " + p.getParentName());
            out.println("ChildId: " + p.getChildId());
        });
    }
}
