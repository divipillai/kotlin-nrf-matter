package no.nordicsemi.nrf.matter.chip

import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ChipStructs
import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Optional
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BindingManager(
    private val chipClient: ChipClient,
) {
    private var lightSwitchFabricIndex: Int? = null

    suspend fun ChipClusters.AccessControlCluster.awaitReadAcl():
            ArrayList<ChipStructs.AccessControlClusterAccessControlEntryStruct?> {

        return suspendCancellableCoroutine { continuation ->

            readAclAttribute(object : ChipClusters.AccessControlCluster.AclAttributeCallback {

                override fun onSuccess(
                    valueList: List<ChipStructs.AccessControlClusterAccessControlEntryStruct?>?
                ) {
                    Napier.d("Read ACL (LIGHT) success", tag = TAG)
                    val result = ArrayList(
                        valueList ?: emptyList()
                    )

                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }

                override fun onError(ex: Exception) {
                    Napier.e("Read ACL (LIGHT) failed with exception: $ex", tag = TAG)
                    if (continuation.isActive) {
                        continuation.resumeWithException(ex)
                    }
                }
            })
            continuation.invokeOnCancellation {
                Napier.d("Read ACL (LIGHT) cancelled", tag = TAG)
            }
        }
    }

    suspend fun ChipClusters.AccessControlCluster.awaitWriteAcl(
        acl: ArrayList<ChipStructs.AccessControlClusterAccessControlEntryStruct?>
    ) {
        return suspendCancellableCoroutine { continuation ->

            writeAclAttribute(
                object : ChipClusters.DefaultClusterCallback {
                    override fun onSuccess() {
                        Napier.d("Write ACL (LIGHT) success", tag = TAG)
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onError(ex: Exception) {
                        Napier.e("Write ACL (LIGHT) failed with exception: $ex", tag = TAG)
                        if (continuation.isActive) {
                            continuation.resumeWithException(ex)
                        }
                    }
                },
                acl
            )

            continuation.invokeOnCancellation {
                Napier.d("Write ACL (LIGHT) cancelled", tag = TAG)
            }
        }
    }

    suspend fun grantOperateAccessClusterApi(
        devicePtr: Long,
        switchNodeId: Long,
        endpoint: Int = 0
    ) {
        val cluster = ChipClusters.AccessControlCluster(devicePtr, endpoint)

        // Read existing ACL
        val existingAcl = cluster.awaitReadAcl()
        Napier.d("Light has already existing ACL of size: ${existingAcl.size}", tag = TAG)
            .takeIf { existingAcl.isNotEmpty() }

        // Check duplicates
        val alreadyExists = existingAcl.any { entry ->
            entry?.subjects?.contains(switchNodeId) == true &&
                    entry.targets?.any {
                        it.cluster == 0x0006L
                    } == true
        }

        if (alreadyExists) {
            Napier.d("ACL already exists, skipping", tag = TAG)
            return
        }
        // Get Fabric Index
        val fabricIndex = existingAcl
            .firstOrNull { it?.fabricIndex != null }
            ?.fabricIndex

        // Save non-null fabric index locally since we are using the same fabric index for both devices.
        fabricIndex?.let {
            Napier.d("Fabric Index: $it", tag = TAG)
            lightSwitchFabricIndex = it
        }


        // Create new ACL entry
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
            fabricIndex
        )

        Napier.d { "AAA, newEntry: $newEntry" }

        // Append
        existingAcl.add(newEntry)

        // Write full list
        cluster.awaitWriteAcl(existingAcl)

        Napier.d("ACL updated successfully (cluster API)", tag = TAG)
    }

    suspend fun bindSwitchToLightClusterApi(
        switchNodeId: Long,
        lightNodeId: Long
    ) {
        Napier.d { "AAA, bindSwitchToLightClusterApi called" }
        val switchPtr = chipClient.getConnectedDevicePointer(switchNodeId)
        val lightPtr = chipClient.getConnectedDevicePointer(lightNodeId)

        // ACL first (LIGHT)
        grantOperateAccessClusterApi(
            devicePtr = lightPtr,
            switchNodeId = switchNodeId
        )

        // Binding (SWITCH)
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
                    Napier.d("Read Binding success", tag = TAG)
                    val result = ArrayList(
                        valueList ?: emptyList()
                    )

                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }

                override fun onError(ex: Exception) {
                    Napier.e("Read Binding failed with exception: $ex", tag = TAG)
                    if (continuation.isActive) {
                        continuation.resumeWithException(ex)
                    }
                }
            })

            continuation.invokeOnCancellation {
                Napier.d("Read Binding cancelled", tag = TAG)
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
                        Napier.d("Write Binding success", tag = TAG)
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onError(ex: Exception) {
                        Napier.e("Write Binding failed with exception: $ex", tag = TAG)
                        if (continuation.isActive) {
                            continuation.resumeWithException(ex)
                        }
                    }
                },
                bindings
            )

            continuation.invokeOnCancellation {
                Napier.d("Write Binding cancelled", tag = TAG)
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

        // Read existing bindings
        val existingBindings = cluster.awaitReadBinding()

        // Check duplicates
        val alreadyExists = existingBindings.any {
            it?.node?.orElse(null) == lightNodeId &&
                    it.cluster?.orElse(null) == 0x0006L &&
                    it.endpoint?.orElse(null) == lightEndpoint
        }

        if (alreadyExists) {
            Napier.d("Binding already exists, skipping", tag = "BindingLightSwitch")
            return
        }
        // Get fabric Index
        val fabricIndex = existingBindings
            .firstOrNull { it?.fabricIndex != null }
            ?.fabricIndex // TODO: If its null then use the fabric index of the light.
        Napier.d { "AAA, fabricIndex: $fabricIndex" }
        // Save non-null fabric index locally since we are using the same fabric index for both devices.
        fabricIndex?.let {
            lightSwitchFabricIndex = it
            Napier.d { "AAA, lightSwitchFabricIndex: $lightSwitchFabricIndex" }
        }

        // 3. Create new entry
        val newEntry = ChipStructs.BindingClusterTargetStruct(
            Optional.of(lightNodeId),
            null, // Taking null since for now we are using single light and switch binding.
            Optional.of(lightEndpoint),
            Optional.of(0x0006L), // ON/OFF cluster
            lightSwitchFabricIndex,
        )

        Napier.d { "AAA, newEntry: $newEntry" }

        existingBindings.add(newEntry)

        // Write full list
        cluster.awaitWriteBinding(existingBindings)

        Napier.d { "Binding written successfully (cluster API)" }

    }

    companion object {
        private val TAG: String
            get() = "BindingLightSwitch"
    }
}

