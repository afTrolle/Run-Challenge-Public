package app.runchallenge.model.data.settings

data class Setting(

    val appSettings: ApplicationSettings = ApplicationSettings(),
    val hasPhoneSetting: Boolean = false,
    val hasPermissions: Boolean = false
) {
    val hasSettings get() = hasPhoneSetting && hasPermissions
}
