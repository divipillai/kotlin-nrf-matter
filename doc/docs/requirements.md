# Requirements

This page lists the phones, development kits, and firmware versions the application works with.

## Phone requirements

### Android

- `minSdk` 27 or newer.
- A device with Google Play Services, because the Home API is used for commissioning.
- A **physical arm64 device**. The vendored CHIP native libraries are built for `arm64-v8a` only, so
  the app does not run on an emulator.

### iOS

- iOS 26.0 or newer. Both the vendored `ios-matter` package and the Xcode targets set that as their
  minimum, because the Apple `Matter` and `MatterSupport` APIs the app relies on are only available
  there.
- Building requires an Xcode recent enough for `swift-tools-version: 6.3`, which means Xcode 26 or
  newer.

## Supported firmware

The vendored CHIP binaries are built against **Matter 1.5.0**, which was first introduced in
**nRF Connect SDK v3.2.0**. Nordic development kits running Matter firmware built with nRF Connect
SDK v3.2.0 or newer are therefore compatible for testing commissioning and control with this app.

| Development kit | SoC |
|-----------------|-----------|
| nRF52840 DK     | nRF52840  |
| nRF5340 DK      | nRF5340   |
| nRF54L15 DK     | nRF54L15  |
| nRF54LM20 DK    | nRF54LM20 |

For the authoritative, up-to-date list of supported hardware, see Nordic's
[Matter hardware and memory requirements](https://nrfconnectdocs.nordicsemi.com/addons/ncs-matter/latest/matter/getting_started/hw_requirements.html)
page — new development kits and SoCs are added there as they gain Matter support.

## Supported device types

The application implements controls for the following Matter device types. Accessories reporting any
other device type can still be commissioned and inspected, but not controlled.

| Device type | Matter device type ID |
| --- | --- |
| On/off light | `0x0100` |
| Dimmable light | `0x0101` |
| Door lock | `0x000A` |
| Light switch | `0x0103` |
| Dimmer switch | `0x0104` |
| Outlet | `0x010A` |
| Manufacturer-specific device | `0xFFF10001` |

See [Overview and user interface](overview.md#supported-device-types) for the controls offered for
each type.

## Network requirements

- Thread accessories require a Thread Border Router on the local network, and Thread network
  credentials installed on the phone. See
  [Thread network credentials](thread_network_credentials.md).
- The phone and the accessory or hub **must be on the same Wi-Fi network**. Credential and device
  discovery relies on local-network multicast (mDNS), which does not cross subnets or routers.
- The router on the network must have **IPv6 enabled**. Without it, Thread commissioning can appear
  to succeed while device control fails afterwards.
