package org.wisp.stories.wispLib

import com.fs.starfarer.api.campaign.rules.MemoryAPI

class Memory(private val memoryApi: MemoryAPI) {
    operator fun get(key: String): Any? {
        val keyWithPrefix = createPrefixedKey(key)
        return memoryApi[keyWithPrefix] as? Any?
    }

    operator fun set(key: String, value: Any) {
        memoryApi[createPrefixedKey(key)] = value
    }

    fun unset(key: String) {
        memoryApi.unset(createPrefixedKey(key))
    }

    private fun createPrefixedKey(key: String) = if (key.startsWith('$')) key else "$${MOD_PREFIX}${key}"
}