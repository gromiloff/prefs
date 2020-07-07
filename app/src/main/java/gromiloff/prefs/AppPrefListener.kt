@file:Suppress("unused")

package gromiloff.prefs
import java.util.*
import kotlin.collections.HashMap

interface PrefObserver {
    fun onChanged(t: Any)
}

/* self delete after first data get */
interface SinglePrefObserver : PrefObserver

internal class ObserverValue {
    private val obs = HashMap<String, LinkedList<PrefObserver>>(10)

    fun notifyObservers(key : String, value: Any) {
        synchronized(this.obs) {
            this.obs.filter { key == it.key }.forEach {
                it.value.forEach { observer ->
                    observer.onChanged(value)
                    if(observer is SinglePrefObserver) deleteObserver(key, observer)
                }
            }
        }
    }

    fun observerCount(key: String? = null) = if(key != null) this.obs[key]?.size ?: 0 else this.obs.size

    fun addObserver(key : String, o: PrefObserver) {
        (this.obs[key] ?: LinkedList()).add(o)
    }

    fun deleteObserver(key : String, o: PrefObserver) {
        val list = this.obs[key]
        list?.remove(o)
        if(true == list?.isEmpty()) obs.remove(key)
    }
}

