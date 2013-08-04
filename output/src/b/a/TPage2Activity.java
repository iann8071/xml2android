package b.a;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.EditText;
import android.text.Editable;
import java.lang.CharSequence;
import android.text.TextWatcher;
import android.content.ContentProvider;
import database.DatabaseManager;
import java.util.ArrayList;
import android.content.ContentValues;
import database.ContentProviderManager;
import android.os.Bundle;
import android.app.Activity;
public class TPage2Activity extends Activity{ ContentProvider cp;
DatabaseManager dm;
String pName = "b.a";
ArrayList<String> taken;
ArrayList<ContentValues> es;
	@Override
	protected void onCreate(Bundle savedInstanceState){ cp = ContentProviderManager.getContentProvider(this);
super.onCreate(savedInstanceState);
this.setContentView(R.layout.activity_t_page2);
taken = this.getIntent().getStringArrayListExtra("taken");
if (taken == null) { taken = new ArrayList<String>();
 };
dm = new DatabaseManager(this);
Button button0 = (Button) this.findViewById(R.id.button0);
button0.setOnClickListener(new OnClickListener(){ 	@Override
	public void onClick(View arg0){ dm.update(taken, "/taken/all", es);
TPage2Activity.this.finish();
 }
 }
);
Button button1 = (Button) this.findViewById(R.id.button1);
button1.setOnClickListener(new OnClickListener(){ 	@Override
	public void onClick(View arg0){ dm.delete(taken, "/taken/all", es);
TPage2Activity.this.finish();
 }
 }
);
TextView textView0 = (TextView) this.findViewById(R.id.textView0);
textView0.setText(dm.getEntity(taken, "/taken/1/1"));
TextView textView1 = (TextView) this.findViewById(R.id.textView1);
textView1.setText(dm.getEntity(taken, "/taken/2/1"));
 }
	@Override
	protected void onStart(){ super.onStart();
 }
	@Override
	protected void onResume(){ super.onResume();
es = dm.getAllEntities(taken, "/taken");
EditText editText0 = (EditText) this.findViewById(R.id.editText0);
editText0.addTextChangedListener(new TextWatcher(){ 	@Override
	public void afterTextChanged(Editable s){ String en = "/taken/1/2";
dm.setEntity(taken, es, en, s.toString());
 }
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after){  }
	@Override
	public void onTextChanged(CharSequence s, int start, int count, int after){  }
 }
);
editText0.setText(dm.getEntity(taken, "/taken/1/2"));
EditText editText1 = (EditText) this.findViewById(R.id.editText1);
editText1.addTextChangedListener(new TextWatcher(){ 	@Override
	public void afterTextChanged(Editable s){ String en = "/taken/2/2";
dm.setEntity(taken, es, en, s.toString());
 }
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after){  }
	@Override
	public void onTextChanged(CharSequence s, int start, int count, int after){  }
 }
);
editText1.setText(dm.getEntity(taken, "/taken/2/2"));
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
