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

public class XMLPaC extends DefaultHandler {
	private String dbName;
	private String tName;
	private ArrayList<ContentValues> fs;
	protected static final String PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	public static final String AUTHORITY = "lib.database.databasecontentprovider";
	public static final String PBASE = "/databases";
	String uSB;
	private final InputSource is;
	private final ContentProvider cp;

	public XMLPaC(Context c, InputSource is) {
		this.is = is;
		uSB = "content://" + AUTHORITY + PBASE;
		Uri u = Uri.parse(uSB);
		cp = c.getContentResolver().acquireContentProviderClient(u)
				.getLocalContentProvider();
	}

	public void startDocument() {
		System.out.println("StartDocument");
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		System.out.println("startElement");
		try {

			if (qName.equals("database")) {
				dbName = attributes.getValue("name");
				Bundle sb = new Bundle();
				sb.putParcelable("uri", Uri.parse(uSB + "/" + dbName));
				cp.call("createDatabase", null, sb);
				System.out.println("database");
			} else if (qName.equals("table")) {
				// table cannot be created until fields appear
				fs = new ArrayList<ContentValues>();
				tName = attributes.getValue("name");
			} else if (qName.equals("field")) {
				ContentValues cv = new ContentValues();
				cv.put("name", attributes.getValue("name"));
				cv.put("type", attributes.getValue("type"));
				fs.add(cv);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void characters(char[] ch, int offset, int length) {
		try {
			String value = new String(ch, offset, length);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void endElement(String uri, String localName, String qName) {
		try {

			if (qName.equals("table")) {
				Bundle sb = new Bundle();
				sb.putParcelable("uri",
						Uri.parse(uSB + "/" + dbName + "/" + tName));
				sb.putParcelableArrayList("fs", fs);
				cp.call("createTable", null, sb);
			} else if (qName.equals("_")) {

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
