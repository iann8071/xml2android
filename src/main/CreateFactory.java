package main;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateFactory {

	public static final ArrayList<String> ENTITIES;
	static {
		ENTITIES = new ArrayList<String>();
		ENTITIES.add("database");
		ENTITIES.add("table");
		ENTITIES.add("index");
		ENTITIES.add("record");
	}

	public final HashMap<String, String> LIBS;
	{
		LIBS = new HashMap<String, String>();
		LIBS.put("Activity", "android.app.Activity");
		LIBS.put("ContentProvider", "android.content.ContentProvider");
		LIBS.put("Bundle", "android.os.Bundle");
		LIBS.put("ArrayList", "java.util.ArrayList");
		LIBS.put("ArrayAdapter", "android.widget.ArrayAdapter");
		LIBS.put("ListView", "android.widget.ListView");
		LIBS.put("OnItemClickListener",
				"android.widget.AdapterView.OnItemClickListener");
		LIBS.put("Intent", "android.content.Intent");
		LIBS.put("AssetManager", "android.content.res.AssetManager");
		LIBS.put("InputStream", "java.io.InputStream");
		LIBS.put("Uri", "android.net.Uri");
		LIBS.put("InputSource", "org.xml.sax.InputSource");
		LIBS.put("AdapterView", "android.widget.AdapterView");
		LIBS.put("TextView", "android.widget.TextView");
		LIBS.put("Button", "android.widget.Button");
		LIBS.put("OnClickListener", "android.view.View.OnClickListener");
		LIBS.put("View", "android.view.View");
		LIBS.put("IOException", "java.io.IOException");
		LIBS.put("ContentValues", "android.content.ContentValues");
		LIBS.put("TextView", "android.widget.TextView");
		LIBS.put("EditText", "android.widget.EditText");
		LIBS.put("Boolean", "java.lang.Boolean");
		LIBS.put("HashMap", "java.util.HashMap");
		LIBS.put("TextWatcher", "android.text.TextWatcher");
		LIBS.put("Editable", "android.text.Editable");
		LIBS.put("CharSequence", "java.lang.CharSequence");
		LIBS.put("Integer", "java.lang.Integer");
		LIBS.put("DatabaseManager", "database.DatabaseManager");
		LIBS.put("ContentProviderManager", "database.ContentProviderManager");
		LIBS.put("DatabaseContentProvider", "database.DatabaseContentProvider");
		LIBS.put("DatabaseHelper", "database.DatabaseHelper");
		LIBS.put("PostDatabase", "database.PostDatabase");
		LIBS.put("XMLPaC", "database.XMLPaC");
		LIBS.put("XMLPaCForSys", "database.XMLPaCForSys");
		LIBS.put("GetDatabase", "database.GetDatabase");
		LIBS.put("", "");
	}

	public StringBuilder pack = new StringBuilder();
	public StringBuilder importFiles = new StringBuilder();
	public StringBuilder fields = new StringBuilder();
	public StringBuilder methods = new StringBuilder();

	public void createPackage(String name) {
		pack.append("package " + name + ";\n");
	}

	public String createImportFile(String name) {
		return "import " + name + ";\n";
	}

	public String createClass(String access, String parent, String name) {
		StringBuilder sb = new StringBuilder();
		appendimportFiles(deleteGenericType(parent));
		sb.append(pack).append(importFiles);
		StringBuilder con = fields.append(methods);
		sb.append(access);
		sb.append(" class " + name + " extends " + parent + "{ " + con + " }");
		return sb.toString();
	}

	public void createField(String con) {
		fields.append(con);
	}

	public String createAssign(boolean f, String type, String name, String value) {
		StringBuilder sb = new StringBuilder();
		if (f)
			sb.append("final ");
		sb.append(type + " " + name + " = " + value);
		appendimportFiles(deleteGenericType(type));
		return sb.toString();
	}

	public String createAssignNoType(String name, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append(name + " = " + value);
		return sb.toString();
	}

	public String createDefinition(boolean f, String type, String name) {
		StringBuilder sb = new StringBuilder();
		if (f)
			sb.append("final ");
		sb.append(type + " " + name);
		appendimportFiles(deleteGenericType(type));
		return sb.toString();
	}

	public String createMethodNoParameterReturnString(boolean override,
			String access, String ret, String name, String con) {
		StringBuilder sb = new StringBuilder();
		if (override)
			sb.append("\t@Override\n");
		sb.append("\t" + access);
		sb.append(" " + ret);
		sb.append(" " + name + "()");
		sb.append("{ " + con + " }\n");
		appendimportFiles(deleteGenericType(ret));
		return sb.toString();
	}

	public void createMethodNoParameter(boolean override, String access,
			String ret, String name, String con) {
		StringBuilder sb = new StringBuilder();
		if (override)
			sb.append("\t@Override\n");
		sb.append("\t" + access);
		sb.append(" " + ret);
		sb.append(" " + name + "()");
		sb.append("{ " + con + " }\n");
		appendimportFiles(deleteGenericType(ret));
		methods.append(sb.toString());
	}

	public String createFieldRead(String cName, String vName) {
		StringBuilder sb = new StringBuilder();
		sb.append(cName + "." + vName);
		return sb.toString();
	}

	public void createMethod(boolean override, String access, String ret,
			String name, String[] pts, String[] pns, String con) {
		StringBuilder sb = new StringBuilder();
		if (override)
			sb.append("\t@Override\n");
		sb.append("\t" + access);
		sb.append(" " + ret);
		sb.append(" " + name + "(");
		for (int i = 0; i < pts.length; i++) {
			sb.append(pts[i] + " " + pns[i] + ", ");
			appendimportFiles(deleteGenericType(pts[i]));
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(")");
		sb.append("{ " + con + " }\n");
		appendimportFiles(deleteGenericType(ret));
		methods.append(sb.toString());
	}

	public String createMethodReturnString(boolean override, String access,
			String ret, String name, String[] pts, String[] pns, String con) {
		StringBuilder sb = new StringBuilder();
		if (override)
			sb.append("\t@Override\n");
		sb.append("\t" + access);
		sb.append(" " + ret);
		sb.append(" " + name + "(");
		for (int i = 0; i < pts.length; i++) {
			sb.append(pts[i] + " " + pns[i] + ", ");
			appendimportFiles(deleteGenericType(pts[i]));
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(")");
		sb.append("{ " + con + " }\n");
		appendimportFiles(deleteGenericType(ret));
		return sb.toString();
	}

	public String createMethodCall(String oName, String mName, String[] ps) {
		StringBuilder sb = new StringBuilder();
		sb.append(oName + "." + mName + "(");
		for (String p : ps) {
			sb.append(p + ", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(")");
		appendimportFiles(deleteGenericType(oName));
		return sb.toString();
	}

	public String createMethodCallNoParameter(String oName, String mName) {
		StringBuilder sb = new StringBuilder();
		sb.append(oName + "." + mName + "()");
		appendimportFiles(deleteGenericType(oName));
		return sb.toString();
	}

	public String createStatement(String con) {
		StringBuilder sb = new StringBuilder();
		sb.append(con + ";\n");
		return sb.toString();
	}

	public String createIf(String pred, String con) {
		StringBuilder sb = new StringBuilder();
		sb.append("if (" + pred + ") { " + con + " };\n");
		return sb.toString();
	}

	public String createWhile(String pred, String con) {
		StringBuilder sb = new StringBuilder();
		sb.append("while (" + pred + ") { " + con + " };\n");
		return sb.toString();
	}

	public String createNew(String cName, String[] ps) {
		StringBuilder sb = new StringBuilder();
		sb.append("new " + cName + "(");
		for (String p : ps) {
			sb.append(p + ", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(")");
		appendimportFiles(deleteGenericType(cName));
		return sb.toString();
	}

	public String createNewNoParameter(String cName) {
		StringBuilder sb = new StringBuilder();
		sb.append("new " + cName + "()");
		appendimportFiles(deleteGenericType(cName));
		return sb.toString();
	}

	public String createAbstractNew(String cName, String[] ps, String con) {
		StringBuilder sb = new StringBuilder();
		sb.append("new " + cName + "(");
		for (String p : ps) {
			sb.append(p + ", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(")");
		sb.append("{ " + con + " }\n");
		appendimportFiles(deleteGenericType(cName));
		return sb.toString();
	}

	public String createAbstractNewNoParameter(String cName, String con) {
		StringBuilder sb = new StringBuilder();
		sb.append("new " + cName + "()");
		sb.append("{ " + con + " }\n");
		appendimportFiles(deleteGenericType(cName));
		return sb.toString();
	}

	public String createCast(String type, String con) {
		StringBuilder sb = new StringBuilder();
		sb.append("(" + type + ")" + " " + con);
		appendimportFiles(deleteGenericType(type));
		return sb.toString();
	}

	public String createTryCatch(String tryStmt, String exptType,
			String exptVar, String catchStmt) {
		StringBuilder sb = new StringBuilder();
		sb.append("try{" + tryStmt + "}catch(" + exptType + " " + exptVar
				+ "){" + catchStmt + "}");
		appendimportFiles(deleteGenericType(exptType));
		return sb.toString();
	}

	public String createReturn(String con) {
		StringBuilder sb = new StringBuilder();
		sb.append("return " + con);
		return sb.toString();
	}

	public void appendimportFiles(String imf) {
		String res = LIBS.get(imf);
		if (res != null) {
			importFiles.append(createImportFile(res));
			LIBS.remove(imf);
		}
	}

	public String deleteGenericType(String type) {
		if (type.contains("<")) {
			importGenericTypes(type.substring(type.indexOf("<") + 1,
					type.lastIndexOf(">")));
			type = type.substring(0, type.indexOf("<"));
		}
		return type;
	}

	public void importGenericTypes(String genericTypes) {
		String[] ts = genericTypes.split(", ");
		for (String t : ts) {
			if (t.contains("<")) {
				importGenericTypes(t.substring(t.indexOf("<") + 1,
						t.lastIndexOf(">")));
				t = t.substring(0, t.indexOf("<"));
			}
			appendimportFiles(t);
		}
	}

	public static String getActivityName(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1)
				+ "Activity";
	}

	public static String getStringLiteral(String name) {
		return "\"" + name + "\"";
	}
}
