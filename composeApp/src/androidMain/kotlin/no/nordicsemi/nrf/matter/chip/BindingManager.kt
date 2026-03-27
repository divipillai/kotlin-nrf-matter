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
    suspend fun createBinding(
        switchNodeId: Long,
        switchEndpoint: Int,
        lightNodeId: Long,
        lightEndpoint: Int,
        clusterId: Long,
    ) {
        val switchPtr = chipClient.getConnectedDevicePointer(switchNodeId)
        val lightPtr = chipClient.getConnectedDevicePointer(lightNodeId)

        // ACL (LIGHT)
        grantOperateAccess(
            devicePtr = lightPtr,
            switchNodeId = switchNodeId,
            clusterId = clusterId
        )

        // Binding (SWITCH)
        createSwitchToLightBinding(
            devicePtr = switchPtr,
            switchEndpoint = switchEndpoint,
            lightNodeId = lightNodeId,
            lightEndpoint = lightEndpoint,
            clusterId = clusterId
        )
    }

    suspend fun grantOperateAccessClusterApi(
    private suspend fun grantOperateAccess(
        devicePtr: Long,
        switchNodeId: Long,
        clusterId: Long,
    ) {
        val cluster = ChipClusters.AccessControlCluster(devicePtr, ROOT_ENDPOINT)

        // Read existing ACL
        val existingAcl = cluster.awaitReadAcl()
        Napier.d("Light has already existing ACL of size: ${existingAcl.size}", tag = TAG)
            .takeIf { existingAcl.isNotEmpty() }

        // Check duplicates
        val alreadyExists = existingAcl.any { entry ->
            entry?.subjects?.contains(switchNodeId) == true &&
                    entry.targets?.any {
                        it.cluster == clusterId
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
                    clusterId, // cluster (OnOff)
                    null,    // endpoint
                    null     // deviceType
                )
            ),
            /* fabricIndex */
            fabricIndex
        )

        // Append
        existingAcl.add(newEntry)

        // Write full list
        cluster.awaitWriteAcl(existingAcl)

        Napier.d("ACL updated successfully (cluster API)", tag = TAG)
    }

    suspend fun bindSwitchToLightClusterApi(
        switchNodeId: Long,
        lightNodeId: Long
    private suspend fun ChipClusters.AccessControlCluster.awaitReadAcl():
            ArrayList<ChipStructs.AccessControlClusterAccessControlEntryStruct?> {

        return suspendCancellableCoroutine { continuation ->

            readAclAttribute(object : ChipClusters.AccessControlCluster.AclAttributeCallback {

                override fun onSuccess(
                    valueList: List<ChipStructs.AccessControlClusterAccessControlEntryStruct?>?
                ) {
                    Napier.d("Read ACL (Access Control List) success", tag = TAG)
                    val result = ArrayList(
                        valueList ?: emptyList()
                    )

                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }

                override fun onError(ex: Exception) {
                    Napier.e("Read ACL (Access Control List) failed with exception: $ex", tag = TAG)
                    if (continuation.isActive) {
                        continuation.resumeWithException(ex)
                    }
                }
            })
            continuation.invokeOnCancellation {
                Napier.d("Read ACL (Access Control List) cancelled", tag = TAG)
            }
        }
    }

    private suspend fun ChipClusters.AccessControlCluster.awaitWriteAcl(
        acl: ArrayList<ChipStructs.AccessControlClusterAccessControlEntryStruct?>
    ) {
        return suspendCancellableCoroutine { continuation ->

            writeAclAttribute(
                object : ChipClusters.DefaultClusterCallback {
                    override fun onSuccess() {
                        Napier.d("Write ACL (Access Control List) success", tag = TAG)
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onError(ex: Exception) {
                        Napier.e(
                            "Write ACL (Access Control List) failed with exception: $ex",
                            tag = TAG
                        )
                        if (continuation.isActive) {
                            continuation.resumeWithException(ex)
                        }
                    }
                },
                acl
            )

            continuation.invokeOnCancellation {
                Napier.d("Write ACL (Access Control List) cancelled", tag = TAG)
            }
        }
    }

    suspend fun ChipClusters.BindingCluster.awaitReadBinding():
    private suspend fun ChipClusters.BindingCluster.awaitReadBinding():
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
    private suspend fun ChipClusters.BindingCluster.awaitWriteBinding(
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
    private suspend fun createSwitchToLightBinding(
        devicePtr: Long,
        switchEndpoint: Int,
        lightNodeId: Long,
        lightEndpoint: Int,
        clusterId: Long,
    ) {
        val cluster = ChipClusters.BindingCluster(devicePtr, switchEndpoint)

        // Read existing bindings
        val existingBindings = cluster.awaitReadBinding()

        // Check duplicates
        val alreadyExists = existingBindings.any {
            it?.node?.orElse(null) == lightNodeId &&
                    it.cluster?.orElse(null) == clusterId &&
                    it.endpoint?.orElse(null) == lightEndpoint
        }

        if (alreadyExists) {
            Napier.d("Binding already exists, skipping", tag = "BindingLightSwitch")
            return
        }
        // Get fabric Index
        val fabricIndex = existingBindings
            .firstOrNull { it?.fabricIndex != null }
            ?.fabricIndex
        // Save non-null fabric index locally since we are using the same fabric index for both devices.
        fabricIndex?.let {
            lightSwitchFabricIndex = it
        }

        // Create new entry (Binding Table)
        val newEntry = ChipStructs.BindingClusterTargetStruct(
            Optional.of(lightNodeId),
            null, // Taking null since for now we are using single light and switch binding.
            Optional.of(lightEndpoint),
            Optional.of(clusterId), // ON/OFF cluster
            lightSwitchFabricIndex,
        )

        existingBindings.add(newEntry)

        // Write full list
        cluster.awaitWriteBinding(existingBindings)

        Napier.d("Binding written successfully", tag = TAG)

    }

    companion object {
        private val TAG: String
            get() = "BindingLightSwitch"

        private const val ROOT_ENDPOINT: Int = 0
    }
}

