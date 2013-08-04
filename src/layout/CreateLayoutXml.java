package layout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import main.CreateFactory;
import main.MainGenerator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateLayoutXml {
	Element e;
	String pName;
	PrintWriter pw;
	Document document;
	HashMap<String, Integer> vns;
	{
		vns = new HashMap<String, Integer>();
		vns.put("TextView", 0);
		vns.put("EditText", 0);
		vns.put("Button", 0);
		vns.put("ListView", 0);
	}
	HashMap<String, String> rgb;
	{
		rgb = new HashMap<String, String>();
		rgb.put("red", "#FF0000");
		rgb.put("green", "#00FF00");
		rgb.put("blue", "#0000FF");
		rgb.put("black", "#000000");
		rgb.put("white", "#FFFFFF");
		rgb.put("yellow", "#FFFF00");
		rgb.put("", "");
	}
	String hBase;
	String vBase;
	boolean oriIsV = true;

	public CreateLayoutXml(Element e) throws ParserConfigurationException {
		this.e = e;
		this.pName = e.getAttribute("name");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		document = db.newDocument();
	}

	public void create() throws IOException, ParserConfigurationException,
			TransformerException {

		Element rl = document.createElement("RelativeLayout");
		rl.setAttribute("xmlns:android",
				"http://schemas.android.com/apk/res/android");
		rl.setAttribute("xmlns:tools", "http://schemas.android.com/tools");
		rl.setAttribute("android:layout_width", "match_parent");
		rl.setAttribute("android:layout_height", "match_parent");
		rl.setAttribute("tools:context",
				"." + CreateFactory.getActivityName(pName));
		create(e, rl);
		document.appendChild(rl);

		File f = new File(MainGenerator.OPATH + "/output/res/layout/"
				+ CreateLayout.getFileName(pName) + ".xml");
		if (!f.isFile())
			f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		StreamResult result = new StreamResult(fos);
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		transformer.setOutputProperty("encoding", "UTF-8");
		transformer.setOutputProperty("indent", "yes");
		DOMSource source = new DOMSource(document);
		transformer.transform(source, result);
		fos.close();
	}

	public void create(Element re, Element we) {
		NodeList nl = re.getChildNodes();
		int nll = nl.getLength();
		for (int i = 0; i < nll; i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element e1 = (Element) n;
				String tn = e1.getTagName();
				if (tn.equals("button")) {
					addButton(e1, we);
				} else if (tn.equals("text")) {
					addText(e1, we);
				} else if (tn.equals("edit")) {
					addEdit(e1, we);
				} else if (tn.equals("list")) {
					addList(e1, we);
				} else if (tn.equals("composite")) {
					if (e1.getAttribute("alignment").equals("horizontal")) {
						oriIsV = false;
						create(e1, we);
						oriIsV = true;
					}
				}
			}
		}
	}

	public void addButton(Element re, Element we) {

		int bN = vns.get("Button");
		String id = "button" + bN;
		Element b = document.createElement("Button");
		b.setAttribute("android:id", "@+id/" + id);
		b.setAttribute("android:layout_width", "wrap_content");
		b.setAttribute("android:layout_height", "wrap_content");

		NodeList nl = re.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ce = (Element) nl.item(i);
				String ceName = ce.getTagName();
				if (ceName.equals("button-text")) {
					String tCon = ce.getTextContent();
					String tSize = ce.getAttribute("size");
					String tCol = rgb.get(ce.getAttribute("color"));
					b.setAttribute("android:textColor", tCol);
					b.setAttribute("android:text", tCon.trim());
					b.setAttribute("android:textSize", tSize + "sp");
				}
			}
		}

		setAlign(b, id);
		we.appendChild(b);
		vns.put("Button", bN + 1);

	}

	public void addText(Element re, Element we) {

		int tN = vns.get("TextView");
		String id = "textView" + tN;
		String tCon = re.getTextContent();
		String tSize = re.getAttribute("size");
		String tCol = rgb.get(re.getAttribute("color"));
		Element t = document.createElement("TextView");
		t.setAttribute("android:id", "@+id/" + id);
		t.setAttribute("android:layout_width", "wrap_content");
		t.setAttribute("android:layout_height", "wrap_content");
		t.setAttribute("android:textColor", tCol);
		t.setAttribute("android:text", tCon);
		t.setAttribute("android:textSize", tSize + "sp");
		setAlign(t, id);
		we.appendChild(t);
		vns.put("TextView", tN + 1);

	}

	public void addEdit(Element re, Element we) {

		int eN = vns.get("EditText");
		String id = "editText" + eN;
		String tCon = re.getTextContent();
		String tSize = re.getAttribute("size");
		String tCol = rgb.get(re.getAttribute("color"));
		Element e = document.createElement("EditText");
		e.setAttribute("android:id", "@+id/" + id);
		e.setAttribute("android:layout_width", "wrap_content");
		e.setAttribute("android:layout_height", "wrap_content");
		e.setAttribute("android:textColor", tCol);
		e.setAttribute("android:text", tCon);
		e.setAttribute("android:textSize", tSize + "sp");
		setAlign(e, id);
		we.appendChild(e);
		vns.put("EditText", eN + 1);

	}

	public void addList(Element re, Element we) {
		int lN = vns.get("ListView");
		String id = "listView" + lN;
		Element lv = document.createElement("ListView");
		lv.setAttribute("android:id", "@+id/" + id);
		lv.setAttribute("android:layout_width", "match_parent");
		lv.setAttribute("android:layout_height", "wrap_content");
		setAlign(lv, id);
		we.appendChild(lv);
		vns.put("ListView", lN + 1);
	}

	public void setAlign(Element e, String id) {
		if (oriIsV) {
			if (vBase != null) {
				e.setAttribute("android:layout_below", "@+id/" + vBase);
			}
			vBase = id;
		} else {
			if (hBase != null) {
				e.setAttribute("android:layout_toRightOf", "@+id/" + hBase);
			}
			if (vBase == null) {
				vBase = id;
			}
			hBase = id;
		}
	}
}
