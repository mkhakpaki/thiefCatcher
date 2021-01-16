package ir.mkhakpaki.thiefcatcher.Utils

import android.app.Application
import ir.mkhakpaki.thiefcatcher.Utils.LocaleUtils.setLocale
import ir.mkhakpaki.thiefcatcher.Utils.LocaleUtils.updateConfiguration
import java.util.*

class MyApp :Application() {
    override fun onCreate() {
        super.onCreate()
        setLocale(Locale("fa", "IR"))
        updateConfiguration(
            this,
            resources.configuration
        )
    }
}