package ir.mkhakpaki.thiefcatcher.Service

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import android.os.UserHandle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

class AdminReceiver: DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        showToast(context, "Sample Device Admin: enabled")

    }
    private fun showToast(context: Context, msg: CharSequence) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        Log.d("admin",msg.toString())
    }

    override fun onDisabled(context: Context, intent: Intent) {
        showToast(context, "Sample Device Admin: disabled")
    }

    override fun onPasswordChanged(context: Context, intent: Intent, user: UserHandle) {
        showToast(context, "Sample Device Admin: pw changed")
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        takeThiefPictureIfNeeded(context)
        super.onPasswordFailed(context,intent)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPasswordFailed(context: Context, intent: Intent, user: UserHandle) {
        takeThiefPictureIfNeeded(context)
        super.onPasswordFailed(context, intent, user)
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent, user: UserHandle) {
        showToast(context, "Sample Device Admin: pw succeeded")
    }
    private fun takeThiefPictureIfNeeded(context: Context){
        val mgr =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val no = mgr.currentFailedPasswordAttempts
        if (no >= 1) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                context.startForegroundService(Intent(context, CameraService::class.java))
            }else{
                context.startService(Intent(context, CameraService::class.java))
            }

        }
    }
}