package database;

import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

public class XMLPaCForSys extends DefaultHandler {

	protected static final String PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	public static final String AUTHORITY = "lib.database.databasecontentprovider";
	public static final String PBASE = "/databases";
	String uSB;
	String uS;
	String uSForC;
	private final InputSource is;
	private final ContentProvider cp;
	private ArrayList<ContentValues> fs = new ArrayList<ContentValues>();
	private final ArrayList<String> cfns;
	{
		cfns = new ArrayList<String>();
		cfns.add("database");
		cfns.add("table");
		cfns.add("field");
	}
	private final ArrayList<String> tfns;
	{
		tfns = new ArrayList<String>();
		tfns.add("database");
		tfns.add("name");
		tfns.add("time");
	}

	private String dbName;
	private String tName;
	private String fName;
	private ContentValues r;

	public XMLPaCForSys(Context c, InputSource is) {
		this.is = is;
		uSB = "content://" + AUTHORITY + PBASE;
		Uri u = Uri.parse(uSB);
		cp = c.getContentResolver().acquireContentProviderClient(u)
				.getLocalContentProvider();
	}

	public void startDocument() {
		System.out.println("StartDocument");
		// create sys_db
		uS = uSB + "/sys_db";
		Uri u = Uri.parse(uS);
		Bundle sb = new Bundle();
		sb.putParcelable("uri", u);
		cp.call("createDatabase", null, sb);
		// create sys_page_index_t
		uSForC = uS + "/client_t";
		u = Uri.parse(uSForC);
		for (String fn : cfns) {
			ContentValues cv = new ContentValues();
			cv.put("name", fn);
			cv.put("type", "TEXT");
			fs.add(cv);
		}
		sb = new Bundle();
		sb.putParcelable("uri", u);
		sb.putParcelableArrayList("fs", fs);
		cp.call("createTable", null, sb);
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		System.out.println("startElement");
		try {

			if (qName.equals("database")) {
				dbName = attributes.getValue("name");
			} else if (qName.equals("table")) {
				// table cannot be created until fields appear
				tName = attributes.getValue("name");
			} else if (qName.equals("field")) {
				fName = attributes.getValue("name");
			} else if (qName.equals("authentication")) {
				r = new ContentValues();
				Uri u;
				// insert record
				r.put("database", dbName);
				r.put("table", tName);
				r.put("field", fName);
				u = Uri.parse(uSForC);
				cp.insert(u, r);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void characters(char[] ch, int offset, int length) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void endElement(String uri, String localName, String qName) {

	}

	public void endDocument() {
		System.out.println("EndDocument");
	}

	public void start() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(is, this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
