# Commissioning devices

Commissioning adds a Matter accessory to the app's fabric so it can be controlled. Before you start,
make sure you have a [prepared Matter device](preparing_a_matter_device.md) and, for Thread
accessories, [Thread network credentials](thread_network_credentials.md) on the phone.

## Starting commissioning

Commissioning is started with **Add New Device** on the getting-started screen, or with the **+**
button once the device list is populated. The app then shows the **Commissioning** screen with an
animation and the message **Please wait while we prepare everything.** while the platform
commissioning flow runs on top of it.

When commissioning succeeds, the app reads the accessory's Basic Information and Descriptor clusters
to determine its device type, adds it to the Dashboard, and returns to the previous screen.

!!! note "The pairing UI belongs to the operating system"

    Scanning the QR code, choosing the network, and naming the device are all handled by the
    operating system, not by this app. You never type a setup code into an app screen.

## Commissioning on Android

Commissioning goes through Google Play Services and the Android Home API:

1. The app requests commissioning, and the Google Home system flow appears.
2. In the system flow, scan the accessory's QR code or enter its setup code manually, let it join the
   Wi-Fi or Thread network, and give it a name.
3. In parallel, the app's own commissioning service adds the accessory to the app's local fabric, so
   the device ends up on both the Google Home fabric and the app's fabric.
4. The app reads the accessory's clusters and adds it to the Dashboard under the name given in the
   system flow.

## Commissioning on iOS

Commissioning goes through Apple's `MatterSupport` framework and the app extension bundled with the
app:

1. The app issues a `MatterAddDeviceRequest`, and the system commissioning sheet appears, showing the
   ecosystem as *Nordic Ecosystem* and the home as *Nordic Home*.
2. Scan the accessory's QR code in the system sheet and pick a room from the list offered by the
   extension.
3. Network selection is automatic: Wi-Fi accessories join the current system network, and Thread
   accessories join the first network found during scanning.
4. The extension commissions the accessory onto the app's local fabric, and the app reads its
   clusters and adds it to the Dashboard.

Cancelling the system sheet ends commissioning with a *Cancelled* message on the error screen.

## If commissioning fails

The **Connection Failed** screen explains that the accessory could not be paired and reports the
details needed to diagnose it.

| Field | Description |
| --- | --- |
| Commissioning id | Identifier of the commissioning attempt, useful for correlating with the log. |
| Error Code | The error reported by the platform or the Matter stack. |
| Stage | Where the failure happened: during commissioning, while reading Basic Information, or while reading the Descriptor cluster. |
| Message | The underlying error message. |

A **TROUBLESHOOTING** section suggests confirming that the accessory is in commissioning mode — its
LED flashing quickly — and that the fabric ID is configured correctly. Two buttons are available:
**Go to Logs Panel** opens the log for the full trace, and **Finish** dismisses the screen.

!!! tip "Commissioning an accessory that was paired before"

    An accessory only accepts commissioning while it is in commissioning mode, and it keeps the
    credentials of fabrics it has already joined. If a device was previously paired — including a
    device that was force-removed from this app — factory reset it before commissioning it again.
