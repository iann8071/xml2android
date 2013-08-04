package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import layout.CreateLayout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import database.CreateDatabase;
import database.CreateSystemDatabase;

public class MainGenerator {

	public static final String OPATH = "C:/Users/Kiichi/xml2android";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fName = args[0];
		String pName = args[1];
		try {
			MainGenerator mg = new MainGenerator();
			File f = new File(OPATH + "/src/input/" + fName);
			DocumentBuilder builder = null;
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

			Document document = null;
			try {
				document = builder.parse(f);
				Element r = document.getDocumentElement();
				mg.createProject(pName);
				mg.create(r, pName);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createProject(String pName) throws IOException {
		final LinkedList<String> pths = new LinkedList<String>();
		pths.add(OPATH + "/output");
		pths.add(OPATH + "/output/src");
		LinkedList<String> fns = new LinkedList<String>();
		Scanner sc = new Scanner(pName);
		sc.useDelimiter("\\.");
		while (sc.hasNext()) {
			fns.add(sc.next());
		}
		sc.close();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fns.size(); i++) {
			sb.append("/" + fns.get(i));
			pths.add(OPATH + "/output/src" + sb.toString());
		}
		pths.add(OPATH + "/output/assets");
		pths.add(OPATH + "/output/libs");
		pths.add(OPATH + "/output/res");
		pths.add(OPATH + "/output/res/layout");

		for (int i = 0; i < pths.size(); i++) {
			File f = new File(pths.get(i));
			if (!f.isDirectory())
				f.mkdir();
		}

		copy(OPATH + "/src/database/xml2androidlibs.jar", OPATH
				+ "/output/libs/xml2androidlibs.jar");
	}

	public void create(Element e, String pName)
			throws ParserConfigurationException, IOException,
			TransformerException {
		NodeList nl = e.getChildNodes();
		int nll = nl.getLength();
		for (int i = 0; i < nll; i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element e1 = (Element) n;
				String tn = (e1.getTagName());
				if (tn.equals("databases")) {
					CreateDatabase cd = new CreateDatabase(e1);
					cd.create();
				} else if (tn.equals("pages")) {
					CreateManifest cm = new CreateManifest(e1, pName);
					cm.create();
					CreateLayout cl = new CreateLayout(e1, pName);
					cl.create();
				}
			}
		}
	}

	public static void copy(String srcPath, String destPath) throws IOException {

		FileChannel srcChannel = new FileInputStream(srcPath).getChannel();
		FileChannel destChannel = new FileOutputStream(destPath).getChannel();
		try {
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		} finally {
			srcChannel.close();
			destChannel.close();
		}

	}

}
