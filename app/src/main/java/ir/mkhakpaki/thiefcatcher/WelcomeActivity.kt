package ir.mkhakpaki.thiefcatcher

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidhiddencamera.HiddenCameraUtils
import ir.mkhakpaki.thiefcatcher.Service.AdminReceiver
import ir.mkhakpaki.thiefcatcher.Utils.Constant
import ir.mkhakpaki.thiefcatcher.Utils.LocaleUtils
import kotlinx.android.synthetic.main.activity_welcome.*
import java.util.*
import kotlin.system.exitProcess


class WelcomeActivity : AppCompatActivity(), WelcomeFragment.WelcomeFragmentCallback {
    companion object {
        private const val CAMERA_PERMISSION = 100
        private const val DEVICE_ADMIN = 200
    }

    init {
        LocaleUtils.updateConfiguration(this);
    }

    private var currentState = WelcomeFragment.WELCOME_STATE
    private var isWaitingForAdminPermission = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        respondToClicks()
        styleUp()

    }

    /**
     * checking that if device admin enabled or not and decide what to do
     */
    override fun onResume() {
        super.onResume()
        if (currentState == WelcomeFragment.DEVICE_ADMIN_STATE && isWaitingForAdminPermission) {
            val cn = ComponentName(this, AdminReceiver::class.java)
            val mgr =
                getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (mgr.isAdminActive(cn)) {
                nextBtn.isEnabled = true
                isWaitingForAdminPermission = false
                goToMainApp()

            } else {
                Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * style up the ui and init first fragment
     */
    private fun styleUp() {
        nextBtn.isEnabled = true
        currentState = WelcomeFragment.WELCOME_STATE
        prevBtn.text = getString(R.string.close)
        nextBtn.text = getString(R.string.next)
        updateProgressBarWithAnimation(20)
        val fragment = WelcomeFragment.newInstance(WelcomeFragment.WELCOME_STATE)
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_left, R.anim.slide_out_left,
            R.anim.slide_out_right, R.anim.slide_in_right
        ).replace(R.id.container, fragment).addToBackStack(null).commit();
    }

    /**
     * checking requirements and going to take permissions from user
     */
    private fun checkAndGoToPermissionsState() {
        prevBtn.text = getString(R.string.previous)
        updateProgressBarWithAnimation(40)
        currentState = WelcomeFragment.PERMISSIONS_STATE
        if ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) || ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
                    == PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    == PackageManager.PERMISSION_GRANTED))
        ) {
            goToDeviceAdminState()
        } else {
            nextBtn.isEnabled = false
            headerTitle.text = getString(R.string.permissions)
            val fragment = WelcomeFragment.newInstance(WelcomeFragment.PERMISSIONS_STATE)
            supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_left, R.anim.slide_out_left,
                R.anim.slide_out_right, R.anim.slide_in_right
            ).replace(R.id.container, fragment).addToBackStack(null).commit();
        }
    }

    /**
     * checking if permissions not granted and ask them
     */
    private fun requestPermissions() {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
                    != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                CAMERA_PERMISSION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestDrawOverApps()
        }
    }

    /**
     * after android M we should request permission to draw over other apps
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestDrawOverApps() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, 0)
    }

    private fun updateProgressBarWithAnimation(value: Int) {
        val progressAnimator =
            ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, value)
        progressAnimator.duration = 500
        progressAnimator.interpolator = LinearInterpolator()
        progressAnimator.start()
    }

    override fun onBackPressed() {
        when (currentState) {
            WelcomeFragment.PERMISSIONS_STATE -> {
                nextBtn.isEnabled = true
                currentState = WelcomeFragment.WELCOME_STATE
                prevBtn.text = getString(R.string.close)
                updateProgressBarWithAnimation(20)
                headerTitle.text = getString(R.string.welcome)
            }
            WelcomeFragment.DEVICE_ADMIN_STATE -> {
                nextBtn.isEnabled = true
                currentState = WelcomeFragment.PERMISSIONS_STATE
                prevBtn.text = getString(R.string.previous)
                updateProgressBarWithAnimation(40)
                headerTitle.text = getString(R.string.permissions)
            }

        }
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    nextBtn.isEnabled = true
                    goToDeviceAdminState()
                } else {
                    val alert = AlertDialog.Builder(this)

                    alert.setTitle(getString(R.string.permissions))
                    alert.setMessage(getString(R.string.needSomePermission))
                    alert.setNeutralButton("Ok") { dialogInterface, i -> }
                    alert.show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun goToDeviceAdminState() {
        updateProgressBarWithAnimation(60)
        currentState = WelcomeFragment.DEVICE_ADMIN_STATE
        nextBtn.isEnabled = false
        headerTitle.text = getString(R.string.deviceAdmin)
        val fragment = WelcomeFragment.newInstance(WelcomeFragment.DEVICE_ADMIN_STATE)
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_left, R.anim.slide_out_left,
            R.anim.slide_out_right, R.anim.slide_in_right
        ).replace(R.id.container, fragment).addToBackStack(null).commit();
    }

    private fun goToMainApp() {
        updateProgressBarWithAnimation(100)
        val sharedPref: SharedPreferences =
            getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(Constant.STORAGE_PATH, HiddenCameraUtils.getCacheDir(this).absolutePath)
        editor.putString(Constant.PASSWORD, UUID.randomUUID().toString())
        editor.putInt(
            Constant.CAMERA_TO_USE,
            Constant.FRONT_CAMERA
        )
        editor.apply()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun respondToClicks() {
        nextBtn.setOnClickListener {
            when (currentState) {
                WelcomeFragment.WELCOME_STATE -> {
                    checkAndGoToPermissionsState()
                }

                WelcomeFragment.PERMISSIONS_STATE -> {
                    goToDeviceAdminState()

                }
                WelcomeFragment.DEVICE_ADMIN_STATE -> {
                    goToMainApp()
                }
            }
        }
        prevBtn.setOnClickListener {
            Log.d("back", "pressed")
            when (currentState) {
                WelcomeFragment.WELCOME_STATE -> {
                    finish()
                    exitProcess(0)
                }
                else -> onBackPressed()
            }
        }
    }

    override fun onGrantPermissionsClicked() {
        requestPermissions()
    }

    override fun onGrantDeviceAdminClicked() {
        val cn = ComponentName(this, AdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.deviceAdminDesc)
            )
        }
        isWaitingForAdminPermission = true
        startActivityForResult(intent, DEVICE_ADMIN)
    }

    override fun onNextClicked() {
        checkAndGoToPermissionsState()
    }
}
