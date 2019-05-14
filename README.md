# Run Challenge 

Run Challenge is a Realtime-running application. The goal is to allows up to 4 people to run a 2 kilometer dash against eachother. 

This project is a rewrite of a protoype that was highly unstable and was using unencrypted traffic.

## TODO 

- [x] Networking
- [x] De/Serlization 
- [x] Encryption & Decryption

- [ ] Game logic
  - [ ] Finite state machine (ready -> running -> finished) **(Working on)**
- [ ] GPS foreground service - implmenet async pushing updates
  - [ ] Update going from react to using co-routines (RxJava was a bit overkill, like killing an insect with a bazzoka) **(Working on)**
- [ ] Game Views 

- [x] Sign in & Sign out 
- [x] Simple Home View
- [x] Prefernces
- [x] Day Night Theme support



## Toolkit
This project relies on  amazing libaries used in the android community such as
* [Android Jetpack](https://developer.android.com/jetpack) - Jetpack is a suite of libraries, tools, and guidance to help developers write high-quality apps easier. 
* [Architecture Components](https://developer.android.com/topic/libraries/architecture/) - Classes for managing your UI component lifecycle and handling data persistence (part of jetpack)
* [Google play services](https://developers.google.com/android/guides/setup) - (used for [Fused-location](https://developers.google.com/location-context/fused-location-provider/) for tacking movement and [Realtime-client](https://developers.google.com/games/services/common/concepts/realtimeMultiplayer) networking)
* [Dagger 2](https://github.com/google/dagger) - Depenency injection 
* [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - Kotlin async library

* [Kotlin serialization](https://github.com/Kotlin/kotlinx.serialization) - reflection less serilzation. (using [protobuf](https://developers.google.com/protocol-buffers/))
