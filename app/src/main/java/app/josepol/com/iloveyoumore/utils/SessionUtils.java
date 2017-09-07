package app.josepol.com.iloveyoumore.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by polsa on 28/08/2017.
 */

public class SessionUtils {

    private SharedPreferences sharedPreferences;

    public SessionUtils(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setId(String id) {
        sharedPreferences.edit().putString("session_id", id).commit();
    }

    public String getId() {
        return sharedPreferences.getString("session_id", "");
    }



}
