package layout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import main.CreateFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateLayout {

	Element e;
	String pName;

	public CreateLayout(Element e, String pName) {
		this.e = e;
		this.pName = pName;
	}

	public void create() throws ParserConfigurationException, IOException,
			TransformerException {
		create(e);
	}

	private void create(Element e, String... ns)
			throws ParserConfigurationException, IOException,
			TransformerException {
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element e1 = (Element) n;
				String tn = e1.getTagName();
				if (tn.equals("page")) {
					String paName = e1.getAttribute("name");
					CreateLayoutXml clx = new CreateLayoutXml(e1);
					clx.create();
					CreateActivity ca = new CreateActivity(e1, pName, paName);
					ca.create();
				}

			}
		}

	}

	public static String getFileName(String name) {
		StringBuilder sb = new StringBuilder();
		Pattern p = Pattern.compile("[A-Z]");
		Matcher m = p.matcher(name);
		int s = 0;
		sb.append("activity_");
		while (m.find()) {
			int ns = m.start();
			sb.append(name.substring(s, ns).toLowerCase() + "_");
			s = ns;
		}
		sb.append(name.substring(s).toLowerCase());
		return sb.toString();
	}
}
