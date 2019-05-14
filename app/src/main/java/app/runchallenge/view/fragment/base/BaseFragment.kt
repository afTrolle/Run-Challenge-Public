package app.runchallenge.view.fragment.base

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import app.runchallenge.dagger.DaggerFragmentComponent
import app.runchallenge.dagger.DaggerViewModelFactory
import app.runchallenge.dagger.FragmentComponent
import app.runchallenge.dagger.FragmentModule
import app.runchallenge.model.data.toolbar.MyToolbarSettings
import app.runchallenge.view.activity.MainActivity
import javax.inject.Inject

//Don't forget to add T to component
abstract class BaseFragment : Fragment() {

    protected abstract val layout: Int

    //should only be set once
    open lateinit var viewModel: BaseViewModel

    private lateinit var fragmentComponent: FragmentComponent

    open val viewModelClass: Class<out BaseViewModel> = DefaultViewModel::class.java

    @Inject
    lateinit var viewModeFactory: DaggerViewModelFactory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mActivity = activity
        if (mActivity is MainActivity) {
            this.fragmentComponent = DaggerFragmentComponent.builder().fragmentModule(FragmentModule(this))
                .activityComponent(mActivity.activityComponent).build()
            fragmentComponent.inject(this)

            //set view model
            viewModel = ViewModelProviders.of(this, viewModeFactory).get(viewModelClass)


            viewModel.toolbarSettings.value?.hasOptionsMenu()?.let { setHasOptionsMenu(it) }


            //observe live data objects
            viewModel.toolbarSettings.observe(this, Observer { toolbarSettings ->
                updateToolbar(toolbarSettings)
            })

            viewModel.toolbarSettings.value?.let {
                updateToolbar(it)
            }

            observerNavigation()
        }
    }


    private fun observerNavigation() {
        viewModel.changeViewLiveData.observe(this, Observer { navigation ->
            when (navigation) {
                is NavigationData.ChangeView -> changeView(navigation.actionId)
                NavigationData.Back -> findNavController().popBackStack()
            }

        })
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(layout, container, false)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.viewModel.toolbarSettings.value?.optionsMenu?.let {
            inflater.inflate(it, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return viewModel.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.viewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        this.viewModel.toolbarSettings.value?.let { updateToolbar(it) }
        this.viewModel.onResume()
    }


    override fun onPause() {
        super.onPause()
        this.viewModel.onPause()
    }


}

val Fragment.supportActionBar get() = mainActivity?.supportActionBar

val Fragment.mainActivity get() = activity as MainActivity?

fun Fragment.changeView(actionId: Int) {
    findNavController().navigate(actionId)
}

/**
 * set toolbar settings
 */
fun Fragment.updateToolbar(toolbarSettings: MyToolbarSettings) {
    val actionBar = mainActivity?.supportActionBar
    if (actionBar != null) {
        if (toolbarSettings.isVisible) {
            actionBar.show()
        } else {
            actionBar.hide()
        }
        actionBar.title = getString(toolbarSettings.title)
        actionBar.setDisplayHomeAsUpEnabled(toolbarSettings.showBackButton)
        actionBar.elevation = toolbarSettings.elevation
    }
}

