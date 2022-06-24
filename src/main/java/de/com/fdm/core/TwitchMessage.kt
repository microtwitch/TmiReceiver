package de.com.fdm.core

data class TwitchMessage(val channel: String, val userName: String, val message: String, val tags: Map<String, String>)
