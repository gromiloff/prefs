@file:Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate", "unused")

package gromiloff.prefs

import android.content.Context
import android.content.SharedPreferences
import android.os.Parcelable
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

class AppPref {
    private class ObserverValue<Type> internal constructor(value: Type?) {
        private val obs = Vector<Observer>()
        internal var customObserverName: Observable? = null

        var value: Type? = null
            private set
        var countListener: Observer? = null

        init {
            this.value = value
        }

        fun save(value: Any) {
            this.value = value as Type
            val arrLocal = this.obs.toTypedArray()

            for (i in arrLocal.indices.reversed())
                (arrLocal[i] as Observer).update(this.customObserverName, this.value)
        }

        fun observerCount() = this.obs.size
        fun addObserver(o: Observer?) {
            if (o == null)
                throw NullPointerException()
            if (!this.obs.contains(o)) {
                this.obs.addElement(o)
            }
            this.countListener?.update(this.customObserverName, obs.size)
        }

        fun deleteObserver(o: Observer?) {
            this.obs.removeElement(o)
            this.countListener?.update(this.customObserverName, obs.size)
        }
    }

    companion object {
        private var pref: SharedPreferences? = null
        private val cache = HashMap<PrefEnum<*>, ObserverValue<*>>()

        fun init(context: Context, name: String) {
            this.pref = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        }

        fun reset(key: PrefEnum<Any>) {
            save<Any>(key, null)
            Log.d(key::class.java.simpleName, "reset $key")
        }

        fun returnIfExist(key: PrefEnum<Any>): Any? {
            var fromCache = false
            val cacheValue = this.cache[key]
            var result = if (cacheValue == null) {
                val value = this.pref?.getString(key.name, key.defaultValue.toString())

                if (value == null || value == key.defaultValue) key.defaultValue else when (key.defaultValue) {
                    is Boolean -> value.toBoolean()
                    is Double -> value.toDouble()
                    is Int -> value.toInt()
                    is Long -> value.toLong()
                    is String -> value
                    is Parcelable -> throw NoSuchMethodException()
                    is Set<*>/*, is List<*>*/ -> {
                        var a: ObjectInputStream? = null
                        var b: ByteArrayInputStream? = null
                        var ret = key.defaultValue
                        try {
                            val split = value.substring(1, value.length - 1).split(", ")
                            val array = ByteArray(split.size)
                            for (i in split.indices) {
                                array[i] = java.lang.Byte.parseByte(split[i])
                            }
                            b = ByteArrayInputStream(array)
                            a = ObjectInputStream(b)
                            ret = a.readObject()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            a?.close()
                            b?.close()
                        }
                        ret
                    }
                    else -> key.defaultValue
                }
            } else {
                fromCache = true
                if (cacheValue.value == key.defaultValue) null else cacheValue.value
            }


            // кешируем на будущий доступ
            if (!fromCache) this.cache[key] = ObserverValue(result)

            // проверка на дефольтное значение, тогда возвращаем NULL
            if(result == key.defaultValue) result = null

            Log.d(key.name, "get from " + (if (fromCache) " CACHE " else " PREFS ") + " [$result]")
            return result
        }

        fun <Type> load(key: PrefEnum<Any>) : Type {
            val result = returnIfExist(key) as? Type
            return result ?: key.defaultValue as Type
        }

        @Suppress("UNCHECKED_CAST")
        fun <Type> save(key: PrefEnum<Any>, v: Type?) {
            val editor = this.pref!!.edit()
            val requestedValue : Type = v as Type ?: key.defaultValue as Type
            val saveValue : Any? = when (key.defaultValue) {
                is Set<*>/*, is List<*>*/ -> {
                    var a: ObjectOutputStream? = null
                    val b = ByteArrayOutputStream()
                    var ret: String = "" + key.defaultValue
                    try {
                        a = ObjectOutputStream(b)
                        a.writeObject(requestedValue)
                        ret = Arrays.toString(b.toByteArray())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        a?.close()
                        b.close()
                    }
                    ret
                }
                is Parcelable -> throw NoSuchMethodException()
                else -> requestedValue
            }

            saveValue?.also {
                editor.putString(key.name, it.toString())
                editor.apply()

                this.cache[key]?.save(it)
                Log.d(key.name, "save [$it] from [$v]")
            }
        }

        fun addListenerValue(key: PrefEnum<Any>, listener: Observer, customObserverName: Observable? = null) {
            var o = this.cache[key]
            if (o == null) {
                o = ObserverValue(returnIfExist(key))
                this.cache[key] = o
            }
            o.customObserverName = customObserverName
            o.addObserver(listener)
        }

        fun removeListenerValue(key: PrefEnum<*>, listener: Observer) {
            this.cache[key]?.deleteObserver(listener)
        }

        fun getCurrentListenersCount(key: PrefEnum<*>) = this.cache[key]?.observerCount() ?: 0
        fun addListenerCountListeners(key: PrefEnum<*>, listener: Observer) {
            var o = this.cache[key]
            if (o == null) {
                o = ObserverValue(this.pref!!.getString(key.name, "" + key.defaultValue))
                this.cache[key] = o
            }
            o.countListener = listener
        }

        fun removeListenerCountListeners(key: PrefEnum<*>) {
            val o = this.cache[key]
            if (o != null) {
                o.countListener = null
            }
        }
    }
}
