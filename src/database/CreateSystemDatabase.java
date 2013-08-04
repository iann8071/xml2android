package database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import main.MainGenerator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateSystemDatabase {

	Element e;
	Element r;
	Element dbe;
	Element te;
	Element ie;
	Element re;
	Document document;

	public CreateSystemDatabase(Element e) throws ParserConfigurationException,
			IOException {
		this.e = e;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		document = db.newDocument();
	}

	public void create() throws IOException, TransformerException {
		r = document.createElement("databases");
		create(e);
		File f = new File(MainGenerator.OPATH
				+ "/output/assets/create_sys_db.xml");
		if (!f.isFile())
			f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		StreamResult result = new StreamResult(fos);
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		transformer.setOutputProperty("encoding", "UTF-8");
		transformer.setOutputProperty("indent", "yes");
		document.appendChild(r);
		DOMSource source = new DOMSource(document);
		transformer.transform(source, result);
		fos.close();
	}

	private void create(Element e) throws IOException, TransformerException {
		NodeList nl = e.getChildNodes();
		int nll = nl.getLength();
		for (int i = 0; i < nll; i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element e1 = (Element) n;
				String tn = e1.getTagName();
				if (tn.equals("database")) {
					dbe = document.createElement("database");
					dbe.setAttribute("name", e1.getAttribute("name"));
					r.appendChild(dbe);
				} else if (tn.equals("table")) {
					te = document.createElement("table");
					te.setAttribute("name", e1.getAttribute("name"));
					dbe.appendChild(te);
				} else if (tn.equals("index")) {
					re = null;
					ie = document.createElement("index");
					ie.setAttribute("name", e1.getAttribute("name"));
					te.appendChild(ie);
				} else if (tn.equals("record")) {
					re = document.createElement("record");
					re.setAttribute("name", e1.getAttribute("name"));
					ie.appendChild(re);
				} else if (tn.equals("item")) {
					Element ite = document.createElement("item");
					ite.setAttribute("name", e1.getAttribute("name"));
					if (re == null) {
						ie.appendChild(ite);
					} else {
						re.appendChild(ite);
					}
				}
				create(e1);
			}
		}
	}
}
