package layout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

import main.CreateFactory;
import main.MainGenerator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.net.Uri;
import android.widget.AdapterView;

public class CreateActivity {
	Element e;
	String aName;
	String fName;
	String pName;
	String[] ns;
	PrintWriter pw;
	Document document;
	CreateFactory cf;
	HashMap<String, Integer> vns;
	{
		vns = new HashMap<String, Integer>();
		vns.put("TextView", 0);
		vns.put("EditText", 0);
		vns.put("Button", 0);
		vns.put("ListView", 0);
	}
	StringBuilder oc;
	StringBuilder ic;
	String oPath;
	String vType;
	String creConS = "";
	String resConS = "";
	String en;
	boolean first = false;
	int in = 0;

	public CreateActivity(Element e, String pName, String paName)
			throws IOException {
		this.e = e;
		oc = new StringBuilder();
		ic = new StringBuilder();
		oPath = MainGenerator.OPATH;
		fName = CreateLayout.getFileName(paName);
		aName = CreateFactory.getActivityName(paName);
		cf = new CreateFactory();
		this.pName = pName;
		String path = oPath + "/output/src/" + pName.replaceAll("\\.", "/")
				+ "/" + aName + ".java";
		File f = new File(path);
		if (!f.isFile())
			f.createNewFile();
		pw = new PrintWriter(new FileWriter(path));
	}

