package no.nordicsemi.nrf.matter.matter

import platform.Foundation.NSData
import platform.Foundation.NSUserDefaults
import platform.Matter.MTRStorageProtocol
import platform.darwin.NSObject

class MatterStorage : NSObject(), MTRStorageProtocol {
    private val defaults = NSUserDefaults(suiteName = "group.P3R8YQEV4L.nordicsemi.nrf.matter")

    override fun removeStorageDataForKey(key: String): Boolean {
        defaults.removeObjectForKey(key)
        return defaults.synchronize()
    }

    override fun storageDataForKey(key: String): NSData? {
        return defaults.dataForKey(key)
    }

    override fun setStorageData(value: NSData, forKey: String): Boolean {
        defaults.setObject(value, forKey)
        return defaults.synchronize()
    }
}
