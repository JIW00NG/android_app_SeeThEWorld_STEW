package jbnu.moblie.app.one.team.STEW;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
            Toast.makeText(context, "[BroadcastReceiver] " + action  , Toast.LENGTH_SHORT).show();
        }

}