	public void createViewStmt(Element e) {
		NodeList nl = e.getChildNodes();
		int nll = nl.getLength();
		for (int i = 0; i < nll; i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element e1 = (Element) n;
				String tn = e1.getTagName();
				String timing = e1.getAttribute("update-timing");

				if (tn.equals("first-page")) {
					first = true;
				}

				if (timing.equals("once")) {
					if (tn.equals("button")) {
						creConS += addButton(e1);
					} else if (tn.equals("text")) {
						creConS += addText(e1);
					} else if (tn.equals("edit")) {
						creConS += addEdit(e1);
					} else if (tn.equals("list")) {
						creConS += addList(e1);
					}
				} else if (timing.equals("always")) {
					if (tn.equals("button")) {
						resConS += addButton(e1);
					} else if (tn.equals("text")) {
						resConS += addText(e1);
					} else if (tn.equals("edit")) {
						resConS += addEdit(e1);
					} else if (tn.equals("list")) {
						resConS += addList(e1);
					}
				}
				createViewStmt(e1);
			}

		}
	}

	public void create() throws IOException {

		createViewStmt(e);

		// package
		cf.createPackage(pName);

		// field
		createFields();

		// onCreate
		cf.createMethod(true, "protected", "void", "onCreate",
				new String[] { "Bundle" },
				new String[] { "savedInstanceState" }, createDynamicOnCreate());
		// onStart
		cf.createMethodNoParameter(true, "protected", "void", "onStart",
				createDynamicOnStart());
		// onResume
		cf.createMethodNoParameter(true, "protected", "void", "onResume",
				createDynamicOnResume());
		// onPause
		cf.createMethodNoParameter(true, "protected", "void", "onPause",
				createDynamicOnPause());
		// onStop
		cf.createMethodNoParameter(true, "protected", "void", "onStop",
				createDynamicOnStop());
		// onDestroy
		cf.createMethodNoParameter(true, "protected", "void", "onDestroy",
				createDynamicOnDestroy());
		// method

		// make class
		pw.println(cf.createClass("public", "Activity", aName));
		pw.close();
	}

	public void createFields() {
		cf.createField(cf.createStatement(cf.createDefinition(false,
				"ContentProvider", "cp")));
		cf.createField(cf.createStatement(cf.createDefinition(false,
				"DatabaseManager", "dm")));
		cf.createField(cf.createStatement(cf.createAssign(false, "String",
				"pName", "\"" + pName + "\"")));
		cf.createField(cf.createStatement(cf.createDefinition(false,
				"ArrayList<String>", "taken")));
		cf.createField(cf.createStatement(cf.createDefinition(false,
				"ArrayList<ContentValues>", "es")));
	}

	public String createDatabaseOrTableActivity() {
		StringBuilder sb = new StringBuilder();

		return sb.toString();
	}

	public String createIndexOrRecord() {
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	}

	public String createDynamicOnCreate() {
		StringBuilder sb = new StringBuilder();
		sb.append(cf.createStatement(cf.createAssignNoType("cp", cf
				.createMethodCall("ContentProviderManager",
						"getContentProvider", new String[] { "this" }))));
		sb.append(cf.createStatement(cf.createMethodCall("super", "onCreate",
				new String[] { "savedInstanceState" })));
		sb.append(cf.createStatement(cf.createMethodCall("this",
				"setContentView", new String[] { "R.layout." + fName })));
		sb.append(cf.createStatement(cf.createAssignNoType("taken", cf
				.createMethodCall(
						cf.createMethodCallNoParameter("this", "getIntent"),
						"getStringArrayListExtra",
						new String[] { cf.getStringLiteral("taken") }))));
		sb.append(cf.createIf(
				"taken == null",
				cf.createStatement(cf.createAssignNoType("taken",
						cf.createNewNoParameter("ArrayList<String>")))));
		sb.append(cf.createStatement(cf.createAssignNoType("dm",
				cf.createNew("DatabaseManager", new String[] { "this" }))));

		if (first) {

			StringBuilder initStmt = new StringBuilder();
			initStmt.append(cf.createStatement(cf.createAssign(false,
					"AssetManager", "am", cf.createMethodCallNoParameter(
							cf.createMethodCallNoParameter("this",
									"getResources"), "getAssets"))));
			StringBuilder tryStmt = new StringBuilder();
			tryStmt.append(cf.createStatement(cf.createAssign(false,
					"InputStream", "is", cf.createMethodCall("am", "open",
							new String[] { "\"create_db.xml\"" }))));
			tryStmt.append(cf.createStatement(cf.createMethodCallNoParameter(
					cf.createNew(
							"XMLPaC",
							new String[] {
									"this",
									cf.createNew("InputSource",
											new String[] { "is" }) }), "start")));
			tryStmt.append(cf.createStatement(cf.createAssignNoType("is", cf
					.createMethodCall("am", "open",
							new String[] { "\"create_db.xml\"" }))));
			tryStmt.append(cf.createStatement(cf.createMethodCallNoParameter(
					cf.createNew(
							"XMLPaCForSys",
							new String[] {
									"this",
									cf.createNew("InputSource",
											new String[] { "is" }) }), "start")));

			initStmt.append(cf.createTryCatch(tryStmt.toString(),
					"IOException", "e",
					cf.createStatement(cf.createMethodCallNoParameter("e",
							"printStackTrace"))));
			sb.append(cf.createIf(
					"!" + cf.createMethodCallNoParameter("dm", "inited"),
					initStmt.toString()));
		}
		sb.append(creConS);
		return sb.toString();
	}

	public String createDynamicOnStart() {
		StringBuilder sb = new StringBuilder();
		sb.append(cf.createStatement(cf.createMethodCallNoParameter("super",
				"onStart")));
		return sb.toString();
	}

	public String createDynamicOnResume() {
		StringBuilder sb = new StringBuilder();
		String en = e.getAttribute("entity");
		sb.append(cf.createStatement(cf.createMethodCallNoParameter("super",
				"onResume")));
		sb.append(cf.createStatement(cf.createAssignNoType(
				"es",
				cf.createMethodCall("dm", "getAllEntities", new String[] {
						"taken", CreateFactory.getStringLiteral(en) }))));
		sb.append(resConS);
		return sb.toString();
	}

	public String createDynamicOnPause() {
		StringBuilder sb = new StringBuilder();
		sb.append(cf.createStatement(cf.createMethodCallNoParameter("super",
				"onPause")));
		return sb.toString();
	}

	public String createDynamicOnStop() {
		StringBuilder sb = new StringBuilder();
		sb.append(cf.createStatement(cf.createMethodCallNoParameter("super",
				"onStop")));
		return sb.toString();
	}

	public String createDynamicOnDestroy() {
		StringBuilder sb = new StringBuilder();
		sb.append(cf.createStatement(cf.createMethodCallNoParameter("super",
				"onDestroy")));
		return sb.toString();
	}

	public String addButton(Element e) {
		StringBuilder sb = new StringBuilder();
		NodeList nl = e.getChildNodes();
		int nll = nl.getLength();
		int bN = vns.get("Button");
		String id = "button" + bN;
		sb.append(cf.createStatement(cf.createAssign(false, "Button", id, (cf
				.createCast("Button", cf.createMethodCall("this",
						"findViewById", new String[] { "R.id." + id }))))));
		vns.put("Button", bN + 1);
		for (int i = 0; i < nll; i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element e1 = (Element) n;
				String tn = e1.getTagName();
				if (tn.equals("event")) {
					String eType = e1.getAttribute("type");
					if (eType.equals("click")) {
						sb.append(cf.createStatement(cf.createMethodCall(id,
								"setOnClickListener",
								new String[] { cf.createAbstractNewNoParameter(
										"OnClickListener",
										cf.createMethodReturnString(true,
												"public", "void", "onClick",
												new String[] { "View" },
												new String[] { "arg0" },
												addEvent(e1))) })));
					}
				}
			}
		}

		return sb.toString();
	}

	public String addText(Element e) {
		StringBuilder sb = new StringBuilder();
		String en = e.getAttribute("entity");
		String tar = e.getAttribute("target");
		NodeList nl = e.getChildNodes();
		int nll = nl.getLength();

		int tN = vns.get("TextView");
		String id = "textView" + tN;
		sb.append(cf.createStatement(cf.createAssign(false, "TextView", id, (cf
				.createCast("TextView", cf.createMethodCall("this",
						"findViewById", new String[] { "R.id." + id }))))));
		vns.put("TextView", tN + 1);
		for (int i = 0; i < nll; i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element e1 = (Element) n;
				String tn = e1.getTagName();
				if (tn.equals("event")) {
					String eType = e1.getAttribute("type");
					if (eType.equals("click")) {
						sb.append(cf.createStatement(cf.createMethodCall(id,
								"setOnClickListener",
								new String[] { cf.createAbstractNewNoParameter(
										"OnClickListener",
										cf.createMethodReturnString(true,
												"public", "void", "onClick",
												new String[] { "View" },
												new String[] { "arg0" },
												addEvent(e1))) })));
					}
				}
			}
		}
		sb.append(cf.createStatement(cf.createMethodCall(id, "setText",
				new String[] { cf.createMethodCall("dm", "getEntity",
						new String[] { "taken", cf.getStringLiteral(en), }) })));
		return sb.toString();
	}

	public String addEdit(Element e) {
		StringBuilder sb = new StringBuilder();
		String en = e.getAttribute("entity");
		String tar = e.getAttribute("target");
		NodeList nl = e.getChildNodes();
		int nll = nl.getLength();

		int eN = vns.get("EditText");
		String id = "editText" + eN;
		sb.append(cf.createStatement(cf.createAssign(false, "EditText", id, (cf
				.createCast("EditText", cf.createMethodCall("this",
						"findViewById", new String[] { "R.id." + id }))))));
		vns.put("EditText", eN + 1);
		sb.append(cf.createStatement(cf.createMethodCall(
				id,
				"addTextChangedListener",
				new String[] { cf.createAbstractNewNoParameter(
						"TextWatcher",
						cf.createMethodReturnString(true, "public", "void",
								"afterTextChanged",
								new String[] { "Editable" },
								new String[] { "s" },
								addChangeValue("EditText", en))
								+ cf.createMethodReturnString(true, "public",
										"void", "beforeTextChanged",
										new String[] { "CharSequence", "int",
												"int", "int" },
										new String[] { "s", "start", "count",
												"after" }, "")
								+ cf.createMethodReturnString(true, "public",
										"void", "onTextChanged", new String[] {
												"CharSequence", "int", "int",
												"int" }, new String[] { "s",
												"start", "count", "after" }, "")) })));
		for (int i = 0; i < nll; i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element e1 = (Element) n;
				String tn = e1.getTagName();
				if (tn.equals("event")) {
					String eType = e1.getAttribute("type");
					if (eType.equals("click")) {
						sb.append(cf.createStatement(cf.createMethodCall(id,
								"setOnClickListener",
								new String[] { cf.createAbstractNewNoParameter(
										"OnClickListener",
										cf.createMethodReturnString(true,
												"public", "void", "onClick",
												new String[] { "View" },
												new String[] { "arg0" },
												addEvent(e1))) })));
					}
				}
			}
		}
		sb.append(cf.createStatement(cf.createMethodCall(id, "setText",
				new String[] { cf.createMethodCall("dm", "getEntity",
						new String[] { "taken", cf.getStringLiteral(en), }) })));
		return sb.toString();
	}

	public String addList(Element e) {
		StringBuilder sb = new StringBuilder();
		String en = e.getAttribute("entity");
		String tar = e.getAttribute("target");
		NodeList nl = e.getChildNodes();
		int nll = nl.getLength();

		int lN = vns.get("ListView");
		String id = "listView" + lN;
		sb.append(cf.createStatement(cf.createAssign(true, "ListView", id, (cf
				.createCast("ListView", cf.createMethodCall("this",
						"findViewById", new String[] { "R.id." + id }))))));
		sb.append(cf.createStatement(cf.createAssign(false,
				"ArrayList<String>", "data", cf.createMethodCall("dm",
						"conVal2Str",
						new String[] { cf
								.createMethodCall(
										"dm",
										"getEntities",
										new String[] { "taken",
												cf.getStringLiteral(en) }) }))));
		sb.append(cf.createStatement(cf.createAssign(
				false,
				"ArrayAdapter<String>",
				"aa",
				(cf.createNew("ArrayAdapter<String>", new String[] { "this",
						"android.R.layout.simple_list_item_1", "data" })))));

		for (int i = 0; i < nll; i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element e1 = (Element) n;
				String tn = e1.getTagName();
				if (tn.equals("event")) {
					String eType = e1.getAttribute("type");
					if (eType.equals("click")) {
						sb.append(cf.createStatement(cf.createMethodCall(
								id,
								"setOnItemClickListener",
								new String[] { cf.createAbstractNewNoParameter(
										"OnItemClickListener",
										cf.createMethodReturnString(
												true,
												"public",
												"void",
												"onItemClick",
												new String[] {
														"AdapterView<?>",
														"View", "int", "long" },
												new String[] { "parent",
														"view", "position",
														"id" }, addEvent(e1))) })));
					}
				}
			}
		}

		sb.append(cf.createStatement(cf.createMethodCall(id, "setAdapter",
				new String[] { "aa" })));
		vns.put("ListView", lN + 1);
		return sb.toString();
	}

	public String addEvent(Element e) {
		StringBuilder sb = new StringBuilder();
		NodeList nl1 = e.getChildNodes();
		for (int j = 0; j < nl1.getLength(); j++) {
			Node n1 = nl1.item(j);
			if (n1 instanceof Element) {
				Element e2 = (Element) n1;
				String tn1 = e2.getTagName();
				if (tn1.equals("action")) {
					String aType = e2.getAttribute("type");
					String en = e2.getAttribute("entity");
					if (aType.equals("create")) {
						sb.append(cf.createStatement(cf.createAssign(
								false,
								"String",
								"rid",
								cf.createMethodCall(
										"dm",
										"insert",
										new String[] {
												"taken",
												CreateFactory
														.getStringLiteral(en) }))));
					} else if (aType.equals("update")) {
						sb.append(cf.createStatement(cf.createMethodCall(
								"dm",
								"update",
								new String[] { "taken",
										CreateFactory.getStringLiteral(en),
										"es" })));
					} else if (aType.equals("delete")) {
						sb.append(cf.createStatement(cf.createMethodCall(
								"dm",
								"delete",
								new String[] { "taken",
										CreateFactory.getStringLiteral(en),
										"es" })));
					} else if (aType.equals("send")) {
						String to = e2.getAttribute("to");
						NodeList nl2 = e2.getChildNodes();
						sb.append(cf.createStatement(cf.createAssign(false,
								"ArrayList<String>", "aus",
								cf.createNewNoParameter("ArrayList<String>"))));
						for (int k = 0; k < nl2.getLength(); k++) {
							Node n2 = nl2.item(k);
							if (n2 instanceof Element) {
								Element e3 = (Element) n2;
								String tn2 = e3.getTagName();
								if (tn2.equals("authentication")) {
									String auen = e3.getAttribute("entity");
									sb.append(cf.createStatement(cf
											.createMethodCall(
													"aus",
													"add",
													new String[] { CreateFactory
															.getStringLiteral(auen) })));
								}
							}
						}
						if (en.contains("taken") || to.contains("taken")) {
							sb.append(cf.createStatement(cf.createMethodCall(
									"dm",
									"sendWithTaken",
									new String[] { "taken",
											CreateFactory.getStringLiteral(en),
											"aus",
											CreateFactory.getStringLiteral(to) })));
						}
					} else if (aType.equals("move")) {
						StringBuilder tcStmt = new StringBuilder();
						String to = e2.getAttribute("to");
						String take = e2.getAttribute("take");
						int lN = vns.get("ListView");
						String id = "listView" + lN;
						tcStmt.append(cf.createStatement(cf.createAssign(
								false,
								"Intent",
								"i",
								cf.createNew(
										"Intent",
										new String[] {
												aName + ".this",
												cf.createMethodCall(
														"Class",
														"forName",
														new String[] { CreateFactory
																.getStringLiteral(pName
																		+ "."
																		+ CreateFactory
																				.getActivityName(to)) }) }))));
						if (take != null && take != "") {
							if (take.contains("selected")) {
								if (((Element) e2.getParentNode()
										.getParentNode()).getTagName().equals(
										"list")) {
									tcStmt.append(cf.createStatement(cf.createAssign(
											false,
											"String",
											"selected",
											cf.createCast(
													"String",
													cf.createMethodCall(
															"dm",
															"getSelectedEntity",
															new String[] {
																	"taken",
																	CreateFactory
																			.getStringLiteral(take),
																	"es",
																	"position" })))));
									tcStmt.append(cf.createStatement(cf.createAssign(
											false,
											"ArrayList<String>",
											"take",
											cf.createMethodCall(
													"dm",
													"getTakeWithSelected",
													new String[] {
															"taken",
															"selected",
															CreateFactory
																	.getStringLiteral(take) }))));
								}
							} else if (take.contains("max")) {
								tcStmt.append(cf.createStatement(cf.createAssign(
										false,
										"ArrayList<String>",
										"take",
										cf.createMethodCall(
												"dm",
												"getTakeWithMax",
												new String[] {
														"taken",
														cf.createMethodCall(
																"dm",
																"getMRid",
																new String[] {
																		"taken",
																		CreateFactory
																				.getStringLiteral(take) }),
														CreateFactory
																.getStringLiteral(take) }))));
							} else {
								tcStmt.append(cf.createStatement(cf.createAssign(
										false,
										"ArrayList<String>",
										"take",
										cf.createMethodCall(
												"dm",
												"getTake",
												new String[] {
														"taken",
														CreateFactory
																.getStringLiteral(take) }))));
							}

							tcStmt.append(cf.createStatement(cf
									.createMethodCall(
											"i",
											"putStringArrayListExtra",
											new String[] {
													CreateFactory
															.getStringLiteral("taken"),
													"take" })));
						}
						tcStmt.append(cf.createStatement(cf.createMethodCall(
								aName + ".this", "startActivity",
								new String[] { "i" })));
						// tcStmt.append(cf.createStatement(cf.createMethodCall(
						// "dm", "initNs", new String[] { "ns" })));
						sb.append(cf.createTryCatch(tcStmt.toString(),
								"ClassNotFoundException", "e",
								cf.createStatement(cf
										.createMethodCallNoParameter("e",
												"printStackTrace"))));
					} else if (aType.equals("back")) {
						sb.append(cf.createStatement(cf
								.createMethodCallNoParameter(aName + ".this",
										"finish")));
					}
				}
			}
		}
		return sb.toString();
	}

	public String addChangeValue(String vType, String en) {
		StringBuilder sb = new StringBuilder();
		if (vType.equals("EditText")) {
			sb.append(cf.createStatement(cf.createAssign(false, "String", "en",
					cf.getStringLiteral(en))));
			sb.append(cf.createStatement(cf.createMethodCall(
					"dm",
					"setEntity",
					new String[] { "taken", "es", "en",
							cf.createMethodCallNoParameter("s", "toString") })));
		}
		return sb.toString();
	}

	/*
	 * public String addFormView(Element e) { StringBuilder sb = new
	 * StringBuilder(); NodeList nl = e.getChildNodes(); int nll =
	 * nl.getLength(); int in = 0; sb.append(cf.createStatement(cf
	 * .createDefinition(false,"TextView", "textView")));
	 * sb.append(cf.createStatement(cf .createDefinition(false,"EditText",
	 * "editText")));
	 * sb.append(cf.createStatement(cf.createDefinition(false,"ContentValues",
	 * "cv"))); for (int i = 0; i < nll; i++) { Node n = nl.item(i); if (n
	 * instanceof Element) { Element e1 = (Element) nl.item(i); String tn =
	 * e1.getTagName(); if (tn.equals("item")) {
	 * sb.append(cf.createStatement(cf.createAssignNoType("cv", cf
	 * .createMethodCall("nvs", "get", new String[] { Integer.valueOf(in)
	 * .toString() })))); sb.append(cf.createStatement(cf.createAssignNoType(
	 * "textView", (cf.createCast("TextView", cf.createMethodCall( "this",
	 * "findViewById", new String[] { "R.id.textView" + in }))))));
	 * sb.append(cf.createStatement(cf.createMethodCall( "textView", "setText",
	 * new String[] { cf .createMethodCall("cv", "getAsString", new String[] {
	 * "\"name\"" }) })));
	 * sb.append(cf.createStatement(cf.createMethodCall("tvs", "add", new
	 * String[] { "textView" })));
	 * sb.append(cf.createStatement(cf.createAssignNoType( "editText",
	 * (cf.createCast("EditText", cf.createMethodCall( "this", "findViewById",
	 * new String[] { "R.id.editText" + in }))))));
	 * sb.append(cf.createStatement(cf.createMethodCall( "editText", "setText",
	 * new String[] { cf .createMethodCall("cv", "getAsString", new String[] {
	 * "\"value\"" }) })));
	 * sb.append(cf.createStatement(cf.createMethodCall("ets", "add", new
	 * String[] { "editText" }))); in++; } } } return sb.toString(); }
	 * 
	 * public String addClickEvent() { StringBuilder sb = new StringBuilder();
	 * StringBuilder tcStmt = new StringBuilder();
	 * tcStmt.append(cf.createStatement(cf.createMethodCallNoParameter("dm",
	 * "insert")));
	 * tcStmt.append(cf.createStatement(cf.createAssign(false,"String", "aName",
	 * cf.createMethodCallNoParameter("dm", "getActivityName"))));
	 * tcStmt.append(cf.createStatement(cf.createAssign(false,"Intent", "i", cf
	 * .createNew( "Intent", new String[] { aName + ".this",
	 * cf.createMethodCall("Class", "forName", new String[] { "\"" + pName +
	 * ".\"" + " + aName" }) }))));
	 * tcStmt.append(cf.createStatement(cf.createAssignNoType("rid",
	 * cf.createMethodCallNoParameter("dm", "getMRid"))));
	 * tcStmt.append(cf.createStatement(cf.createMethodCall("i", "putExtra", new
	 * String[] { "\"rid\"", "rid" })));
	 * tcStmt.append(cf.createStatement(cf.createMethodCall("i",
	 * "putStringArrayListExtra", new String[] { "\"ns\"", "ns" })));
	 * tcStmt.append(cf.createStatement(cf.createMethodCall(aName + ".this",
	 * "startActivity", new String[] { "i" })));
	 * sb.append(cf.createTryCatch(tcStmt.toString(), "ClassNotFoundException",
	 * "e", cf.createStatement(cf .createMethodCallNoParameter("e",
	 * "printStackTrace")))); return sb.toString(); }
	 * 
	 * public String addClickEventForList() { StringBuilder sb = new
	 * StringBuilder(); sb.append(cf.createStatement(cf.createAssign("ListView",
	 * "listView", cf.createCast("ListView", "parent")))); StringBuilder tcStmt
	 * = new StringBuilder(); switch (ns.length) { case 0: case 1: case 2:
	 * tcStmt.append(cf.createStatement(cf.createAssign( "ArrayList<String>",
	 * "sns", cf.createNewNoParameter("ArrayList<String>"))));
	 * tcStmt.append(cf.createStatement(cf.createMethodCall("sns", "addAll", new
	 * String[] { "ns" })));
	 * tcStmt.append(cf.createStatement(cf.createMethodCall("sns", "add", new
	 * String[] { cf.createCast("String", cf.createMethodCall( "listView",
	 * "getItemAtPosition", new String[] { "position" })) })));
	 * tcStmt.append(cf.createStatement(cf.createMethodCall("dm", "setNs", new
	 * String[] { "sns" })));
	 * tcStmt.append(cf.createStatement(cf.createAssign("String", "aName",
	 * cf.createMethodCallNoParameter("dm", "getActivityName"))));
	 * tcStmt.append(cf.createStatement(cf.createAssign("Intent", "i", cf
	 * .createNew( "Intent", new String[] { aName + ".this",
	 * cf.createMethodCall("Class", "forName", new String[] { "\"" + pName +
	 * ".\"" + " + aName" }) })))); tcStmt.append(cf.createStatement(cf
	 * .createMethodCall("i", "putStringArrayListExtra", new String[] {
	 * "\"ns\"", "sns" }))); break; case 3:
	 * tcStmt.append(cf.createStatement(cf.createAssignNoType("rid", cf
	 * .createMethodCall("dm", "getRid", new String[] { "position" }))));
	 * tcStmt.append(cf.createStatement(cf.createAssign("String", "aName",
	 * cf.createMethodCallNoParameter("dm", "getActivityName"))));
	 * tcStmt.append(cf.createStatement(cf.createAssign("Intent", "i", cf
	 * .createNew( "Intent", new String[] { aName + ".this",
	 * cf.createMethodCall("Class", "forName", new String[] { "\"" + pName +
	 * ".\"" + " + aName" }) }))));
	 * tcStmt.append(cf.createStatement(cf.createMethodCall("i", "putExtra", new
	 * String[] { "\"rid\"", "rid" })));
	 * tcStmt.append(cf.createStatement(cf.createMethodCall("i",
	 * "putStringArrayListExtra", new String[] { "\"ns\"", "ns" }))); break;
	 * case 4: tcStmt.append(cf.createStatement(cf.createMethodCall("i",
	 * "putStringArrayListExtra", new String[] { "\"ns\"", "ns" }))); }
	 * 
	 * tcStmt.append(cf.createStatement(cf.createMethodCall(aName + ".this",
	 * "startActivity", new String[] { "i" })));
	 * sb.append(cf.createTryCatch(tcStmt.toString(), "ClassNotFoundException",
	 * "e", cf.createStatement(cf .createMethodCallNoParameter("e",
	 * "printStackTrace"))));
	 * 
	 * return sb.toString(); }
	 */
}
