package ir.mkhakpaki.thiefcatcher

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_welcome.*
import kotlinx.android.synthetic.main.fragment_welcome.view.*

/**
 * A simple [Fragment] subclass.
 */
class WelcomeFragment : Fragment() {
    private lateinit var fragmentCallback: WelcomeFragmentCallback
    companion object{
        const val WELCOME_STATE=1
        const val PERMISSIONS_STATE=2
        const val DEVICE_ADMIN_STATE=3
        private const val CURRENT_STATE = "cur_state"
        fun newInstance(state: Int):WelcomeFragment{
            val args: Bundle = Bundle()
            args.putInt(CURRENT_STATE, state)
            val fragment = WelcomeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentCallback = context as WelcomeFragmentCallback
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_welcome, container, false)
        when(arguments?.getInt(CURRENT_STATE)){
            WELCOME_STATE ->setupWelcome(view)
            PERMISSIONS_STATE ->setupPermission(view)
            DEVICE_ADMIN_STATE->setupDeviceAdmin(view)
            else-> Log.e("Error","wrong state")
        }
        view.button.setOnClickListener {
            when(arguments?.getInt(CURRENT_STATE)){
                WELCOME_STATE ->fragmentCallback.onNextClicked()
                PERMISSIONS_STATE ->fragmentCallback.onGrantPermissionsClicked()
                DEVICE_ADMIN_STATE->fragmentCallback.onGrantDeviceAdminClicked()
                else-> Log.e("Error","wrong state")
            }
        }
        return view
    }
    private fun setupWelcome(view:View){
        view.titleTv.text = getString(R.string.welcome)
        view.descTv.text = getString(R.string.needPermissionDesc)
        view.button.text = getString(R.string.next)

    }
    private fun setupPermission(view:View){
        view.titleTv.text = getString(R.string.needSomePermission)
        view.descTv.text = getString(R.string.neededPermissions)
        view.button.text = getString(R.string.grantPermissions)


    }
    private fun setupDeviceAdmin(view:View){
        view.titleTv.text = getString(R.string.needDeviceAdmin)
        view.descTv.text = getString(R.string.deviceAdminDesc)
        view.button.text = getString(R.string.grantDeviceAdmin)

    }
    interface WelcomeFragmentCallback{
        fun onGrantPermissionsClicked()
        fun onGrantDeviceAdminClicked()
        fun onNextClicked()

    }
}
