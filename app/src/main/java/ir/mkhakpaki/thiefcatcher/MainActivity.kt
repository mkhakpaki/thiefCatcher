package ir.mkhakpaki.thiefcatcher

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import ir.mkhakpaki.thiefcatcher.Service.AdminReceiver
import ir.mkhakpaki.thiefcatcher.Utils.Constant
import ir.mkhakpaki.thiefcatcher.Utils.LocaleUtils
import ir.mkhakpaki.thiefcatcher.Utils.getDate
import ir.mkhakpaki.thiefcatcher.Utils.getPath
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var sharedpref: SharedPreferences
    private lateinit var adapter:ArrayAdapter<String>
    private lateinit var listItems:ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedpref = getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)
        setContentView(R.layout.activity_main)
        styleUp()
        respondToClicks()
        val password = sharedpref.getString(Constant.PASSWORD,"")
        setupListView()
    }
    init {
        LocaleUtils.updateConfiguration(this);
    }
    override fun onResume() {
        super.onResume()
        refreshListView()
    }
    private fun setupListView(){
        listItems = arrayListOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        picturesList.adapter = adapter
        picturesList.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val encryptedFilesList = listEncryptedFiles()
                val file = encryptedFilesList?.get(position)
                val intent = Intent(this, ShowEncryptedImageActivity::class.java)
                intent.putExtra(Constant.IMAGE_NAME,file)
                startActivity(intent)
            }
        refreshListView()
    }
    private fun refreshListView(){
        listItems.clear()
        val encryptedFilesList = listEncryptedFiles()
        if (encryptedFilesList!=null) {
            for (i in encryptedFilesList.indices) {
                var fileName = encryptedFilesList[i].split("ENC_")[1]
                fileName = fileName.substring(0, fileName.length - 4)
                listItems.add(getDate(fileName.toLong())!!)
            }
            adapter.notifyDataSetChanged()
        }
    }
    private fun styleUp(){
        when(sharedpref.getInt(
            Constant.CAMERA_TO_USE,
            Constant.FRONT_CAMERA)){
            Constant.FRONT_CAMERA->radioGroup.check(R.id.frontCamera)
            Constant.REAR_CAMERA->radioGroup.check(R.id.rearCamera)
        }
    }
    private fun respondToClicks(){
        radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val editor = sharedpref.edit()
            when(checkedId){
                R.id.frontCamera->{
                    editor.putInt(
                        Constant.CAMERA_TO_USE,
                        Constant.FRONT_CAMERA)
                }
                R.id.rearCamera->{
                    editor.putInt(
                        Constant.CAMERA_TO_USE,
                        Constant.REAR_CAMERA)
                }
            }
            editor.apply()
        })

        selectPathBtn.setOnClickListener{
            selectStoragePath()
        }
        deleteBtn.setOnClickListener{
            val devAdminReceiver =
                ComponentName(this, AdminReceiver::class.java)
            val mDPM: DevicePolicyManager =
                getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            mDPM.removeActiveAdmin(devAdminReceiver)
            val packageURI =
                Uri.parse("package:" + "ir.mkhakpaki.thiefcatcher")
            val uninstallIntent =
                Intent(Intent.ACTION_DELETE, packageURI)
            startActivity(uninstallIntent)
        }
    }
    private fun selectStoragePath(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val i =
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            i.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(
                Intent.createChooser(i, "Choose directory"),
                Constant.READ_REQUEST_CODE
            )
        }
        else{
            Toast.makeText(this,getString(R.string.selectFolderForbidded),Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constant.READ_REQUEST_CODE->{

                val uri = data!!.data
                val docUri: Uri = DocumentsContract.buildDocumentUriUsingTree(
                    uri,
                    DocumentsContract.getTreeDocumentId(uri))
                val sharedpref = getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)
                val editor = sharedpref.edit()
                val path = getPath(
                    this,
                    docUri
                ) +"ThiefCather"
                File(path).mkdirs()
                editor.putString(Constant.STORAGE_PATH, path)
                editor.apply()
                refreshListView()
            }
        }
    }
    private fun listEncryptedFiles(): Array<String>? {
        val sharedpref = getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)
        val storage = sharedpref.getString(Constant.STORAGE_PATH,"")
        return File(storage!!).list()
    }
}
