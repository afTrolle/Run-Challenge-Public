package app.runchallenge.view.fragment.about

import android.util.Log
import app.runchallenge.R
import app.runchallenge.model.data.toolbar.MyToolbarSettings
import app.runchallenge.view.fragment.base.BaseViewModel
import javax.inject.Inject

class AboutViewModel @Inject constructor() : BaseViewModel() {

    override val myToolbarSettings
        get() =
            MyToolbarSettings(title = R.string.about_us, showBackButton = true)


    override fun onCleared() {
        Log.d("NetworkMessage","about Cleared")
        super.onCleared()
    }

}