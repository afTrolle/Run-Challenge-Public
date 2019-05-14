package app.runchallenge.dagger

import android.content.Context
import app.runchallenge.controller.repository.game.GameRepositoryImpl
import app.runchallenge.controller.repository.SettingsRepository
import app.runchallenge.controller.repository.UserRepository
import app.runchallenge.view.activity.MainActivity
import app.runchallenge.view.application.MyApplication
import app.runchallenge.view.fragment.preferences.PreferenceFragment
import app.runchallenge.view.fragment.base.BaseFragment
import dagger.Component


@ApplicationScope
@Component(modules = [ApplicationModule::class, ViewModelFactoryModule::class, ViewModelsModule::class])
interface ApplicationComponent {
    fun inject(app: MyApplication)

    fun provideContext(): Context

    //Parent component must explicitly declare objects which can be used in child components.

    fun provideUserRepository(): UserRepository

    fun provideGameRepository(): GameRepositoryImpl

    fun settingsRepository(): SettingsRepository

    fun provideViewModelFactory(): DaggerViewModelFactory
}

@ActivityScope
@Component(
    modules = [ActivityModule::class],
    dependencies = [ApplicationComponent::class]
)
interface ActivityComponent {
    fun inject(app: MainActivity)

    fun settingsRepository(): SettingsRepository
    fun provideUserRepository(): UserRepository
    fun provideViewModelFactory(): DaggerViewModelFactory

    // services

}

@FragmentScope
@Component(
    modules = [FragmentModule::class],
    dependencies = [ActivityComponent::class]
)
interface FragmentComponent {

    fun inject(preferenceFragment: PreferenceFragment)
    fun inject(baseFragment: BaseFragment)
}