package app.runchallenge.dagger

import javax.inject.Qualifier

// if different objects of the same type

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationContextQualifier()


@Qualifier
annotation class ActivityContextQualifier