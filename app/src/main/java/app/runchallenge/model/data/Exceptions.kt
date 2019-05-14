package app.runchallenge.model.data

import java.lang.Exception

class MissingPermission(val permission: String) : Exception(permission)


class NotSignedInException(message: String = "") : Exception(message)

class GooglePlayClientMissingException(word: String) : Exception(word)