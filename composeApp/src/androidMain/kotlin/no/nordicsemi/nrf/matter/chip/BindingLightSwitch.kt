package no.nordicsemi.nrf.matter.chip

import chip.devicecontroller.model.ChipAttributePath
import chip.tlv.AnonymousTag
import chip.tlv.ContextSpecificTag
import chip.tlv.TlvReader
import chip.tlv.TlvWriter
import io.github.aakira.napier.Napier

data class AclEntry(
    val privilege: Int,
    val authMode: Int,
    val subjects: List<Long>,
    val cluster: Long? = null
)

class BindingLightSwitch(
    private val chipClient: ChipClient,
) {
    /**
     * Encodes a list of ACL entries into a TLV structure.
     */
    private fun encodeAcl(entries: List<AclEntry>): ByteArray {
        val writer = TlvWriter()

        writer.startArray(AnonymousTag)

        for (entry in entries) {
            writer.startStructure(AnonymousTag)

            writer.put(ContextSpecificTag(1), entry.privilege)
            writer.put(ContextSpecificTag(2), entry.authMode)

            writer.startArray(ContextSpecificTag(3))
            entry.subjects.forEach {
                writer.put(AnonymousTag, it)
            }
            writer.endArray()

            entry.cluster?.let {
                writer.startArray(ContextSpecificTag(4))
                writer.startStructure(AnonymousTag)
                writer.put(ContextSpecificTag(2), it)
                writer.endStructure()
                writer.endArray()
            }

            writer.endStructure()
        }

        writer.endArray()

        return writer.getEncoded()
    }

    /**
     * Decodes a TLV structure into a list of ACL entries.
     */
    private fun decodeAcl(tlv: ByteArray): MutableList<AclEntry> {
        val reader = TlvReader(tlv)
        val result = mutableListOf<AclEntry>()

        reader.enterArray(AnonymousTag)

        while (!reader.isEndOfContainer()) {
            reader.enterStructure(AnonymousTag)

            var privilege = 0
            var authMode = 0
            val subjects = mutableListOf<Long>()
            var cluster: Long? = null

            while (!reader.isEndOfContainer()) {
                when (reader.nextElement().tag) {
                    ContextSpecificTag(1) -> privilege = reader.getInt(ContextSpecificTag(1))
                    ContextSpecificTag(2) -> authMode = reader.getInt(ContextSpecificTag(2))

                    ContextSpecificTag(3) -> {
                        reader.enterArray(ContextSpecificTag(3))
                        while (!reader.isEndOfContainer()) {
                            subjects.add(reader.getLong(AnonymousTag))
                        }
                        reader.exitContainer()
                    }

                    ContextSpecificTag(4) -> {
                        reader.enterArray(ContextSpecificTag(4))
                        if (!reader.isEndOfContainer()) {
                            reader.enterStructure(AnonymousTag)
                            if (reader.nextElement().tag == ContextSpecificTag(2)) {
                                cluster = reader.getLong(ContextSpecificTag(2))
                            }
                            reader.exitContainer()
                        }
                        reader.exitContainer()
                    }

                    else -> reader.skipElement()
                }
            }

            reader.exitContainer()

            result.add(AclEntry(privilege, authMode, subjects, cluster))
        }

        reader.exitContainer()
        return result
    }

    /**
     * Grants Operate access to a device.
     */
    private suspend fun grantOperateAccessSafe(
        devicePtr: Long,
        switchNodeId: Long,
        lightEndpoint: Long = 0
    ) {
        val accessControlClusterId = 0x001F.toLong()
        val aclAttributeId = 0x0000.toLong()

        val attributePath = ChipAttributePath.newInstance(
            lightEndpoint,
            accessControlClusterId,
            aclAttributeId
        )

        // 1. Read existing ACL
        val existingState = chipClient.readAttribute(devicePtr, attributePath)
            ?: throw IllegalStateException("ACL read returned null")

        val existingTlv = existingState.tlv ?: throw IllegalStateException("ACL TLV missing")

        val aclEntries = decodeAcl(existingTlv)

        // 2. Check if already exists
        val alreadyExists = aclEntries.any {
            it.subjects.contains(switchNodeId) &&
                    it.cluster == 0x0006L
        }

        if (alreadyExists) {
            Napier.d { "ACL entry already exists, skipping write" }
            return
        }

        // 3. Append new entry
        val newEntry = AclEntry(
            privilege = 3,           // Operate
            authMode = 2,            // CASE
            subjects = listOf(switchNodeId),
            cluster = 0x0006L        // OnOff
        )

        aclEntries.add(newEntry)

        // 4. Encode full ACL
        val newTlv = encodeAcl(aclEntries)

        // 5. Write back full list
        chipClient.writeAttribute(devicePtr, attributePath, newTlv)

        Napier.d { "ACL updated safely with new entry" }
    }

    /**
     * Binds a switch to a light.
     */
    suspend fun bind(
        switchNodeId: Long,
        lightNodeId: Long
    ) {
        // 1. Get device pointers
        val switchPtr = chipClient.getConnectedDevicePointer(switchNodeId)
        val lightPtr = chipClient.getConnectedDevicePointer(lightNodeId)

        // 2. Grant ACL on LIGHT (first!)
        grantOperateAccessSafe(
            devicePtr = lightPtr,
            switchNodeId = switchNodeId
        )

        // 3. Bind on SWITCH
        bindLightSwitchToLight(
            devicePtr = switchPtr,
            switchEndpoint = 1,
            lightNodeId = lightNodeId,
            lightEndpoint = 1
        )
    }

    /**
     * Binds a light switch to a light.
     */
    private suspend fun bindLightSwitchToLight(
        devicePtr: Long,
        switchEndpoint: Long,
        lightNodeId: Long,
        lightEndpoint: Int = 1
    ) {
        val bindingClusterId = 0x001E.toLong()
        val bindingAttributeId = 0x0000.toLong()

        val attributePath = ChipAttributePath.newInstance(
            switchEndpoint,
            bindingClusterId,
            bindingAttributeId
        )

        // 1. Read existing bindings
        val existingState = chipClient.readAttribute(devicePtr, attributePath)
            ?: throw IllegalStateException("Binding read failed")

        val existingTlv = existingState.tlv
            ?: throw IllegalStateException("Binding TLV missing")

        val reader = TlvReader(existingTlv)
        val entries = mutableListOf<Triple<Long, Long, Int>>() // node, cluster, endpoint

        reader.enterArray(AnonymousTag)
        while (!reader.isEndOfContainer()) {
            reader.enterStructure(AnonymousTag)

            var nodeId = 0L
            var clusterId = 0L
            var endpoint = 1

            while (!reader.isEndOfContainer()) {
                when (reader.nextElement().tag) {
                    ContextSpecificTag(1) -> nodeId = reader.getLong(ContextSpecificTag(1))
                    ContextSpecificTag(2) -> clusterId = reader.getLong(ContextSpecificTag(2))
                    ContextSpecificTag(3) -> endpoint = reader.getInt(ContextSpecificTag(3))
                    else -> reader.skipElement()
                }
            }

            reader.exitContainer()
            entries.add(Triple(nodeId, clusterId, endpoint))
        }
        reader.exitContainer()

        // 2. Avoid duplicates
        val alreadyExists = entries.any {
            it.first == lightNodeId &&
                    it.second == 0x0006L &&
                    it.third == lightEndpoint
        }

        if (!alreadyExists) {
            entries.add(Triple(lightNodeId, 0x0006L, lightEndpoint))
        }

        // 3. Re-encode full list
        val writer = TlvWriter()
        writer.startArray(AnonymousTag)

        for ((nodeId, clusterId, endpoint) in entries) {
            writer.startStructure(AnonymousTag)
            writer.put(ContextSpecificTag(1), nodeId)
            writer.put(ContextSpecificTag(2), clusterId)
            writer.put(ContextSpecificTag(3), endpoint)
            writer.endStructure()
        }

        writer.endArray()

        val newTlv = writer.getEncoded()

        // 4. Write back full list
        chipClient.writeAttribute(devicePtr, attributePath, newTlv)
    }
}

