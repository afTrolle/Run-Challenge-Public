package app.runchallenge.view.fragment.splash


import app.runchallenge.R
import app.runchallenge.view.fragment.base.BaseFragment
import app.runchallenge.view.fragment.base.changeView


class SplashFragment : BaseFragment() {


    override val layout: Int = R.layout.fragment_splash

    private var once: Boolean = false

    override fun onStart() {
        super.onStart()
        if (!once) {
            changeView(R.id.action_splashFragment_to_homeFragment)
            once = true
        }
    }

}
