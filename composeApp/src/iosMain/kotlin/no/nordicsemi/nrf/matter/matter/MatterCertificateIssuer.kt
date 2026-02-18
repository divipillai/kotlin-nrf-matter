package no.nordicsemi.nrf.matter.matter

import platform.Foundation.NSError
import platform.Matter.MTRDeviceAttestationInfo
import platform.Matter.MTRDeviceController
import platform.Matter.MTROperationalCSRInfo
import platform.Matter.MTROperationalCertificateChain
import platform.Matter.MTROperationalCertificateIssuerProtocol
import platform.darwin.NSObject

class MatterCertificateIssuer : NSObject(), MTROperationalCertificateIssuerProtocol {
    override fun issueOperationalCertificateForRequest(
        csrInfo: MTROperationalCSRInfo,
        attestationInfo: MTRDeviceAttestationInfo,
        controller: MTRDeviceController,
        completion: (MTROperationalCertificateChain?, NSError?) -> Unit
    ) {

    }

    override fun shouldSkipAttestationCertificateValidation(): Boolean {
        return true
    }
}
