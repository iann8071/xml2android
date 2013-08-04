package database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.Context;
import android.net.Uri;

public class ContentProviderManager {
	public static final String AUTHORITY = "lib.database.databasecontentprovider";
	public static final String PBASE = "/databases";
	public static final String uSB = "content://" + AUTHORITY + PBASE;
	public static final Uri u = Uri.parse(uSB);
	public static final Uri sysPIU = Uri
			.parse(uSB + "/sys_db/sys_page_index_t");
	public static final Uri sysPRU = Uri.parse(uSB
			+ "/sys_db/sys_page_record_t");

	public static ContentProvider getContentProvider(Context c) {
		return c.getContentResolver().acquireContentProviderClient(u)
				.getLocalContentProvider();
	}

	public static String getUriString(List<String> taken) {
		StringBuilder sb = new StringBuilder();
		sb.append(uSB);
		if (taken != null)
			for (int i = 0; i < taken.size() && i < 2; i++) {
				sb.append("/" + taken.get(i));
			}
		return sb.toString();
	}

	public static String getUriString(String en) {
		StringBuilder sb = new StringBuilder();
		sb.append(uSB).append(en);
		return sb.toString();
	}
}
