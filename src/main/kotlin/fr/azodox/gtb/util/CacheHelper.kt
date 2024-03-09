package fr.azodox.gtb.util

/**
 * A simple cache helper to store objects in a map
 */
object CacheHelper {
    private val cache = mutableMapOf<String, Any>()

    fun <T : Any> getOrPut(key: String, supplier: () -> T): T {
        return cache.getOrPut(key, supplier) as T
    }

    fun <T : Any> get(key: String): T? {
        return cache[key] as T?
    }

    fun <T : Any> put(key: String, value: T) {
        cache[key] = value
    }
}