package no.nordicsemi.nrf.matter.cluster

import no.nordicsemi.nrf.matter.model.Device


fun Device.toClusters(client: MatterClient): List<Cluster> {

    val hasManufacturerSpecCluster = deviceMatterInfo.any {
        ManufacturerSpecClusterInfo.ID in it.serverClusters
    }

    return deviceMatterInfo.flatMap { info ->
        info.serverClusters.mapNotNull { clusterId ->
            when (clusterId) {
                OnOffClusterInfo.ID -> OnOffCluster(deviceId, info.endpoint, client)
                LevelControlClusterInfo.ID -> LevelControlCluster(deviceId, info.endpoint, client)
                DoorLockClusterInfo.ID -> DoorLockCluster(deviceId, info.endpoint, client)
                ManufacturerSpecClusterInfo.ID -> ManufacturerSpecCluster(deviceId, info.endpoint, client)
                BasicInfoClusterInfo.ID -> BasicInfoExtCluster(deviceId, info.endpoint, client)
                    .takeIf { hasManufacturerSpecCluster }
                else -> null
            }
        }
    }
}
