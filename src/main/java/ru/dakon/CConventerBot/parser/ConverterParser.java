package ru.dakon.CConventerBot.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.dakon.CConventerBot.exception.RubParsingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@Slf4j
@Component
public class ConverterParser implements Parser{
    @Override
    public Double parse(String priceAsString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Double value = null;
        try {
            builder = factory.newDocumentBuilder();
            var reader = new StringReader(priceAsString);
            Document doc = builder.parse(new InputSource(reader));

            doc.getDocumentElement().normalize();
            NodeList list = doc.getElementsByTagName("rates");

            for (var rowIdx = 0; rowIdx < list.getLength(); rowIdx++) {
                var node = list.item(rowIdx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    var element = (Element) node;
                    value = Double.parseDouble(element.getElementsByTagName("RUB").item(0).getTextContent());

                }
            }
            log.info("todays price is  " + value); // for delete
            return value;
        } catch (Exception e) {
            log.error("xml parsing error, xml:{}", priceAsString, e);
            throw new RubParsingException(e);
        }
    }

}
