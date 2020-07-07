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

    internal fun getBoolean(@StringRes key: Int, def : Boolean = false) = getBoolean(key.toString(), def)
    internal fun getFloat(@StringRes key: Int, def : Float = 0f) = getFloat(key.toString(), def)
    internal fun getInt(@StringRes key: Int, def : Int = 0) = getInt(key.toString(), def)
    internal fun getLong(@StringRes key: Int, def : Long = 0L) = getLong(key.toString(), def)
    internal fun getString(@StringRes key: Int, def : String? = null) = getString(key.toString(), def)

    internal fun setBoolean(@StringRes key: Int, def : Boolean = false) {
        this.pref?.edit()?.putBoolean(key.toString(), def)?.store()
    }
    internal fun setFloat(@StringRes key: Int, def : Float = 0f) {
        this.pref?.edit()?.putFloat(key.toString(), def)?.store()
    }
    internal fun setInt(@StringRes key: Int, def : Int = 0) {
        this.pref?.edit()?.putInt(key.toString(), def)?.store()
    }
    internal fun setLong(@StringRes key: Int, def : Long = 0L) {
        this.pref?.edit()?.putLong(key.toString(), def)?.store()
    }
    internal fun setString(@StringRes key: Int, def : String? = null) {
        this.pref?.edit()?.putString(key.toString(), def)?.store()
    }

    internal fun addObserver(observer : PrefObserver, @StringRes keyRes: Int? = null, keyStr: String? = null){
        if(this.prefObserver == null) this.prefObserver = ObserverValue()
        this.prefObserver?.addObserver(keyStr ?: keyRes.toString(), observer)
    }
    internal fun removeObserver(observer : PrefObserver, @StringRes keyRes: Int? = null, keyStr: String? = null){
        this.prefObserver?.deleteObserver(keyStr ?: keyRes.toString(), observer)
        if(observerCount() == 0) this.prefObserver = null
    }

    internal fun observerCount(@StringRes keyRes: Int? = null, keyStr: String? = null) = this.prefObserver?.observerCount() ?: 0

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

        fun getString(@StringRes key: Int, def : String? = null) = this.instance.getString(key, def)
        fun getBoolean(@StringRes key: Int, def : Boolean = false) = this.instance.getBoolean(key, def)
        fun getFloat(@StringRes key: Int, def : Float = 0f) = this.instance.getFloat(key, def)
        fun getInt(@StringRes key: Int, def : Int = 0) = this.instance.getInt(key, def)
        fun getLong(@StringRes key: Int, def : Long = 0L) = this.instance.getLong(key, def)

        fun getString(key: String, def : String? = null) = this.instance.getString(key, def)
        fun getBoolean(key: String, def : Boolean = false) = this.instance.getBoolean(key, def)
        fun getFloat(key: String, def : Float = 0f) = this.instance.getFloat(key, def)
        fun getInt(key: String, def : Int = 0) = this.instance.getInt(key, def)
        fun getLong(key: String, def : Long = 0L) = this.instance.getLong(key, def)

        fun addObserver(observer : PrefObserver, @StringRes keyRes: Int? = null, keyStr: String? = null){
            this.instance.addObserver(observer, keyRes, keyStr)
        }
        fun removeObserver(observer : PrefObserver, @StringRes keyRes: Int? = null, keyStr: String? = null){
            this.instance.removeObserver(observer, keyRes, keyStr)
        }

        fun getCurrentListenersCount(@StringRes keyRes: Int? = null, keyStr: String? = null) = this.instance.observerCount(keyRes, keyStr)
    }
}
