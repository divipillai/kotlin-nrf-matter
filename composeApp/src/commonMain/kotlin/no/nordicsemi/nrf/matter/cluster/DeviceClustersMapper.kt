package no.nordicsemi.nrf.matter.cluster

import no.nordicsemi.nrf.matter.model.Device


fun Device.toClusters(client: MatterClient): List<Cluster> {

    val hasManufacturerSpecCluster = deviceMatterInfo.any {
        ManufacturerSpecCluster.ID in it.serverClusters
    }

    return deviceMatterInfo.flatMap { info ->
        info.serverClusters.mapNotNull { clusterId ->
            when (clusterId) {
                OnOffCluster.ID -> OnOffCluster(deviceId, info.endpoint, client)
                LevelControlCluster.ID -> LevelControlCluster(deviceId, info.endpoint, client)
                DoorLockCluster.ID -> DoorLockCluster(deviceId, info.endpoint, client)
                ManufacturerSpecCluster.ID -> ManufacturerSpecCluster(deviceId, info.endpoint, client)
                BasicInfoExtCluster.ID -> BasicInfoExtCluster(deviceId, info.endpoint, client)
                    .takeIf { hasManufacturerSpecCluster }
                else -> null
            }
        }
    }
}
