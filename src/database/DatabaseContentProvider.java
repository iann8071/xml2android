package database;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

//given that databases have already created

public class DatabaseContentProvider extends ContentProvider {

	private static final UriMatcher sURIMatcher;
	private static final int DATABASES = 1;
	private static final int DATABASE_ID = 2;
	private static final int TABLE_ID = 3;
	public static final String AUTHORITY = "lib.database.databasecontentprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/");
	public static final String PBASE = "/databases";
	private static final HashMap<String, DatabaseHelper> mDBs;
	private static final int DATABASE_VERSION = 1;
	private static final String pName = "mine.database5";
	public static final String dbPath = "/data/data/" + pName + "/databases/";
	public static final String xPath = "/data/data/" + pName + "/xmls/";
	private static final HashMap<String, String> typeMap = new HashMap<String, String>();
	private static final ArrayList<String> sqSysWord = new ArrayList<String>();
	private static final ArrayList<String> sysDb = new ArrayList<String>();
	private static final ArrayList<String> sysT = new ArrayList<String>();

	static {
		sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sURIMatcher.addURI(DatabaseContentProvider.AUTHORITY, "databases",
				DATABASES);
		sURIMatcher.addURI(DatabaseContentProvider.AUTHORITY, "databases/*",
				DATABASE_ID);
		sURIMatcher.addURI(DatabaseContentProvider.AUTHORITY, "databases/*/*",
				TABLE_ID);
		mDBs = new HashMap<String, DatabaseHelper>();

		typeMap.put("TEXT", "TEXT");
		typeMap.put("REAL", "REAL");
		typeMap.put("INTEGER", "INTEGER");
		typeMap.put("NULL", "NULL");
		typeMap.put("BLOB", "BLOB");

		sqSysWord.add("group");
		sqSysWord.add("table");
		sqSysWord.add("add");
		sqSysWord.add("create");
		sqSysWord.add("delete");
		sqSysWord.add("insert");
		sqSysWord.add("update");
		sqSysWord.add("index");
		sqSysWord.add("unique");

		sysDb.add("sys_db");
		sysT.add("android_metadata");
	}

	@Override
	public Bundle call(String m, String args, Bundle sb) {

		Uri u = sb.getParcelable("uri");
		Bundle rb = null;

		if (m.equals("getEntities")) {
			rb = getEntities(u);
		} else if (m.equals("inited")) {
			rb = new Bundle();
			rb.putBoolean("result", inited());
		} else if (m.equals("lock")) {
			lock(u);
		} else if (m.equals("unlock")) {
			unlock(u);
		} else if (m.equals("createXml")) {
			String xCon = sb.getString("xCon");
			createXml(u, xCon);
		} else if (m.equals("getXml")) {
			String xCon = getXml(u);
			rb = new Bundle();
			rb.putString("xCon", xCon);
		} else if (m.equals("createTable")) {
			ArrayList<ContentValues> fs = sb.getParcelableArrayList("fs");
			ArrayList<String> unis = sb.getStringArrayList("unis");
			createTable(u, fs, unis);
		} else if (m.equals("createDatabase")) {
			rb = new Bundle();
			rb.putParcelable("uri", createDatabase(u));
		} else if (m.equals("deleteGroup")) {
			deleteGroup(u);
		} else {
			String xCons = sb.getString("xCons");
			divideXCons(xCons);
		}

		return rb;
	}

	private boolean inited() {
		boolean res = false;
		String path = getContext().getFilesDir().getAbsolutePath();
		path = path.substring(0, path.lastIndexOf("files")) + "databases";
		File f = new File(path);
		for (String fn : f.list()) {
			if (isSysDb(fn)) {
				res = true;
				break;
			}
		}
		return res;
	}

	private HashMap<String, String> divideXCons(String xCons) {
		HashMap<String, String> dNaC = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new StringReader(xCons));
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document d = db.parse(new InputSource(br));
			Node n = d.getFirstChild();
			NodeList nl = n.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				n = nl.item(i);
				NamedNodeMap nnm = n.getAttributes();
				String dbName = nnm.getNamedItem("name").getTextContent();
				dNaC.put(dbName, n.getTextContent());
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dNaC;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		DatabaseHelper dh = getDBHelper(uri);
		List<String> ss = uri.getPathSegments();
		selection = getFormarizedS(selection);
		selectionArgs = getFormarizedSA(selectionArgs);
		int affected = -1;
		if (dh != null) {
			switch (ss.size()) {
			case DATABASE_ID:
				break;
			case TABLE_ID:
				SQLiteDatabase db = dh.getWritableDatabase();
				String tName = esSysW(ss.get(TABLE_ID - 1));
				affected = db.delete(tName, rSysW(selection), selectionArgs);
				getContext().getContentResolver().notifyChange(uri, null);
				Log.d("TAG", "Deleted");
				break;
			}
		}
		return affected;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// TODO Auto-generated method stub

		ArrayList<String> fs = getEntities(uri).getStringArrayList("table");
		ContentValues values = new ContentValues();
		for (int i = 0; i < fs.size(); i++) {
			values.put(fs.get(i), "");
		}

		boolean s = false;
		DatabaseHelper dh = getDBHelper(uri);
		List<String> ss = uri.getPathSegments();
		if (dh != null) {
			switch (ss.size()) {
			case DATABASE_ID:
				break;
			case TABLE_ID:
				SQLiteDatabase db = dh.getWritableDatabase();
				db.beginTransaction();
				String tName = esSysW(ss.get(TABLE_ID - 1));
				values = getFormarizedCV(values);
				try {
					if (!isSysT(tName)) {
						long lT = System.currentTimeMillis();
						values.put("time", Long.valueOf(lT).toString());

						int mrid = 1;
						Cursor c = query(uri, new String[] { "rid" }, null,
								null, "rid desc");
						if (c.moveToFirst()) {
							mrid = c.getInt(0) + 1;
						}

						values.put("rid", mrid);
					}

					long rowId = db.insert(tName, null, values);
					if (rowId > 0) {
						Uri tUri = ContentUris.withAppendedId(uri, rowId);
						getContext().getContentResolver().notifyChange(tUri,
								null);
						db.setTransactionSuccessful();
						Log.d("TAG", "Inserted");
						s = true;
					}

				} catch (SQLiteException e) {
					e.printStackTrace();
				} finally {
					db.endTransaction();
				}
				// db.close();
			}
		}

		if (s)
			return uri;
		else
			return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		getContext().getContentResolver().notifyChange(uri, null);

		DatabaseHelper dh = getDBHelper(uri);
		List<String> ss = uri.getPathSegments();
		int affected = -1;
		if (dh != null) {
			switch (ss.size()) {
			case DATABASE_ID:
				break;
			case TABLE_ID:
				SQLiteDatabase db = dh.getWritableDatabase();
				db.beginTransaction();
				String tName = esSysW(ss.get(TABLE_ID - 1));
				values = getFormarizedCV(values);
				try {
					long lT = System.currentTimeMillis();
					if (!isSysT(tName)) {
						if (values.getAsString("time") == null)
							values.put("time", Long.valueOf(lT).toString());

						if (values.get("sender") == null) {
							values.put("sender", "");
						}
					}

					affected = db.update(tName, values, rSysW(selection),
							selectionArgs);
					getContext().getContentResolver().notifyChange(uri, null);
					Log.d("TAG", "Updated");
					db.setTransactionSuccessful();
				} catch (SQLiteException e) {
					e.printStackTrace();
				} finally {
					db.endTransaction();
				}
				break;
			}
		}
		return affected;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub

		DatabaseHelper dh = getDBHelper(uri);
		List<String> ss = uri.getPathSegments();
		Cursor c = null;
		projection = getFormarizedSA(projection);
		selection = getFormarizedS(selection);
		selectionArgs = getFormarizedSA(selectionArgs);
		sortOrder = getFormarizedS(sortOrder);

		if (dh != null) {
			SQLiteDatabase db = dh.getReadableDatabase();
			db.beginTransactionNonExclusive();
			try {
				switch (ss.size()) {
				case DATABASE_ID:
					c = db.query("sqlite_master", new String[] { "name" },
							"type=?", new String[] { "table" }, null, null,
							null);
					break;
				case TABLE_ID:
					String tName = esSysW(ss.get(TABLE_ID - 1));
					if (selection == null) {
						c = db.query(tName, projection, null, null, null, null,
								sortOrder);
					} else {
						if (selection.startsWith("DISTINCT ")) {
							selection = selection.substring("DISTINCT "
									.length());
							if (selection.length() == 0)
								selection = null;
							c = db.query(true, tName, projection, selection,
									selectionArgs, null, null, sortOrder, null);
						} else {
							c = db.query(tName, projection, selection,
									selectionArgs, null, null, sortOrder);
						}
					}
				}
				db.setTransactionSuccessful();
			} catch (SQLiteException e) {
				e.printStackTrace();
				String es = e.toString();
				Log.i("TAG", es);
			} finally {
				db.endTransaction();
			}

		}

		return c;
	}

	private Bundle getEntities(Uri uri) {
		DatabaseHelper dh = getDBHelper(uri);
		List<String> ss = uri.getPathSegments();
		Bundle rb = new Bundle();
		ArrayList<String> el = new ArrayList<String>();

		switch (ss.size()) {
		case DATABASES:
			String path = getContext().getFilesDir().getAbsolutePath();
			path = path.substring(0, path.lastIndexOf("files")) + "databases";
			File f = new File(path);
			for (String fn : f.list()) {
				if (!isSysDb(fn))
					el.add(fn.substring(0, fn.indexOf(".")));
			}
			rb.putStringArrayList("databases", el);
			break;
		case DATABASE_ID:
			if (dh != null) {
				SQLiteDatabase db = dh.getReadableDatabase();
				Cursor c = null;
				c = db.query("sqlite_master", new String[] { "name" },
						"type=?", new String[] { "table" }, null, null, null);
				if (c.moveToFirst()) {
					do {
						String tName = c.getString(0);
						if (!isSysT(tName))
							el.add(c.getString(0));
					} while (c.moveToNext());
				}
			}
			rb.putStringArrayList("tables", el);
			break;
		case TABLE_ID:
			if (dh != null) {
				SQLiteDatabase db = dh.getReadableDatabase();
				String tName = esSysW(ss.get(TABLE_ID - 1));
				Cursor c = null;
				c = db.rawQuery("PRAGMA table_info(" + tName + ");", null);
				ArrayList<String> t = new ArrayList<String>();
				if (c.moveToFirst()) {
					do {
						t.add(c.getString(1));
					} while (c.moveToNext());
				}

				rb.putStringArrayList("table", t);
			}
		}
		return rb;
	}

	private void lock(Uri uri) {
		DatabaseHelper dh = getDBHelper(uri);
		SQLiteDatabase db = dh.getWritableDatabase();
		db.beginTransactionNonExclusive();
	}

	private void unlock(Uri uri) {
		DatabaseHelper dh = getDBHelper(uri);
		SQLiteDatabase db = dh.getWritableDatabase();
		db.endTransaction();
	}

	private void createTable(Uri uri, ArrayList<ContentValues> fs,
			ArrayList<String> unis) {

		DatabaseHelper dh = getDBHelper(uri);
		SQLiteDatabase db = dh.getWritableDatabase();

		String tName = esSysW(uri.getLastPathSegment());
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE table " + tName + " (");
		for (int i = 0; i < fs.size(); i++) {
			ContentValues f = fs.get(i);
			String fName = esSysW(f.getAsString("name"));
			String fType = esSysW(f.getAsString("type"));
			if (!fName.equals("time") && !fName.equals("sender")
					&& !fName.equals("rid"))
				sb.append(fName + " " + modifyType(fType) + ", ");
		}
		if (!isSysT(tName)) {
			sb.append("time INTEGER, ");
			sb.append("sender TEXT, ");
			sb.append("rid INTEGER PRIMARY KEY, ");
		}

		StringBuffer uniS = new StringBuffer();

		if (unis != null && unis.size() != 0) {
			uniS.append("UNIQUE(");
			for (String uni : unis) {
				uniS.append(esSysW(uni) + ", ");
			}
			uniS.delete(uniS.length() - ", ".length(), uniS.length());
			sb.append(uniS.toString());
			sb.append(")");
		} else {
			sb.delete(sb.length() - ", ".length(), sb.length());
		}

		sb.append(");");

		try {
			db.execSQL(sb.toString());
			Log.d("TAG", "table created");
		} catch (SQLiteException e) {
			Log.d("TAG", e.toString());
		}
	}

	private Uri createDatabase(Uri u) {
		switch (sURIMatcher.match(u)) {
		case DATABASES:
			break;
		case DATABASE_ID:
			String dbName = u.getLastPathSegment();
			DatabaseHelper dh = mDBs.get(dbName);
			if (dh == null) {
				Log.d("createDatabase", dbName);
				dh = new DatabaseHelper(getContext(), dbName + ".db", null, 1);
				SQLiteDatabase db = dh.getWritableDatabase();
				db.close();
				mDBs.put(dbName, dh);
				return u;
			}
		case TABLE_ID:
		}

		return null;
	}

	private void createXml(Uri u, String xCon) {
		switch (sURIMatcher.match(u)) {
		case DATABASES:
		case TABLE_ID:
			return;
		case DATABASE_ID:
			List<String> ss = u.getPathSegments();
			String xName = ss.get(1);
			File f;
			if (xName.contains("schema")) {
				f = new File("/data/data/mine.database5/xmls/" + xName + ".xsd");
			} else {
				f = new File("/data/data/mine.database5/xmls/" + xName + ".xml");
			}
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				bw.write(xCon);
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		default:
		}
	}

	private String getXml(Uri u) {
		// xName == dbName
		StringBuffer xCon = new StringBuffer();
		switch (sURIMatcher.match(u)) {
		case DATABASES:
		case TABLE_ID:
			return null;
		case DATABASE_ID:
			List<String> ss = u.getPathSegments();
			String xName = ss.get(1);
			File f;
			if (xName.contains("schema")) {
				f = new File("/data/data/mine.database5/xmls/" + xName + ".xsd");
			} else {
				f = new File("/data/data/mine.database5/xmls/" + xName + ".xml");
			}
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line;
				while ((line = br.readLine()) != null) {
					xCon.append(line + "\n");
				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		default:
		}
		return xCon.toString();
	}

	private void deleteGroup(Uri u) {
		switch (sURIMatcher.match(u)) {
		case DATABASES:
		case DATABASE_ID:
			List<String> ss = u.getPathSegments();
			String gName = ss.get(1);
			File dir = new File("/data/data/mine.database5/xmls/");
			File[] fs = dir.listFiles();
			for (int i = 0; i < fs.length; i++) {
				File f = fs[i];
				String fName = f.getName();
				if (fName.contains(gName)) {
					f.delete();
				}
			}

			dir = new File("/data/data/mine.database5/databases/");
			fs = dir.listFiles();
			for (int i = 0; i < fs.length; i++) {
				File f = fs[i];
				String fName = f.getName();
				if (fName.contains(gName)) {
					f.delete();
				}
			}

			Uri u1 = Uri.parse("content://" + AUTHORITY + PBASE
					+ "/mine_groupInfo/group_list");
			delete(u1, "name=?", new String[] { gName });
		case TABLE_ID:
		default:
		}
	}

	private String modifyType(String type) {
		return typeMap.get(type);
	}

	private String esSysW(String word) {
		if (sqSysWord.contains(word))
			word = "\"" + word + "\"";
		return word;
	}

	public String rSysW(String word) {

		if (word == null)
			return null;

		for (String ssw : sqSysWord) {
			if (word.equals(ssw) || word.contains(ssw + "=?"))
				word = word.replaceAll(ssw, "\"" + ssw + "\"");
		}
		return word;
	}

	public ContentValues getFormarizedCV(ContentValues cv) {
		ContentValues rcv = new ContentValues();
		Set<String> ks = cv.keySet();
		for (String k : ks) {
			rcv.put(rSysW(k), cv.getAsString(k));
		}
		return rcv;
	}

	public String[] getFormarizedSA(String[] ss) {
		String[] rss = null;
		if (ss != null) {
			rss = new String[ss.length];
			for (int i = 0; i < ss.length; i++) {
				rss[i] = rSysW(ss[i]);
			}
		}
		return rss;
	}

	public String getFormarizedS(String s) {
		return rSysW(s);
	}

	private boolean isSysDb(String n) {
		boolean res = false;
		if (n.contains("journal") || n.contains("sys_db"))
			res = true;
		return res;
	}

	private boolean isSysT(String n) {
		boolean res = false;
		if (n.contains("android_metadata") || n.startsWith("sys_"))
			res = true;
		return res;
	}

	private DatabaseHelper getDBHelper(Uri uri) {
		// given that databases exist
		Log.d("urimatcher", sURIMatcher.toString());
		switch (sURIMatcher.match(uri)) {
		case DATABASES:
			return null;
		case DATABASE_ID:
		case TABLE_ID:
			Log.d("database", "exist");
			List<String> ss = uri.getPathSegments();
			String dbName = ss.get(1);
			DatabaseHelper dh = mDBs.get(dbName);
			if (dh == null) {
				Log.d("database", dbName);
				dh = new DatabaseHelper(getContext(), dbName + ".db", null, 1);
				mDBs.put(dbName, dh);
			}
			return dh;
		default:
			String s = Integer.valueOf(sURIMatcher.match(uri)).toString();
			Log.d("result", s);
		}
		return null;
	}
}
