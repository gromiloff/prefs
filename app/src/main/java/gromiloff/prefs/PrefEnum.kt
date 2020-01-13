package gromiloff.prefs

/**
 * Created by @gromiloff on 21.06.2018
 */
interface PrefEnum<Type> {
    val name: String
    val defaultValue: Type
}