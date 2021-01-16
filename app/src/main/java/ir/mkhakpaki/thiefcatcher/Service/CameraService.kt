package ir.mkhakpaki.thiefcatcher.Service

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.androidhiddencamera.CameraConfig
import com.androidhiddencamera.CameraError
import com.androidhiddencamera.HiddenCameraService
import com.androidhiddencamera.HiddenCameraUtils
import com.androidhiddencamera.config.CameraFacing
import com.androidhiddencamera.config.CameraImageFormat
import com.androidhiddencamera.config.CameraResolution
import com.androidhiddencamera.config.CameraRotation
import ir.mkhakpaki.thiefcatcher.Utils.Constant
import ir.mkhakpaki.thiefcatcher.Utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import java.util.Timer
import kotlin.concurrent.schedule

class CameraService : HiddenCameraService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                val sharedpref = getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)
                val cameraConfig: CameraConfig = CameraConfig()
                    .getBuilder(this)
                    .setCameraFacing(if(sharedpref.getInt(
                            Constant.CAMERA_TO_USE,
                            Constant.FRONT_CAMERA) == Constant.FRONT_CAMERA) CameraFacing.FRONT_FACING_CAMERA else CameraFacing.REAR_FACING_CAMERA)
                    .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                    .setImageFormat(CameraImageFormat.FORMAT_SER)
                    .setImageRotation(CameraRotation.ROTATION_0)
                    .setImageFile(File(sharedpref.getString(Constant.STORAGE_PATH,"")!!
                            + File.separator
                            + "ENC_" + System.currentTimeMillis()
                            + ".ser"))
                    .build()

                startCamera(cameraConfig)
                val password = sharedpref.getString(Constant.PASSWORD,"")
                CoroutineScope(IO).launch {
                    Log.d("tagger","capturing image")
                    withContext(Main){
                        takePicture(password)
                    }
                    Log.d("tagger","taken image")
                }
            } else {
                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this)
            }
        } else {
            showToast("Camera permission not available")
        }
        Timer("SettingUp", false).schedule(5000) {
            stopSelf()
        }
        return Service.START_NOT_STICKY

    }

    override fun onCameraError(errorCode: Int) {
        when(errorCode){
            CameraError.ERROR_CAMERA_OPEN_FAILED ->{
                Log.d("tagger","camera is being used")
            }
            CameraError.ERROR_IMAGE_WRITE_FAILED ->{
                Log.d("tagger","camera is being used")
            }
            CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE ->{
                Log.d("tagger","camera is being used")
            }
            CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION ->{
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
            CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA ->{
                Log.d("tagger","camera is being used")
            }
        }
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onImageCapture(imageFile: File) {
        stopSelf()
    }

    override fun onCreate() {
        startForeground(1, Notification())
        super.onCreate()
    }
}
