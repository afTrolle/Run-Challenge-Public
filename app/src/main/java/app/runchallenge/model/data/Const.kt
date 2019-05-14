package app.runchallenge.model.data

/* multi player vars */

//number of players
const val MIN_PLAYERS: Int = 2
const val MAX_PLAYERS: Int = 4
//number of opponents
const val MIN_OPPONENTS = MIN_PLAYERS - 1
const val MAX_OPPONENTS = MAX_PLAYERS - 1
const val MIN_OPPONENTS_TO_START_GAME = MIN_OPPONENTS


/* request codes */
const val RC_SELECT_PLAYERS = 9006 //selecting players to invite to game
const val RC_WAIT_ROOM = 9005 // wait roomData for players to connect
const val RC_INVITATION_INBOX = 9008 // invite inbox, fetch game invites
const val RC_RESOLVE_GAME_ERROR = 9009 // auto resolve issues
