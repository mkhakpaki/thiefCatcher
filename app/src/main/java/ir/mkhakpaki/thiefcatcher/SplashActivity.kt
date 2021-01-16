package ir.mkhakpaki.thiefcatcher

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import ir.mkhakpaki.thiefcatcher.Service.AdminReceiver

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cn = ComponentName(this, AdminReceiver::class.java)
        val mgr =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        setContentView(R.layout.activity_splash)
        if (checkPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE)&&checkPermission(Manifest.permission.CAMERA)&&mgr.isAdminActive(cn)) {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        else
        {
            val intent = Intent(this,WelcomeActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
    private fun checkPermission(permission:String):Boolean{
        return (ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED)
    }
}
