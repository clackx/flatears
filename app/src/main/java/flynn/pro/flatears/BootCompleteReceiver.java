package flynn.pro.flatears;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by clackx on 08.06.16.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    public BootCompleteReceiver() {
    }
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            // :: IF BOOT COMPLETED - CHECK REACH SERVERS
            //NetworkUtil.updateStatus(context);
            // TODO:: START APP MINIMIZED

        }
    }
}