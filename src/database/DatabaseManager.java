package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class DatabaseManager {

	Activity a;
	ContentProvider cp;
	ArrayList<String> sysDbKs = new ArrayList<String>();
	{
		sysDbKs.add("database");
		sysDbKs.add("table");
		sysDbKs.add("index");
		sysDbKs.add("record");
	}
	ArrayList<String> sysCols = new ArrayList<String>();
	{
		sysCols.add("sender");
		sysCols.add("time");
		sysCols.add("rid");
	}
	// different between index and record
	ArrayList<ContentValues> nvs = new ArrayList<ContentValues>();

	public DatabaseManager(Activity a) {
		this.a = a;
		cp = ContentProviderManager.getContentProvider(a);
	}

	public String getMRid(ArrayList<String> taken, String en) {
		String speS = replaceTaken(taken, en);
		ArrayList<String> dat = getDbAndT(speS);
		String mrid = "-1";
		Cursor c = cp.query(
				Uri.parse(ContentProviderManager.getUriString(dat)),
				new String[] { "rid" }, null, null, "rid desc");
		if (c.moveToFirst()) {
			mrid = c.getString(0);
		}
		return mrid;
	}

	public void initNs(ArrayList<String> taken) {
		taken.remove(taken.size() - 1);
	}

	public ArrayList<String> getTake(ArrayList<String> taken, String en) {
		ArrayList<String> res = new ArrayList<String>();
		String speS = replaceTaken(taken, en);
		Scanner sc = new Scanner(speS);
		sc.useDelimiter("/");
		while (sc.hasNext())
			res.add(sc.next());
		sc.close();
		return res;
	}

	public ArrayList<String> getTakeWithSelected(ArrayList<String> taken,
			String selected, String en) {
		ArrayList<String> res = new ArrayList<String>();
		String speS = replaceSelected(selected, replaceTaken(taken, en));
		Scanner sc = new Scanner(speS);
		sc.useDelimiter("/");
		while (sc.hasNext())
			res.add(sc.next());
		sc.close();
		return res;
	}

	public ArrayList<String> getTakeWithMax(ArrayList<String> taken,
			String max, String en) {
		ArrayList<String> res = new ArrayList<String>();
		String speS = replaceMax(max, replaceTaken(taken, en));
		Scanner sc = new Scanner(speS);
		sc.useDelimiter("/");
		while (sc.hasNext())
			res.add(sc.next());
		sc.close();
		return res;
	}

	public String insert(ArrayList<String> taken, String en) {
		ContentValues cv = new ContentValues();
		String speS = replaceTaken(taken, en);
		ArrayList<String> dat = getDbAndT(speS);
		String f = null;
		ArrayList<String> fs = getFieldNames(dat, f);
		cv.put(fs.get(0), "New");
		cp.insert(Uri.parse(ContentProviderManager.getUriString(dat)), cv);
		return getMRid(taken, en);
	}

	public void update(ArrayList<String> taken, String en,
			ArrayList<ContentValues> es) {
		String speS = replaceTaken(taken, en);
		ArrayList<String> dat = getDbAndT(speS);
		ContentValues cv = new ContentValues();
		switch (getLevel(speS)) {
		case 1:
			break;
		case 2:
			break;
		case 3:
		case 4:
			String r = getRecord(speS);
			cv = es.get(Integer.valueOf(getRecordNum(es, r)));
			String rid = cv.getAsString("rid");
			cp.update(Uri.parse(ContentProviderManager.getUriString(dat)), cv,
					"rid=?", new String[] { rid });
			break;
		}
	}

	public void delete(ArrayList<String> taken, String en,
			ArrayList<ContentValues> es) {
		String speS = replaceTaken(taken, en);
		ArrayList<String> dat = getDbAndT(speS);
		switch (getLevel(speS)) {
		case 1:
			break;
		case 2:
			break;
		case 3:
		case 4:
			String r = getRecord(speS);
			String rid = es.get(Integer.valueOf(getRecordNum(es, r)))
					.getAsString("rid");
			cp.delete(Uri.parse(ContentProviderManager.getUriString(dat)),
					"rid=?", new String[] { rid });
			break;
		}

	}

	public void sendWithTaken(ArrayList<String> taken, String en,
			ArrayList<String> aus, String to) {
		String speS = replaceTaken(taken, en);
		// get database name
		String dbName = getDatabase(speS);
		ContentValues cv = new ContentValues();
		for (String au : aus) {
			speS = replaceTaken(taken, au);
			ArrayList<String> dat = getDbAndT(speS);
			String r = getRecord(speS);
			String f = getField(au);
			ArrayList<String> fs = getFieldNames(dat, f);
			String name = getNameOrValue(
					getRecords(dat, r, toArray(fs)).get(0), "name");
			String value = getNameOrValue(getRecords(dat, r, toArray(fs))
					.get(0), "value");
			cv.put(name, value);
		}

		speS = replaceTaken(taken, to);
		ArrayList<String> dat = getDbAndT(speS);
		String r = getRecord(speS);
		String f = getField(to);
		ArrayList<String> fs = getFieldNames(dat, f);
		String url = getNameOrValue(getRecords(dat, r, toArray(fs)).get(0),
				"value");

		Intent i = new Intent(a, PostDatabase.class);
		i.putExtra("dbName", dbName);
		i.putExtra("aus", aus);
		i.putExtra("url", url);
		a.startService(i);
	}

	public ArrayList<ContentValues> getAllEntities(ArrayList<String> taken,
			String en) {
		ArrayList<ContentValues> res = new ArrayList<ContentValues>();
		ArrayList<String> dat, fs;
		String f, r;
		Bundle sb;
		String uS;
		Bundle rb;
		String speS = replaceTaken(taken, en);
		switch (getLevel(speS) + 1) {
		case 1:
			sb = new Bundle();
			uS = ContentProviderManager.getUriString(speS);
			sb.putParcelable("uri", Uri.parse(uS));
			rb = cp.call("getEntities", null, sb);
			res = str2ConVal(speS, rb.getStringArrayList("databases"));
			break;
		case 2:
			sb = new Bundle();
			uS = ContentProviderManager.getUriString(speS);
			sb.putParcelable("uri", Uri.parse(uS));
			rb = cp.call("getEntities", null, sb);
			res = str2ConVal(speS, rb.getStringArrayList("tables"));
			break;
		case 3:
			dat = getDbAndT(speS);
			f = null;
			r = "all";
			fs = getFieldNames(dat, f);
			res = getRecords(dat, r, toArray(fs));
			break;
		case 4:
			dat = getDbAndT(speS);
			f = null;
			r = "all";
			fs = getFieldNames(dat, f);
			res = getRecords(dat, r, toArray(fs));
			break;
		case 5:
			dat = getDbAndT(speS);
			f = null;
			r = "all";
			fs = getFieldNames(dat, f);
			res = getRecords(dat, r, toArray(fs));
			break;
		}

		return res;
	}

	public ArrayList<ContentValues> getEntities(ArrayList<String> taken,
			String en) {
		ArrayList<ContentValues> res = new ArrayList<ContentValues>();
		ArrayList<String> dat, fs;
		String speS = replaceTaken(taken, en), f, r;
		Bundle sb;
		String uS;
		Bundle rb;
		switch (getLevel(speS)) {
		case 1:
			speS = speS.replace("/all", "");
			sb = new Bundle();
			uS = ContentProviderManager.getUriString(speS);
			sb.putParcelable("uri", Uri.parse(uS));
			rb = cp.call("getEntities", null, sb);
			res = str2ConVal(speS, rb.getStringArrayList("databases"));
			break;
		case 2:
			speS = speS.replace("/all", "");
			sb = new Bundle();
			uS = ContentProviderManager.getUriString(speS);
			sb.putParcelable("uri", Uri.parse(uS));
			rb = cp.call("getEntities", null, sb);
			res = str2ConVal(speS, rb.getStringArrayList("tables"));
			break;
		case 3:
			dat = getDbAndT(speS);
			f = getField(speS);
			r = getRecord(speS);
			fs = getFieldNames(dat, f);
			res = getRecords(dat, r, toArray(fs));
			break;
		case 4:
			dat = getDbAndT(speS);
			f = getField(speS);
			r = getRecord(speS);
			fs = getFieldNames(dat, f);
			res = getRecords(dat, r, toArray(fs));
			break;
		case 5:
			dat = getDbAndT(speS);
			f = getField(speS);
			r = getRecord(speS);
			fs = getFieldNames(dat, f);
			res = getRecords(dat, r, toArray(fs));
			break;
		}

		return res;
	}

	public ArrayList<ContentValues> str2ConVal(String speS, ArrayList<String> vs) {
		ArrayList<ContentValues> res = new ArrayList<ContentValues>();
		switch (getLevel(speS) + 1) {
		case 1:
			for (int i = 0; i < vs.size(); i++) {
				ContentValues cv = new ContentValues();
				cv.put("dbName", vs.get(i));
				res.add(cv);
			}
			break;
		case 2:
			for (int i = 0; i < vs.size(); i++) {
				ContentValues cv = new ContentValues();
				cv.put("tName", vs.get(i));
				res.add(cv);
			}
			break;
		}
		return res;
	}

	public ArrayList<String> conVal2Str(ArrayList<ContentValues> cvs) {
		ArrayList<String> res = new ArrayList<String>();
		for (int i = 0; i < cvs.size(); i++) {
			String v = "";
			ContentValues cv = cvs.get(i);
			Set<String> ks = cv.keySet();
			Iterator<String> ite = ks.iterator();
			while (ite.hasNext()) {
				v += (cv.getAsString(ite.next()) + "\n");
			}
			res.add(v);
		}
		return res;
	}

	public String getEntity(ArrayList<String> taken, String en) {
		String res;
		String speS = replaceTaken(taken, en);
		ArrayList<String> dat = getDbAndT(speS);
		String f = getField(speS);
		String r = getRecord(speS);
		String nov = getNameOrValue(speS);
		ArrayList<String> fs = getFieldNames(dat, f);
		res = getNameOrValue(getRecords(dat, r, toArray(fs)).get(0), nov);
		return res;
	}

	public void setEntity(ArrayList<String> taken, ArrayList<ContentValues> es,
			String en, String val) {
		String speS = replaceTaken(taken, en);
		ArrayList<String> dat = getDbAndT(speS);
		String r, f;
		switch (getLevel(speS)) {
		case 1:
			break;
		case 2:
			break;
		case 3:
		case 4:
		case 5:
			r = getRecord(speS);
			f = getField(speS);
			ArrayList<String> fs = getFieldNames(dat, f);
			es.get(Integer.valueOf(getRecordNum(es, r))).put(fs.get(0), val);
			break;
		}
	}

	public String getNameOrValue(ContentValues r, String nameOrValue) {
		Set<String> s = r.keySet();
		Iterator<String> ite = s.iterator();
		if (nameOrValue.equals("1") || nameOrValue.equals("name")) {
			return ite.next();
		} else {
			return r.getAsString(ite.next());
		}
	}

	public String getRid(int position) {
		return nvs.get(position).getAsString("rid");
	}

	public String getSelectedEntity(ArrayList<String> taken, String en,
			ArrayList<ContentValues> es, int position) {
		String res = null;
		String speS = replaceTaken(taken, en);
		switch (getLevel(speS)) {
		case 1:
			res = es.get(position).getAsString("dbName");
			break;
		case 2:
			res = es.get(position).getAsString("tName");
			break;
		case 3:
			res = es.get(position).getAsString("rid");
			break;
		}
		return res;
	}

	public ArrayList<String> getFieldValues(ArrayList<String> fs,
			ArrayList<String> taken) {
		ArrayList<String> vs = new ArrayList<String>();
		Uri u = Uri.parse(ContentProviderManager.getUriString(taken.subList(0,
				2)));
		Cursor cu = cp.query(u, null, null, null, null);
		if (cu.moveToFirst()) {
			do {
				StringBuilder listS = new StringBuilder();
				ContentValues cv = new ContentValues();
				for (int i = 0; i < cu.getColumnCount(); i++) {
					cv.put(cu.getColumnName(i), cu.getString(i));
					if (fs.contains(cu.getColumnName(i)))
						listS.append(cu.getString(i) + "\n");
				}
				nvs.add(cv);
				vs.add(listS.toString());
			} while (cu.moveToNext());
		}
		return vs;
	}

	public ArrayList<ContentValues> getRecords(ArrayList<String> dbAndt,
			String r, String[] fs) {
		ArrayList<ContentValues> rs = new ArrayList<ContentValues>();
		Cursor cu;

		if (r.equals("all")) {
			cu = cp.query(
					Uri.parse(ContentProviderManager.getUriString(dbAndt)), fs,
					null, null, null);
			if (cu.moveToFirst()) {
				do {
					ContentValues cv = new ContentValues();
					for (int i = 0; i < cu.getColumnCount(); i++) {
						cv.put(cu.getColumnName(i), cu.getString(i));
					}
					rs.add(cv);
				} while (cu.moveToNext());
			}
		} else {
			cu = cp.query(
					Uri.parse(ContentProviderManager.getUriString(dbAndt)), fs,
					"rid=?", new String[] { r }, null);
			if (cu.moveToFirst()) {
				ContentValues cv = new ContentValues();
				for (int i = 0; i < cu.getColumnCount(); i++) {
					cv.put(cu.getColumnName(i), cu.getString(i));
				}
				rs.add(cv);
			}
		}

		return rs;
	}

	public ArrayList<String> getFieldNames(ArrayList<String> dbAndt, String f) {
		ArrayList<String> res = new ArrayList<String>();
		Bundle sb = new Bundle();
		String uS = ContentProviderManager.getUriString(dbAndt);
		sb.putParcelable("uri", Uri.parse(uS));
		Bundle rb = cp.call("getEntities", null, sb);
		ArrayList<String> fs = rb.getStringArrayList("table");
		if (f == null || f.equals("all")) {
			for (int i = 0; i < fs.size(); i++) {
				res.add(fs.get(i));
			}
		} else {
			res.add(fs.get(Integer.valueOf(f) - 1));
		}

		return res;
	}

	public ArrayList<String> getDbAndT(ArrayList<String> taken) {
		ArrayList<String> dat = new ArrayList<String>();
		dat.add(taken.get(0));
		dat.add(taken.get(1));
		return dat;
	}

	public ArrayList<String> getDbAndT(String fen) {
		ArrayList<String> dat = new ArrayList<String>();
		dat.add(getDatabase(fen));
		dat.add(getTable(fen));
		return dat;
	}

	public String getNameOrValue(String fen) {
		String res = null;
		int f = -2, s = -2;
		Pattern p = Pattern.compile("/");
		Matcher m = p.matcher(fen);
		try {
			m.find();
			m.find();
			m.find();
			m.find();
			m.find();
			f = m.start();
			m.find();
			s = m.start();
			res = fen.substring(f + 1, s);
		} catch (IllegalStateException e) {
			if (f != -2)
				res = fen.substring(f + 1);
			else
				res = null;
		}
		return res;
	}

	public String getDatabase(String fen) {
		String res = null;
		int f = -2, s = -2;
		Pattern p = Pattern.compile("/");
		Matcher m = p.matcher(fen);
		try {
			m.find();
			f = m.start();
			m.find();
			s = m.start();
			res = fen.substring(f + 1, s);
		} catch (IllegalStateException e) {
			if (f != -2)
				res = fen.substring(f + 1);
			else
				res = null;
		}
		return res;
	}

	public String getTable(String fen) {
		String res = null;
		int f = -2, s = -2;
		Pattern p = Pattern.compile("/");
		Matcher m = p.matcher(fen);
		try {
			m.find();
			m.find();
			f = m.start();
			m.find();
			s = m.start();
			res = fen.substring(f + 1, s);
		} catch (IllegalStateException e) {
			if (f != -2)
				res = fen.substring(f + 1);
			else
				res = null;
		}
		return res;
	}

	public String getRecord(String fen) {
		String res = null;
		int f = -2, s = -2;
		Pattern p = Pattern.compile("/");
		Matcher m = p.matcher(fen);
		try {
			m.find();
			m.find();
			m.find();
			f = m.start();
			m.find();
			s = m.start();
			res = fen.substring(f + 1, s);
		} catch (IllegalStateException e) {
			if (f != -2)
				res = fen.substring(f + 1);
			else
				res = null;
		}
		return res;
	}

	public String getField(String fen) {
		String res = null;
		int f = -2, s = -2;
		Pattern p = Pattern.compile("/");
		Matcher m = p.matcher(fen);
		try {
			m.find();
			m.find();
			m.find();
			m.find();
			f = m.start();
			m.find();
			s = m.start();
			res = fen.substring(f + 1, s);
		} catch (IllegalStateException e) {
			if (f != -2)
				res = fen.substring(f + 1);
			else
				res = null;
		}
		return res;
	}

	public String[] toArray(ArrayList<String> fs) {
		String[] fsa = new String[fs.size()];
		for (int i = 0; i < fs.size(); i++) {
			fsa[i] = fs.get(i);
		}
		return fsa;
	}

	public String getRecordNum(ArrayList<ContentValues> es, String rid) {
		String res = null;
		for (int i = 0; i < es.size(); i++) {
			if (es.get(i).getAsString("rid").equals(rid))
				res = Integer.valueOf(i).toString();
		}
		return res;
	}

	public boolean inited() {
		boolean res = false;
		Bundle sb = new Bundle();
		res = cp.call("inited", null, sb).getBoolean("result");
		return res;
	}

	public static String replaceTaken(List<String> taken, String en) {
		StringBuilder sb = new StringBuilder();
		if (taken != null)
			for (int i = 0; i < taken.size() && i < 3; i++) {
				sb.append("/" + taken.get(i));
			}
		return en.replace("/taken", sb.toString());
	}

	public static String replaceSelected(String selected, String en) {
		return en.replace("selected", selected);
	}

	public static String replaceMax(String max, String en) {
		return en.replace("max", max);
	}

	public static int getLevel(String en) {
		int count = 0;
		for (int i = -1; i < en.length();) {
			if ((i = en.indexOf("/", i + 1)) != -1) {
				count++;
			} else
				break;
		}
		return count;
	}
}
