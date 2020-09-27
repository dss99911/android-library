package kim.jeonghyeon.sample.viewmodel

import io.ktor.http.*
import kim.jeonghyeon.client.BaseViewModel
import kim.jeonghyeon.sample.di.serviceLocator
import kim.jeonghyeon.sample.repository.UserRepository

class SignUpViewModel(val userRepo: UserRepository) : BaseViewModel() {

    //todo required for ios to create instance, currently kotlin doesn't support predefined parameter
    // if it's supported, remove this
    constructor(): this(serviceLocator.userRepository)

    val inputId = dataFlow("")
    val inputName = dataFlow("")
    val inputPassword = dataFlow("")

    fun onClickSignUp() {
        status.load {
            userRepo.signUp(inputId.value, inputPassword.value, inputName.value)
            finishSuccess()
        }
    }

    fun onClickGoogle() {
        status.load {
            userRepo.signGoogle()
        }
    }

    fun onClickFacebook() {
        status.load {
            userRepo.signFacebook()
        }
    }

    override fun onDeeplinkReceived(url: Url) {
        userRepo.onOAuthDeeplinkReceived(url)
        finishSuccess()
    }

    private fun finishSuccess() {
        //todo toast is not supported on ios. how to show ui even while screen is changed.
//        toast("success to sign up")
        goBack()
    }
}