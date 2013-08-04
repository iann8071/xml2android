package b.a;
import android.widget.Button;
import android.content.Intent;
import java.util.ArrayList;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.ContentProvider;
import database.DatabaseManager;
import android.content.ContentValues;
import database.ContentProviderManager;
import android.os.Bundle;
import android.app.Activity;
public class LoginTPage1Activity extends Activity{ ContentProvider cp;
DatabaseManager dm;
String pName = "b.a";
ArrayList<String> taken;
ArrayList<ContentValues> es;
	@Override
	protected void onCreate(Bundle savedInstanceState){ cp = ContentProviderManager.getContentProvider(this);
super.onCreate(savedInstanceState);
this.setContentView(R.layout.activity_login_t_page1);
taken = this.getIntent().getStringArrayListExtra("taken");
if (taken == null) { taken = new ArrayList<String>();
 };
dm = new DatabaseManager(this);
Button button0 = (Button) this.findViewById(R.id.button0);
button0.setOnClickListener(new OnClickListener(){ 	@Override
	public void onClick(View arg0){ String rid = dm.insert(taken, "/login_db/login_t");
try{Intent i = new Intent(LoginTPage1Activity.this, Class.forName("b.a.LoginTPage2Activity"));
ArrayList<String> take = dm.getTakeWithMax(taken, dm.getMRid(taken, "/login_db/login_t/max"), "/login_db/login_t/max");
i.putStringArrayListExtra("taken", take);
LoginTPage1Activity.this.startActivity(i);
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
es = dm.getAllEntities(taken, "/login_db/login_t");
final ListView listView0 = (ListView) this.findViewById(R.id.listView0);
ArrayList<String> data = dm.conVal2Str(dm.getEntities(taken, "/login_db/login_t/all/1"));
ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
listView0.setOnItemClickListener(new OnItemClickListener(){ 	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){ try{Intent i = new Intent(LoginTPage1Activity.this, Class.forName("b.a.LoginTPage2Activity"));
String selected = (String) dm.getSelectedEntity(taken, "/login_db/login_t/selected", es, position);
ArrayList<String> take = dm.getTakeWithSelected(taken, selected, "/login_db/login_t/selected");
i.putStringArrayListExtra("taken", take);
LoginTPage1Activity.this.startActivity(i);
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
