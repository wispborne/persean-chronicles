package org.wisp.stories.wispLib

import kotlin.reflect.KProperty

object PersistentDataWrapper {
    operator fun get(key: String): Any? {
        val keyWithPrefix = createPrefixedKey(key)
        return di.sector.persistentData[keyWithPrefix] as? Any?
    }

    /**
     * Automatically adds mod prefix.
     */
    operator fun set(key: String, value: Any?) {
        di.sector.persistentData[createPrefixedKey(key)] = value
    }

    fun unset(key: String) {
        di.sector.persistentData.remove(createPrefixedKey(key))
    }

    private fun createPrefixedKey(key: String) = if (key.startsWith('$')) key else "$${MOD_PREFIX}_$key"
}

class PersistentNullableData<T>(private val prefKey: String?, private val defaultValue: T? = null) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return di.persistentData[prefKey ?: property.name] as? T ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        di.persistentData[prefKey ?: property.name] = value
    }
}

class PersistentData<T>(private val prefKey: String?, private val defaultValue: T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return di.persistentData[prefKey ?: property.name] as? T ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        di.persistentData[prefKey ?: property.name] = value
    }
}

class PersistentBoolean(private val prefKey: String?, private val defaultValue: Boolean = false) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return di.persistentData[prefKey ?: property.name] as? Boolean ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        di.persistentData[prefKey ?: property.name] = value
    }
}