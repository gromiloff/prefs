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

    internal fun getBoolean(key: String, def : Boolean = false) = this.pref?.getBoolean(key, def)
    internal fun getFloat(key: String, def : Float = 0f) = this.pref?.getFloat(key, def)
    internal fun getInt(key: String, def : Int = 0) = this.pref?.getInt(key, def)
    internal fun getLong(key: String, def : Long = 0L) = this.pref?.getLong(key, def)
    internal fun getString(key: String, def : String? = null) = this.pref?.getString(key, def)

    internal fun setBoolean(key: String, value : Boolean) {
        this.pref?.edit()?.putBoolean(key, value)?.store()
        this.prefObserver?.notifyObservers(key, value)
    }
    internal fun setFloat(key: String, value : Float) {
        this.pref?.edit()?.putFloat(key, value)?.store()
        this.prefObserver?.notifyObservers(key, value)
    }
    internal fun setInt(key: String, value : Int) {
        this.pref?.edit()?.putInt(key, value)?.store()
        this.prefObserver?.notifyObservers(key, value)
    }
    internal fun setLong(key: String, value : Long) {
        this.pref?.edit()?.putLong(key, value)?.store()
        this.prefObserver?.notifyObservers(key, value)
    }
    internal fun setString(key: String, value : String?) {
        this.pref?.edit()?.putString(key, value)?.store()
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

    private fun SharedPreferences.Editor.store() = this.apply(){
        if(Looper.myLooper() == Looper.getMainLooper()){
            apply()
        } else {
            commit()
        }
    }

    companion object {
        var disableCache = false
        private val instance : AppPref by lazy { AppPref() }

        fun init(context: Context, name: String) {
            this.instance.init(context, name)
        }

        /* getter for fields */
        fun getString(@StringRes key: Int, def : String? = null) = this.instance.getString(key.toString(), def)
        fun getBoolean(@StringRes key: Int, def : Boolean = false) = this.instance.getBoolean(key.toString(), def)
        fun getFloat(@StringRes key: Int, def : Float = 0f) = this.instance.getFloat(key.toString(), def)
        fun getInt(@StringRes key: Int, def : Int = 0) = this.instance.getInt(key.toString(), def)
        fun getLong(@StringRes key: Int, def : Long = 0L) = this.instance.getLong(key.toString(), def)

        fun getString(key: String, def : String? = null) = this.instance.getString(key, def)
        fun getBoolean(key: String, def : Boolean = false) = this.instance.getBoolean(key, def)
        fun getFloat(key: String, def : Float = 0f) = this.instance.getFloat(key, def)
        fun getInt(key: String, def : Int = 0) = this.instance.getInt(key, def)
        fun getLong(key: String, def : Long = 0L) = this.instance.getLong(key, def)

        /* setter for fields */
        fun setString(@StringRes key: Int, value : String?) {
            this.instance.setString(key.toString(), value)
        }
        fun setBoolean(@StringRes key: Int, value : Boolean) {
            this.instance.setBoolean(key.toString(), value)
        }
        fun setFloat(@StringRes key: Int, value : Float) {
            this.instance.setFloat(key.toString(), value)
        }
        fun setInt(@StringRes key: Int, value : Int) {
            this.instance.setInt(key.toString(), value)
        }
        fun setLong(@StringRes key: Int, value : Long) {
            this.instance.setLong(key.toString(), value)
        }

        /* listeners for fields */
        fun addObserver(observer : PrefObserver, @StringRes keyRes: Int? = null, keyStr: String? = null){
            this.instance.addObserver(observer, keyRes, keyStr)
        }
        fun removeObserver(observer : PrefObserver, @StringRes keyRes: Int? = null, keyStr: String? = null){
            this.instance.removeObserver(observer, keyRes, keyStr)
        }

        fun getCurrentListenersCount(@StringRes keyRes: Int? = null, keyStr: String? = null) = this.instance.observerCount(keyRes, keyStr)
    }
}
