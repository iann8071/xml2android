package b.a;
import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.ContentProvider;
import database.DatabaseManager;
import android.content.ContentValues;
import database.ContentProviderManager;
import android.content.res.AssetManager;
import java.io.InputStream;
import org.xml.sax.InputSource;
import database.XMLPaC;
import database.XMLPaCForSys;
import java.io.IOException;
import android.os.Bundle;
import android.app.Activity;
public class DbsPageActivity extends Activity{ ContentProvider cp;
DatabaseManager dm;
String pName = "b.a";
ArrayList<String> taken;
ArrayList<ContentValues> es;
	@Override
	protected void onCreate(Bundle savedInstanceState){ cp = ContentProviderManager.getContentProvider(this);
super.onCreate(savedInstanceState);
this.setContentView(R.layout.activity_dbs_page);
taken = this.getIntent().getStringArrayListExtra("taken");
if (taken == null) { taken = new ArrayList<String>();
 };
dm = new DatabaseManager(this);
if (!dm.inited()) { AssetManager am = this.getResources().getAssets();
try{InputStream is = am.open("create_db.xml");
new XMLPaC(this, new InputSource(is)).start();
is = am.open("create_db.xml");
new XMLPaCForSys(this, new InputSource(is)).start();
}catch(IOException e){e.printStackTrace();
} };
Button button0 = (Button) this.findViewById(R.id.button0);
button0.setOnClickListener(new OnClickListener(){ 	@Override
	public void onClick(View arg0){ try{Intent i = new Intent(DbsPageActivity.this, Class.forName("b.a.LoginTPage1Activity"));
DbsPageActivity.this.startActivity(i);
}catch(ClassNotFoundException e){e.printStackTrace();
} }
 }
);
 }
	@Override
	protected void onStart(){ super.onStart();
 }
	@Override
	protected void onResume(){ super.onResume();
es = dm.getAllEntities(taken, "");
final ListView listView0 = (ListView) this.findViewById(R.id.listView0);
ArrayList<String> data = dm.conVal2Str(dm.getEntities(taken, "/all"));
ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
listView0.setOnItemClickListener(new OnItemClickListener(){ 	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){ try{Intent i = new Intent(DbsPageActivity.this, Class.forName("b.a.DbPageActivity"));
String selected = (String) dm.getSelectedEntity(taken, "/selected", es, position);
ArrayList<String> take = dm.getTakeWithSelected(taken, selected, "/selected");
i.putStringArrayListExtra("taken", take);
DbsPageActivity.this.startActivity(i);
}catch(ClassNotFoundException e){e.printStackTrace();
} }
 }
);
listView0.setAdapter(aa);
 }
	@Override
	protected void onPause(){ super.onPause();
 }
	@Override
	protected void onStop(){ super.onStop();
 }
	@Override
	protected void onDestroy(){ super.onDestroy();
 }
 }
