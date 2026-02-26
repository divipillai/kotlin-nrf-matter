package no.nordicsemi.nrf.matter.matter

import io.github.aakira.napier.Napier
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
        Napier.i("MatterCertificateIssuer - issueOperationalCertificateForRequest")

        val dummyCertChain: MTROperationalCertificateChain? = null // albo stwórz prawdziwy obiekt
        val error: NSError? = null

        completion(dummyCertChain, error)
    }

    override fun shouldSkipAttestationCertificateValidation(): Boolean {
        Napier.i("MatterCertificateIssuer - shouldSkipAttestationCertificateValidation")
        return true
    }
}
