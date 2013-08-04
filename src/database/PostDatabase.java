package database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.Service;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class PostDatabase extends Service {

	public static final String AUTHORITY = "lib.database.databasecontentprovider";
	public static final String PBASE = "/databases";
	ContentProvider cp;
	String urlS;
	String dbName;
	ContentValues aus;
	String url;
	Thread thread;

	@Override
	public void onCreate() {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		dbName = intent.getStringExtra("dbName");
		aus = intent.getParcelableExtra("aus");
		url = intent.getStringExtra("url");
		cp = ContentProviderManager.getContentProvider(this);

		thread = new SDThread(url, aus, dbName);
		thread.start();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class SDThread extends Thread {

		String dbName;
		ContentValues aus;
		String url;
		ArrayList<String> ts;
		int nrs = 1;
		long lT = 0;

		SDThread(String url, ContentValues aus, String dbName) {
			this.aus = aus;
			this.dbName = dbName;
			this.url = url;
		}

		public void run() {

			while (thread != null) {
				try {
					Uri uri = Uri.parse("content://" + AUTHORITY + PBASE
							+ "/sys_db/time_t");
					Cursor c = cp.query(uri, null, "database=?",
							new String[] { dbName }, null);
					if (c.moveToFirst()) {
						lT = c.getLong(c.getColumnIndex("time"));
					}

					String dbCons = getSXCons();
					if (nrs > 0) {
						URL u;
						u = new URL(url);
						HttpURLConnection con = (HttpURLConnection) u
								.openConnection();
						con.setRequestMethod("POST");
						con.setDoOutput(true);
						con.connect();

						BufferedWriter bw = new BufferedWriter(
								new OutputStreamWriter(con.getOutputStream()));
						Set<String> ks = aus.keySet();
						Iterator<String> ite = ks.iterator();
						while (ite.hasNext()) {
							String k = ite.next();
							bw.write(k + "=" + aus.getAsString(k) + "&");
						}
						bw.write("type=database&content=");
						bw.write(dbCons);
						bw.close();

						BufferedReader bf = new BufferedReader(
								new InputStreamReader(con.getInputStream()));
						String line;
						while ((line = bf.readLine()) != null) {
							Log.d("response", line);
							if (line.contains("database ok"))
								thread = null;
						}
						bf.close();
						con.disconnect();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private String getSXCons() {
			Uri uri;
			Cursor c;
			String sxCons = null;
			DocumentBuilder db;

			try {
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				DOMImplementation domImpl = db.getDOMImplementation();
				Document d = domImpl.createDocument("", "database", null);
				Element r = d.getDocumentElement();
				r.setAttribute("name", dbName);

				ts = getTables(dbName);

				uri = Uri
						.parse("content://" + AUTHORITY + PBASE + "/" + dbName);
				Bundle b = new Bundle();
				b.putParcelable("uri", uri);
				cp.call("lock", null, b);

				for (String tName : ts) {
					Element cn = d.createElement("table");
					cn.setAttribute("name", tName);
					String[] pros = getFields(dbName, tName);
					c = cp.query(uri, pros, "time>=?", new String[] { Long
							.valueOf(lT).toString() }, null);

					if (c.moveToFirst()) {
						do {
							Element cn1 = d.createElement("record");
							cn1.setAttribute("name", Integer.valueOf(nrs)
									.toString());
							for (int i = 0; i < c.getColumnCount(); i++) {
								String fName = c.getColumnName(i);
								String value = c.getString(i);
								Element cn2 = d.createElement("field");
								cn2.setAttribute("name", fName);
								cn2.setAttribute("value", value);
								cn1.appendChild(cn2);
								Log.d("TAG", fName);
								Log.d("TAG", value);
							}

							cn.appendChild(cn1);
							nrs++;
						} while (c.moveToNext());
					}
					r.appendChild(cn);
				}

				setTime(dbName);
				cp.call("unlock", null, b);

				StringWriter writer = new StringWriter();
				Transformer transformer = TransformerFactory.newInstance()
						.newTransformer();
				transformer.transform(new DOMSource(r),
						new StreamResult(writer));
				sxCons = writer.toString();
			} catch (TransformerFactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return sxCons;
		}
	}

	private ArrayList<String> getTables(String dbName) {
		Bundle sb = new Bundle();
		sb.putParcelable("uri",
				Uri.parse("content://" + AUTHORITY + PBASE + "/" + dbName));
		Bundle rb = cp.call("getEntities", null, sb);
		return rb.getStringArrayList("tables");
	}

	private String[] getFields(String dbName, String tName) {
		String[] fs;
		Bundle sb = new Bundle();
		sb.putParcelable(
				"uri",
				Uri.parse("content://" + AUTHORITY + PBASE + "/" + dbName + "/"
						+ tName));
		Bundle rb = cp.call("getEntities", null, sb);
		ArrayList<String> fsl = rb.getStringArrayList("table");
		fs = new String[fsl.size()];
		for (int i = 0; i < fs.length; i++) {
			fs[i] = fsl.get(i);
		}

		return fs;
	}

	private void setTime(String dbName) {
		Long t = System.currentTimeMillis();
		ContentValues cv = new ContentValues();
		cv.put("time", t);
		int affected = cp.update(
				Uri.parse("content://" + AUTHORITY + PBASE + "/sys_db/time_t"),
				cv, "database=?", new String[] { dbName });
		if (affected == -1) {
			cv.put("database", dbName);
			cp.insert(
					Uri.parse("content://" + AUTHORITY + PBASE
							+ "/sys_db/time_t"), cv);
		}
	}
}
