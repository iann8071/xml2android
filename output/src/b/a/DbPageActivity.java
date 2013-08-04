package b.a;
import android.widget.ListView;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.widget.AdapterView;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.content.ContentProvider;
import database.DatabaseManager;
import android.content.ContentValues;
import database.ContentProviderManager;
import android.os.Bundle;
import android.app.Activity;
public class DbPageActivity extends Activity{ ContentProvider cp;
DatabaseManager dm;
String pName = "b.a";
ArrayList<String> taken;
ArrayList<ContentValues> es;
	@Override
	protected void onCreate(Bundle savedInstanceState){ cp = ContentProviderManager.getContentProvider(this);
super.onCreate(savedInstanceState);
this.setContentView(R.layout.activity_db_page);
taken = this.getIntent().getStringArrayListExtra("taken");
if (taken == null) { taken = new ArrayList<String>();
 };
dm = new DatabaseManager(this);
 }
	@Override
	protected void onStart(){ super.onStart();
 }
	@Override
	protected void onResume(){ super.onResume();
es = dm.getAllEntities(taken, "/taken");
final ListView listView0 = (ListView) this.findViewById(R.id.listView0);
ArrayList<String> data = dm.conVal2Str(dm.getEntities(taken, "/taken/all"));
ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
listView0.setOnItemClickListener(new OnItemClickListener(){ 	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){ try{Intent i = new Intent(DbPageActivity.this, Class.forName("b.a.TPage1Activity"));
String selected = (String) dm.getSelectedEntity(taken, "/taken/selected", es, position);
ArrayList<String> take = dm.getTakeWithSelected(taken, selected, "/taken/selected");
i.putStringArrayListExtra("taken", take);
DbPageActivity.this.startActivity(i);
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
