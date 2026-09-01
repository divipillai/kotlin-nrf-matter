# Overview and user interface

The app is build inn Compose Multiplatform, the user interface provides identical screens, labels,
and controls across both Android and iOS. The app automatically adapts to the system theme on both
platforms. The only platform-specific behavior occurs during commissioning, where execution is
handed off to the native operating system — `Google Play Services` on Android and Apple’s
`MatterSupport`
on iOS.

Upon launch, the app opens to the Dashboard. If no accessories have been commissioned, a
getting-started screen appears with options to begin setup, access Matter documentation, and view
the app version. Once a device is commissioned, the Dashboard dynamically updates to display the
list of commissioned devices.

<div align="center">
  <img src="./screenshots/dashboard_empty_android.png" alt="Dashboard with no devices on Android" />
  <img src="./screenshots/dashboard_empty_ios.png" alt="Dashboard with no devices on iOS" />
</div>

## Common interface

The following elements are present on every screen.

| UI element            | Description                                                                                                                                                                                  |
|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Top app bar           | Displays the title of the current screen.                                                                                                                                                    |
| Bottom navigation bar | The bottom navigation bar allows to switch between the three main screens: **Dashboard**, **Bindings**, and **Logs Panel**.                                                                  |
| Add device button     | A floating **+** button in the bottom-right corner allows the user to commission other matter devices. The button appears if device already has at least one commissioned device to the app. |
| Back navigation       | The system back gesture or button closes the current screen. From **Bindings** or **Logs Panel** it returns to the Dashboard; from the Dashboard it leaves the app.                          |

The top app bar title depends on the current screen.

| Screen                                      | Bottom navigation label          | Top app bar title |
|---------------------------------------------|----------------------------------|-------------------|
| Dashboard, no devices commissioned          | Dashboard                        | `nRF Matter`      |
| Dashboard, at least one device commissioned | Dashboard                        | `Dashboard`       |
| Bindings                                    | Bindings                         | `Bindings`        |
| Logs                                        | Logs Panel                       | `Logs`            |
| Commissioning                               | *(not in the bottom navigation)* | `Commissioning`   |

## Dashboard

The Dashboard is the home screen and lists every device commissioned onto the app's fabric.

### Getting-started screen

When no device has been commissioned, the Dashboard shows the following elements.

| UI element              | Description                                                                                       |
|-------------------------|---------------------------------------------------------------------------------------------------|
| **Let's get connected** | Heading, shown above an animated illustration.                                                    |
| Introductory text       | Explains that no Matter accessories have been added yet.                                          |
| **Add New Device**      | The user can begin by commissiong the matter device by adding a new device. Starts commissioning. |
| **What is Matter?**     | Opens Nordic's Matter documentation in the system browser.                                        |
| `Version: <version>`    | The application version, shown at the bottom.                                                     |

### Dashboard Device Cards

The dashboard displays a list of all commissioned devices. Each device card features an icon,
product name, and primary control at a glance. Tapping the header expands the card to access
additional controls, Matter device details, and the Remove/Decommission Device option.

<div align="center">
  <img src="./screenshots/device_card_light.png" alt="Expanded light device card" />
  <img src="./screenshots/device_information.png" alt="Matter Device Information sheet" />
</div>

