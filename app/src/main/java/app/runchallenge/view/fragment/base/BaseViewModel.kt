package app.runchallenge.view.fragment.base

import android.content.Intent
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.runchallenge.model.data.toolbar.MyToolbarSettings
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    protected open val myToolbarSettings: MyToolbarSettings =
        MyToolbarSettings()

    val toolbarSettings: MutableLiveData<MyToolbarSettings> = MutableLiveData(myToolbarSettings)

    val changeViewLiveData: NavigationLiveData = NavigationLiveData()


    fun changeView(actionId: Int) {
        changeViewLiveData.changeView(actionId)
    }

    fun navigateUp() {
        changeViewLiveData.navigateBack()
    }

    //Optional for overriding

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    open fun onResume() {

    }

    open fun onPause() {}

    open fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    open fun onActiveMediator() {}


}

sealed class NavigationData {
    data class ChangeView(val actionId: Int) : NavigationData()
    object Back : NavigationData()
    object Idle : NavigationData()
}

class NavigationLiveData : LiveData<NavigationData>(NavigationData.Idle) {
    override fun onInactive() {
        postValue(NavigationData.Idle)
    }

    fun changeView(actionId: Int) {
        postValue(NavigationData.ChangeView(actionId))
    }

    fun navigateBack() {
        postValue(NavigationData.Back)
    }

}

//default view model when no view model is needed for view
class DefaultViewModel @Inject constructor() : BaseViewModel() {

}