package no.nordicsemi.nrf.matter.chip

import androidx.navigation3.ui.NavDisplay
import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ChipStructs
import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Optional
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BindingLightSwitch(
    private val chipClient: ChipClient,
) {
    suspend fun ChipClusters.AccessControlCluster.awaitReadAcl():
            ArrayList<ChipStructs.AccessControlClusterAccessControlEntryStruct?> {

        return suspendCancellableCoroutine { continuation ->

            readAclAttribute(object : ChipClusters.AccessControlCluster.AclAttributeCallback {

                override fun onSuccess(
                    valueList: List<ChipStructs.AccessControlClusterAccessControlEntryStruct?>?
                ) {
                    Napier.d { "AAA, awaitReadAcl onSuccess called" }
                    val result = ArrayList(
                        valueList ?: emptyList()
                    )

                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }

                override fun onError(ex: Exception) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(ex)
                    }
                }
            })
        }
    }

    suspend fun ChipClusters.AccessControlCluster.awaitWriteAcl(
        acl: ArrayList<ChipStructs.AccessControlClusterAccessControlEntryStruct?>
    ) {
        return suspendCancellableCoroutine { continuation ->

            writeAclAttribute(
                object : ChipClusters.DefaultClusterCallback {
                    override fun onSuccess() {
                        Napier.d { "AAA, awaitWriteAcl onSuccess called" }
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onError(ex: Exception) {
                        Napier.d { "AAA, awaitWriteAcl onError called" }
                        if (continuation.isActive) {
                            continuation.resumeWithException(ex)
                        }
                    }
                },
                acl
            )

            continuation.invokeOnCancellation {
                // No cancellation support in Matter API,
                // but good to log for debugging
                Napier.d { "awaitWriteAcl cancelled" }
            }
        }
    }

    suspend fun grantOperateAccessClusterApi(
        devicePtr: Long,
        switchNodeId: Long,
        endpoint: Int = 0
    ) {
        val cluster = ChipClusters.AccessControlCluster(devicePtr, endpoint)

        // 1. Read existing ACL
        val existingAcl = cluster.awaitReadAcl()
        Napier.d { "AAA, existingAcl size: ${existingAcl.size}" }

        // 2. Check duplicates
        val alreadyExists = existingAcl.any { entry ->
            entry?.subjects?.contains(switchNodeId) == true &&
                    entry.targets?.any {
                        it.cluster == 0x0006L
                    } == true
        }
        Napier.d { "AAA, alreadyExists: $alreadyExists" }

        if (alreadyExists) {
            Napier.d { "ACL already exists, skipping" }
            return
        }
        // Get Fabric Index
        val fabricIndex = existingAcl
            .firstOrNull { it?.fabricIndex != null }
            ?.fabricIndex

        Napier.d { "AAA, fabricIndex: $fabricIndex" }


        // 3. Create new ACL entry
        val newEntry = ChipStructs.AccessControlClusterAccessControlEntryStruct(
            /* privilege */ 3, // Operate
            /* authMode */ 2,  // CASE

            /* subjects */
            arrayListOf(switchNodeId),

            /* targets */
            arrayListOf(
                ChipStructs.AccessControlClusterTarget(
                    0x0006L, // cluster (OnOff)
                    null,    // endpoint
                    null     // deviceType
                )
            ),

            /* fabricIndex */
            fabricIndex // FIXME: According to my belief SDK will automatically assign the current fabric if we don't specify it.
        )

        Napier.d { "AAA, newEntry: $newEntry" }

        // 4. Append
        existingAcl.add(newEntry)

        // 5. Write full list
        cluster.awaitWriteAcl(existingAcl)

        Napier.d { "ACL updated successfully (cluster API)" }
    }

    suspend fun bindSwitchToLightClusterApi(
        switchNodeId: Long,
        lightNodeId: Long
    ) {
        Napier.d { "AAA, bindSwitchToLightClusterApi called" }
        val switchPtr = chipClient.getConnectedDevicePointer(switchNodeId)
        Napier.d { "AAA, switchPtr: $switchPtr" }
        val lightPtr = chipClient.getConnectedDevicePointer(lightNodeId)
        Napier.d { "AAA, lightPtr: $lightPtr" }


        // 1. ACL first (LIGHT)
        grantOperateAccessClusterApi(
            devicePtr = lightPtr,
            switchNodeId = switchNodeId
        )

        // 2. Binding (SWITCH)
        bindLightSwitchToLightClusterApi(
            devicePtr = switchPtr,
            switchEndpoint = 1,
            lightNodeId = lightNodeId
        )
    }

    suspend fun ChipClusters.BindingCluster.awaitReadBinding():
            ArrayList<ChipStructs.BindingClusterTargetStruct?> {

        return suspendCancellableCoroutine { continuation ->

            readBindingAttribute(object : ChipClusters.BindingCluster.BindingAttributeCallback {

                override fun onSuccess(
                    valueList: List<ChipStructs.BindingClusterTargetStruct?>?
                ) {
                    Napier.d { "AAA, awaitReadBinding onSuccess called" }

                    val result = ArrayList(
                        valueList ?: emptyList()
                    )

                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }

                override fun onError(ex: Exception) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(ex)
                    }
                }
            })

            continuation.invokeOnCancellation {
                Napier.d { "awaitReadBinding cancelled" }
            }
        }
    }

    suspend fun ChipClusters.BindingCluster.awaitWriteBinding(
        bindings: ArrayList<ChipStructs.BindingClusterTargetStruct?>
    ) {
        return suspendCancellableCoroutine { continuation ->

            writeBindingAttribute(
                object : ChipClusters.DefaultClusterCallback {
                    override fun onSuccess() {
                        if (continuation.isActive) {
                            Napier.d { "AAA, awaitWriteBinding onSuccess called" }
                            continuation.resume(Unit)
                        }
                    }

                    override fun onError(ex: Exception) {
                        if (continuation.isActive) {
                            Napier.d { "AAA, awaitWriteBinding onError called" }
                            continuation.resumeWithException(ex)
                        }
                    }
                },
                bindings
            )

            continuation.invokeOnCancellation {
                Napier.d { "awaitWriteBinding cancelled" }
            }
        }
    }

    suspend fun bindLightSwitchToLightClusterApi(
        devicePtr: Long,
        switchEndpoint: Int,
        lightNodeId: Long,
        lightEndpoint: Int = 1
    ) {
        Napier.d { "AAA, bindLightSwitchToLightClusterApi called" }
        val cluster = ChipClusters.BindingCluster(devicePtr, switchEndpoint)

        // 1. Read existing bindings
        val existingBindings = cluster.awaitReadBinding()
        Napier.d { "AAA, existingBindings size: ${existingBindings.size}" }

        // 2. Check duplicates
        val alreadyExists = existingBindings.any {
            it?.node?.orElse(null) == lightNodeId &&
                    it.cluster?.orElse(null) == 0x0006L &&
                    it.endpoint?.orElse(null) == lightEndpoint
        }
        Napier.d { "AAA, alreadyExists: $alreadyExists" }

        if (alreadyExists) {
            Napier.d { "Binding already exists, skipping" }
            return
        }
        // Get fabric Index
        val fabricIndex = existingBindings
            .firstOrNull { it?.fabricIndex != null }
            ?.fabricIndex
        Napier.d { "AAA, fabricIndex: $fabricIndex" }

        // 3. Create new entry
        val newEntry = ChipStructs.BindingClusterTargetStruct(
            Optional.of(lightNodeId),
            null,
            Optional.of(lightEndpoint),
            Optional.of(0x0006L),
            2, // TODO: add the fabric index value instead of null.
        )

        Napier.d { "AAA, newEntry: $newEntry" }

        existingBindings.add(newEntry)

        // 4. Write full list
        cluster.awaitWriteBinding(existingBindings)

        Napier.d { "Binding written successfully (cluster API)" }
    }
}

