package app.runchallenge.view.fragment.sign.`in`


import android.os.Bundle
import android.view.View
import app.runchallenge.R
import app.runchallenge.model.extensions.showError
import app.runchallenge.model.data.auth.AccountError
import app.runchallenge.view.fragment.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_sign_in.*

class SignInFragment : BaseFragment() {

    override val viewModelClass: Class<SignInViewModel> get() = SignInViewModel::class.java

    override
    val layout: Int = R.layout.fragment_sign_in

    lateinit var myViewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myViewModel = viewModel as SignInViewModel

        myViewModel.liveData.observer(this,
            {},
            { error ->
                when (error) {
                    is AccountError.ResolvableError -> {
                        activity?.let { error.startResolution(it) }
                    }
                    is AccountError.UnResolvableError -> {
                        //print ignoreError
                        view?.showError(error.message)
                    }
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sign_in_button.setOnClickListener {
            myViewModel.onSignInClicked(this)
        }
    }


}
