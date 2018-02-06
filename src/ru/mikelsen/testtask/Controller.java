package ru.mikelsen.testtask;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

public class Controller {
    private Server server;

    @FXML
    private TextField address;

    @FXML
    private TextArea log;

    @FXML
    private TextField port;

    @FXML
    private Button initializeButton;

    @FXML
    public void initialize() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                Platform.runLater(() -> log.appendText(String.valueOf((char) b)));
            }
        }));
    }

    @FXML
    public void initializeButtonClicked(ActionEvent event) {
        try {
            server = new Server(parseXML());
            server.start();
            System.out.println("Server started.");
            initializeButton.setDisable(true);
        } catch (ParserConfigurationException | SAXException | IOException | NumberFormatException | NullPointerException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to http server: " + e.getMessage());
        }
    }

    private int parseXML() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new URL("http://" + address.getText() + ":" + port.getText() + "/config.xml").openStream());

        Node root = document.getDocumentElement();
        NodeList commands = root.getChildNodes();
        for (int i = 0; i < commands.getLength(); i++) {
            if (commands.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) commands.item(i);
                String value = element.getAttribute("value");
                String method = element.getAttribute("method");

                Commands.commands.put(Short.valueOf(value, 16), method);
            }
        }
        return Integer.parseInt(((Element) root).getAttribute("port"));
    }


}
