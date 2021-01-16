package ir.mkhakpaki.thiefcatcher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androidhiddencamera.Encryption
import com.androidhiddencamera.HiddenCameraUtils
import com.androidhiddencamera.config.CameraRotation
import ir.mkhakpaki.thiefcatcher.Utils.Constant
import kotlinx.android.synthetic.main.activity_show_encrypted_image.*
import java.io.*

class ShowEncryptedImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_encrypted_image)
        val fileName = intent.getStringExtra(Constant.IMAGE_NAME) //getting image name from intent
        val pref = getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE) // openning shared pref to read requirements
        val password = pref.getString(Constant.PASSWORD,"") /// getting password that used to encrypt the image
        val path = pref.getString(Constant.STORAGE_PATH,"") // getting path where images are stored
        val fullFilePath = path + File.separator+fileName // generating full path
        val file = File(fullFilePath)
        if ( file.exists()){
            var fis: FileInputStream? = null
            var inputStream: ObjectInputStream? = null
            try {
                fis = FileInputStream(fullFilePath)
                inputStream = ObjectInputStream(fis)
                val myHashMap =
                    inputStream.readObject() as? HashMap<String,ByteArray>
                val decrypted = myHashMap?.let { Encryption().decrypt(it, password!!.toCharArray()) }

                //Convert byte array to bitmap
                var bitmap =
                    decrypted?.size?.let { BitmapFactory.decodeByteArray(decrypted, 0, it) }
                //Rotate the bitmap
                val rotatedBitmap: Bitmap
                    rotatedBitmap =
                        bitmap?.let { HiddenCameraUtils.rotateBitmap(it, CameraRotation.ROTATION_270) }!!
                    bitmap = null

                imageView.setImageBitmap(rotatedBitmap)

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: StreamCorruptedException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fis?.close()
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
