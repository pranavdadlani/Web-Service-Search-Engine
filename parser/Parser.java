/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import com.sun.jndi.toolkit.url.Uri;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Parin
 */
public class Parser {

    /**
     * @param args the command line arguments
     */
    
    public static ArrayList<String> str = new ArrayList<String>();
    public static ArrayList<String> fileList = new ArrayList<String>();
    public static ArrayList<String> documentList = new ArrayList<String>();

    @SuppressWarnings("empty-statement")
    public static void parse() throws RemoteException, ParserConfigurationException, IOException, SAXException, TransformerConfigurationException, TransformerException {
        // TODO code application logic here
        System.out.println("Parsing....");
        Scanner file = new Scanner(new File("src/stopword/stop.txt"));
        ArrayList<String> stopwords = new ArrayList<String>();

        while (file.hasNext()) {
            stopwords.add(file.next());
        }
        String fileName = "src/wsdlFiles/wsdlFiles.txt";
        try {

            //Create object of FileReader
            FileReader inputFile = new FileReader(fileName);

            //Instantiate the BufferedReader Class
            BufferedReader bufferReader = new BufferedReader(inputFile);

            //Variable to hold the one line data
            String line;

            // Read file line by line and print on the console
            while ((line = bufferReader.readLine()) != null) {
                fileList.add(line);
            }
            
            //Close the buffer reader
            bufferReader.close();
        } catch (Exception e) {
            //   System.out.println("Error while reading file line by line:" + e.getMessage());
        }

        for (int index = 0; index < fileList.size(); index++) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fileList.get(index));
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("operation");
            if (nList.getLength() == 0) {
                nList = doc.getElementsByTagName("wsdl:operation");
            }

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                NodeList tempNodeList = nList.item(temp).getChildNodes();
                for (int k = 0; k < tempNodeList.getLength(); k++) {

                    Node msg1Node = tempNodeList.item(k);

                    if (msg1Node.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement1 = (Element) msg1Node;
                        // System.out.println(eElement);
                        if (eElement1.getTagName() == "wsdl:documentation") {
                            //     System.out.println( getNodeValue(nNode.getChildNodes().item(1)));
                            String document = getNodeValue(nNode.getChildNodes().item(1));
                            conversionForDocument(document);
                        }
                        if (eElement1.getAttribute("message").toString() != "") {
                            String[] tempArray = eElement1.getAttribute("message").toString().split(":");

                            if (tempArray[1] != null) {
                                conversion(tempArray[1]);

                            }
                        }
                    }
                }

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String s = eElement.getAttribute("name").toString();
                    conversion(s);
                }
            }
            makeFiles(index);
            str.clear();
            documentList.clear();
        }

        for (int index = 0; index < fileList.size(); index++) {
            ArrayList<String> actual = new ArrayList<String>();
            String filename = "src/wsdlpre/"+String.valueOf(index) + ".txt";
            Scanner file1 = new Scanner(new File(filename));
            
            while (file1.hasNext()) {
                String trm = file1.next();
                actual.add(trm.toLowerCase());
            }
            
            file1.close();
            actual.removeAll(stopwords);

            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new FileOutputStream("src/wsdl/" + index + ".txt"));
                for (int j = 0; j < actual.size(); j++) {
                    String act = actual.get(j).replace("'", "");
                    pw.println(act);
                }
                pw.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            }
            actual.clear();
           

        }

    }

    public static void conversion(String s) {
        char arr[] = s.toCharArray();
        String sb = new String();
        sb += (arr[0]);
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] >= 'A' && arr[i] <= 'Z') {
                str.add(sb);
                sb = new String();
                sb += arr[i];
                //sb=new StringBuffer();
            } else if (arr[i] >= 'a' && arr[i] <= 'z') {
                if (i == arr.length - 1) {
                    sb += arr[i];
                    str.add(sb);
                } else {
                    sb += (arr[i]);
                }
            }
        }

    }

    private static String getNodeValue(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int x = 0; x < childNodes.getLength(); x++) {
            Node data = childNodes.item(x);
            if (data.getNodeType() == Node.TEXT_NODE) {
                return data.getNodeValue();
            }
        }
        return "";
    }

    private static void makeFiles(int index) {
            PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream("src/wsdlpre/" + index + ".txt"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
            pw.println(fileList.get(index));
            for (int i = 0; i < str.size(); i++) {

                pw.println(str.get(i));
            }
            for (int i = 0; i < documentList.size(); i++) {
                pw.println(documentList.get(i));
            }

            pw.close();

    }

    private static void conversionForDocument(String document) {
        String[] tempDocumentArray = document.trim().split(" ");
        for (int index = 0; index < tempDocumentArray.length; index++) {
            documentList.add(tempDocumentArray[index]);

        }

    }
}