| UI element                     | Description                                                                                                                                                                                                                        |
|--------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Card header                    | Device icon, title, subtitle, and the primary control for the device type, for example the on/off switch of a light. Tap to expand or collapse the card.                                                                           |
| Device-specific controls       | Shown when the card is expanded. The available controls depend on the Matter device type — see [Supported device types](#supported-device-types).                                                                                  |
| **Matter Device information**  | An expandable preview row displays the Vendor and Firmware version. Tapping this row opens the [Matter Device Information](#matter-device-information) sheet, where you can view the complete set of Basic Information attributes. |
| **Remove/Decommission Device** | Decommissions / Removes the matter accessory from the app's fabric — see [Removing a device](#removing-a-device).                                                                                                                  |

## Supported device types

The controls offered on a card are chosen from the Matter device type reported by the accessory's
Descriptor cluster.

| Device type                  | Matter device type ID | Controls available in the app                                                   |
|------------------------------|-----------------------|---------------------------------------------------------------------------------|
| On/off light                 | `0x0100`              | On/off switch, **Brightness Control** slider                                    |
| Dimmable light               | `0x0101`              | On/off switch, **Brightness Control** slider                                    |
| Door lock                    | `0x000A`              | Lock/unlock control                                                             |
| Light switch                 | `0x0103`              | None — the switch is a client node and is configured on the **Bindings** screen | 
| Manufacturer-specific device | `0xFFF10001`          | **Generate number** button, **LED** switch, button state indicator              | 
| Any other device type        | —                     | None — reported as unsupported                                                  |

Regardless of the device type, every card provides the **Matter Device information** sheet and the
**Remove/Decommission Device** button.

### Lights

Once a Light Bulb is commissioned, you can control it directly through the app—with support for both
standard On/Off and Dimmable lights. The interface includes a power switch and a brightness slider
that updates its percentage in real time as you drag. The brightness command sends as soon as you
release the slider. Because the app subscribes to level attribute updates, the control stays in sync
if the light is adjusted externally.

### Light bulb controls

The following table describes the controls available for On/Off and Dimmable lights.

* On/Off - The app writes the On/Off cluster (`0x0006`) on the accessory. It continuously
  subscribes to this attribute, ensuring the toggle switch updates in real time if the light is
  turned on or off externally.
* Dimmable - The app writes the Level Control cluster (`0x0008`). The percentage updates dynamically
  while dragging, and the command sends upon release. The app subscribes to the level attribute to
  stay synced with external changes.
* Binding capability - The light bulb can serve as a binding target device. For more information,
  see [Configuring bindings](bindings.md).

### Door lock

Once a Door Lock is commissioned, you can control it directly through the app. The interface
features a lock/unlock toggle that responds to your taps. The app subscribes to
lock state attribute updates, the control stays continuously in sync if the lock is manually or
externally operated.

### Door lock controls

* Lock/unlock - The app writes the Door Lock cluster (`0x0101`) on the accessory. It continuously
  subscribes to this attribute, ensuring the toggle switch updates in real time if the lock is
  operated externally.

### Light switches

The Light Switch device is a Matter *client* node, this light switch binds with target lighting
devices to control their light states. Because switches operate as clients, they do not expose
controllable states within the app.

### Manufacturer-specific device

This card demonstrates a vendor-defined cluster and a cluster extension, as implemented by the
nRF Connect SDK
[manufacturer-specific sample](https://github.com/nrfconnect/sdk-nrf/tree/v3.3.0/samples/matter/manufacturer_specific).

| UI element          | Description                                                                                                                                                                                                         |
|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Generate number** | Invokes a command added to the Basic Information cluster (`0x28`) by a cluster extension. The returned value is shown below as **Random number**; a placeholder is displayed until the first value arrives.         |
| **LED** switch      | Writes the manufacturer-specific cluster (`0xFFF1FC01`) to turn the LED on the development kit on or off.                                                                                                           |
| Button state        | A read-only indicator that follows a subscription to the same manufacturer-specific cluster. It reads **Press button 01** until the physical button on the kit is pressed, and **Button pressed** while it is held. |

### Unsupported device types

For device types the app does not implement, the card shows the product name, the subtitle
**Device not supported in this version of the app.**, and an explanation that the local application
profile has not been implemented yet. The **Matter Device information** sheet and the
**Remove/Decommission Device** button remain available, so the accessory can still be inspected and
removed.

## Matter Device Information

This sheet reads the accessory's Basic Information cluster (`0x0028`). The values are fetched from
the accessory when the secure session is established. Fields that the accessory does not report are
omitted.

| Field                 | Attribute |
|-----------------------|-----------|
| Product Name          | `0x0003`  |
| Vendor ID             | `0x0002`  |
| Product ID            | `0x0004`  |
| Vendor Name           | `0x0001`  |
| Software Version      | `0x0009`  |
| Serial Number         | `0x000F`  |
| Unique ID             | `0x0012`  |
| Specification Version | `0x0013`  |

**Close** dismisses the sheet.

## Removing a device

**Remove / Decommission Device** removes the accessory from the app's fabric and clears all
associated bindings.

Since the app is commissioned through Android's Google Play services and Home API, the device is
linked across all integrated fabrics (Only Applicable to Android). Decommissioning disassociates the
device across these APIs,
returning it to a factory-ready state.

Once decommissioned, the device is ready to be re-commissioned at any time by scanning its QR code
or entering the setup code.

### Force Remove

If removing the fabric from the device fails (e.g., if the device is offline), a prompt will give
you the option to Force Remove it. Force removing deletes the device from the app’s repository
immediately without waiting to unlink the fabric directly on the device.
!!! note "Note"

    Force-removing a device only clears the app's own records. The accessory keeps the fabric
    credentials it was given, so it may need to be factory reset before it can be commissioned again.
