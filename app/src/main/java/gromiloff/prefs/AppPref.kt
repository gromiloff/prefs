@file:Suppress("unused")

package gromiloff.prefs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.annotation.StringRes

@SuppressLint("CommitPrefEdits")
data class AppPref(private var prefs : SharedPreferences? = null) {
    private var pref: SharedPreferences? = null
    private var prefObserver : ObserverValue? = null

    internal fun init(context: Context, name: String) {
        this.pref = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    internal fun getBoolean(key: String, def : Boolean = false) = this.pref?.getBoolean(key, def) ?: def
    internal fun getFloat(key: String, def : Float = 0f) = this.pref?.getFloat(key, def) ?: def
    internal fun getInt(key: String, def : Int = 0) = this.pref?.getInt(key, def) ?: def
    internal fun getLong(key: String, def : Long = 0L) = this.pref?.getLong(key, def) ?: def
    internal fun getString(key: String, def : String) = this.pref?.getString(key, def) ?: def

    internal fun setBoolean(key: String, value : Boolean) {
        store { it.putBoolean(key, value) }
        this.prefObserver?.notifyObservers(key, value)
    }
    internal fun setFloat(key: String, value : Float) {
        store { it.putFloat(key, value) }
        this.prefObserver?.notifyObservers(key, value)
    }
    internal fun setInt(key: String, value : Int) {
        store { it.putInt(key, value) }
        this.prefObserver?.notifyObservers(key, value)
    }
    internal fun setLong(key: String, value : Long) {
        store { it.putLong(key, value) }
        this.prefObserver?.notifyObservers(key, value)
    }
    internal fun setString(key: String, value : String?) {
        store { it.putString(key, value) }
        this.prefObserver?.notifyObservers(key, value)
    }

    internal fun addObserver(observer : PrefObserver, @StringRes keyRes: Int? = null, keyStr: String? = null){
        if(this.prefObserver == null) this.prefObserver = ObserverValue()
        this.prefObserver?.addObserver(keyStr ?: keyRes.toString(), observer)
    }
    internal fun removeObserver(observer : PrefObserver, @StringRes keyRes: Int? = null, keyStr: String? = null){
        if(this.prefObserver?.deleteObserver(keyStr ?: keyRes.toString(), observer) == 0) this.prefObserver = null
    }

    internal fun observerCount(@StringRes keyRes: Int? = null, keyStr: String? = null) = this.prefObserver?.observerCount(keyStr ?: keyRes.toString()) ?: 0

    @SuppressLint("ApplySharedPref")
    private fun store(func : (editor : SharedPreferences.Editor) -> Unit) {
        this.pref?.edit()?.apply {
            func(this)
            if (Looper.myLooper() == Looper.getMainLooper()) {
                apply()
            } else {
                commit()
            }
        }
    }

    companion object {
        var disableCache = false
        private val instance : AppPref by lazy { AppPref() }

        fun init(context: Context, name: String) {
            this.instance.init(context, name)
        }

        /* getter for fields */
        fun <T : PrefEnum<*>> getString(key: T, defaultValue : String?) = this.instance.getString(
            key.name,
            defaultValue ?: key.defaultValue as String
        )
        fun <T : PrefEnum<*>> getBoolean(key: T) = this.instance.getBoolean(key.name, key.defaultValue as Boolean)
        fun <T : PrefEnum<*>> getFloat(key: T)   = this.instance.getFloat(key.name, key.defaultValue as Float)
        fun <T : PrefEnum<*>> getInt(key: T)     = this.instance.getInt(key.name, key.defaultValue as Int)
        fun <T : PrefEnum<*>> getLong(key: T)    = this.instance.getLong(key.name, key.defaultValue as Long)

        /* setter for fields */
        fun <T : PrefEnum<*>> setString(key: T, value : String?) {
            this.instance.setString(key.toString(), value)
        }
        fun <T : PrefEnum<*>> setBoolean(key: T, value : Boolean) {
            this.instance.setBoolean(key.toString(), value)
        }
        fun <T : PrefEnum<*>> setFloat(key: T, value : Float) {
            this.instance.setFloat(key.toString(), value)
        }
        fun <T : PrefEnum<*>> setInt(key: T, value : Int) {
            this.instance.setInt(key.toString(), value)
        }
        fun <T : PrefEnum<*>> setLong(key: T, value : Long) {
            this.instance.setLong(key.toString(), value)
        }


        fun <T : PrefEnum<*>> resetString(key: T) { this.instance.getString(key.name, key.defaultValue as String) }

        /* listeners for fields */
        /*fun addObserver(observer : PrefObserver, keyRes: PrefEnum<*>? = null, keyStr: String? = null){
            this.instance.addObserver(observer, keyRes, keyStr)
        }
        fun removeObserver(observer : PrefObserver, keyRes: PrefEnum<*>? = null, keyStr: String? = null){
            this.instance.removeObserver(observer, keyRes, keyStr)
        }*/

        fun getCurrentListenersCount(@StringRes keyRes: Int? = null, keyStr: String? = null) = this.instance.observerCount(keyRes, keyStr)
    }
}
