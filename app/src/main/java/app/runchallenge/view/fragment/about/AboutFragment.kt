package app.runchallenge.view.fragment.about

import app.runchallenge.R
import app.runchallenge.view.fragment.base.BaseFragment
import app.runchallenge.view.fragment.base.BaseViewModel
import app.runchallenge.view.fragment.home.HomeViewModel

class AboutFragment : BaseFragment() {
    override val layout: Int get() = R.layout.fragment_about

    override val viewModelClass = AboutViewModel::class.java


    // override val showBackButton: Boolean = true
    // override val toolbarTitle: Int = R.string.about_us
    // override val layout: Int get() = R.layout.fragment_about
}