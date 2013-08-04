package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateManifest {

	Element e;
	String pName;
	PrintWriter pw;
	Document document;

	public CreateManifest(Element e, String pName)
			throws ParserConfigurationException {
		this.e = e;
		this.pName = pName;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		document = db.newDocument();
	}

	public void create() throws IOException, ParserConfigurationException,
			TransformerException {

		Element rl = document.createElement("manifest");
		rl.setAttribute("xmlns:android",
				"http://schemas.android.com/apk/res/android");
		rl.setAttribute("package", pName);
		rl.setAttribute("android:versionCode", "1");
		rl.setAttribute("android:versionName", "1.0");
		// addSdk
		addUsesSdk(rl);
		// addPermission
		addPermission(rl);
		// add application
		addApplication(rl);
		document.appendChild(rl);

		File f = new File(MainGenerator.OPATH + "/output/AndroidManifest.xml");
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

	public void addUsesSdk(Element rl) {
		Element cl = document.createElement("uses-sdk");
		cl.setAttribute("android:minSdkVersion", "15");
		cl.setAttribute("android:targetSdkVersion", "17");
		rl.appendChild(cl);
	}

	public void addPermission(Element rl) {
		Element cl = document.createElement("uses-permission");
		cl.setAttribute("android:name", "android.permission.BLUETOOTH");
		rl.appendChild(cl);
	}

	public void addApplication(Element rl) {
		Element cl = document.createElement("application");
		cl.setAttribute("android:allowBackup", "true");
		// add service
		// add provider
		Element cl1 = document.createElement("provider");
		cl1.setAttribute("android:name", "DatabaseContentProvider");
		cl1.setAttribute("android:authorities",
				"lib.database.databasecontentprovider");
		cl.appendChild(cl1);
		// add activity
		NodeList nl = e.getChildNodes();
		int nll = nl.getLength();
		for (int i = 0; i < nll; i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				addActivity((Element) n, cl);
			}
		}
		rl.appendChild(cl);
	}

	// e1:reading node cl:creating node
	public void addActivity(Element e1, Element cl) {
		String tn = e1.getTagName();
		if (tn.equals("page")) {
			Element cl1 = document.createElement("activity");
			String name = e1.getAttribute("name");
			cl1.setAttribute("android:name",
					pName + "." + CreateFactory.getActivityName(name));
			cl.appendChild(cl1);
			NodeList nl = e1.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n instanceof Element) {
					if (((Element) n).getTagName().equals("first-page")) {
						Element ife = document.createElement("intent-filter");
						Element ifec = document.createElement("action");
						ifec.setAttribute("android:name",
								"android.intent.action.MAIN");
						Element ifec2 = document.createElement("category");
						ifec2.setAttribute("android:name",
								"android.intent.category.LAUNCHER");
						ife.appendChild(ifec);
						ife.appendChild(ifec2);
						cl1.appendChild(ife);
					}
				}
			}
		}
	}
}
