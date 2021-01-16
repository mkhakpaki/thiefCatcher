package ir.mkhakpaki.thiefcatcher.Utils

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import android.view.ContextThemeWrapper
import java.util.*


object LocaleUtils {
    private var mLocale: Locale? = null
    fun setLocale(locale: Locale?) {
        mLocale = locale
        if (mLocale != null) {
            Locale.setDefault(mLocale)
        }
    }

    fun updateConfiguration(wrapper: ContextThemeWrapper) {
        if (mLocale != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val configuration =
                Configuration()
            configuration.setLocale(mLocale)
            wrapper.applyOverrideConfiguration(configuration)
        }
    }

    fun updateConfiguration(
        application: Application,
        configuration: Configuration?
    ) {
        if (mLocale != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val config =
                Configuration(configuration)
            config.locale = mLocale
            val res = application.baseContext.resources
            res.updateConfiguration(configuration, res.displayMetrics)
        }
    }

    fun updateConfiguration(
        context: Context,
        language: String?,
        country: String?
    ) {
        val locale = Locale(language, country)
        setLocale(locale)
        if (mLocale != null) {
            val res = context.resources
            val configuration = res.configuration
            configuration.locale = mLocale
            res.updateConfiguration(configuration, res.displayMetrics)
        }
    }

}