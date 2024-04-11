package fr.azodox.gtb.util

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

    fun <T : Any> compare(key: String, value: T): Boolean {
        return cache[key] == value
    }

    fun remove(key: String) {
        cache.remove(key)
    }

    fun <T: Any> remove(key: String, value: T) {
        cache.remove(key, value)
    }
}