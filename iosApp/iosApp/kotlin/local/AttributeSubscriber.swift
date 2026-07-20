//
//  AttributeSubscriber.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/04/2026.
//

import Matter
import SharedCode

/// A single update delivered by an attribute subscription, carrying either a parsed value or an error.
struct AttributeUpdate<T : Sendable> : Sendable {
    /// The parsed attribute value, if the update succeeded.
    let value: T?
    /// The error reported for this update, if the read failed.
    let error: Error?
}

/// Observes attribute reports for a single Matter node via a shared, cached `MTRDevice`.
///
/// `MTRDevice` maintains one persistent, self-resubscribing report stream per node, unlike the
/// lower-level `MTRBaseDevice`/`MTRBaseCluster*` APIs where every attribute opens its own
/// independent subscription. Every observer of the same node must share one `AttributeSubscriber`
/// instance (use `shared(deviceId:)`.
///
/// Using multiple delegates on the same device causes visible lags in received subscriptions.
class AttributeSubscriber: NSObject, MTRDeviceDelegate {

    /// Guarded by `cacheLock`; the concurrency checker can't see that, so this is manually
    /// asserted safe rather than isolated to an actor.
    nonisolated(unsafe) private static var instances: [NSNumber: AttributeSubscriber] = [:]
    private static let cacheLock = NSLock()

    /// Returns the shared observer for the given node, creating it if necessary.
    ///
    /// - Parameter deviceId: The Matter node ID of the target device.
    /// - Throws: An error if the local controller cannot be obtained.
    static func shared(deviceId: NSNumber) throws -> AttributeSubscriber {
        cacheLock.lock()
        defer { cacheLock.unlock() }

        if let existing = instances[deviceId] {
            return existing
        }

        let created = try AttributeSubscriber(deviceId: deviceId)
        instances[deviceId] = created
        return created
    }

    private struct Registration {
        let endpoint: NSNumber
        let cluster: NSNumber
        let attribute: NSNumber
        let handle: (Any) -> Void
    }

    private let device: MTRDevice
    private let registrationsLock = NSLock()
    private var registrations: [Registration] = []

    private init(deviceId: NSNumber) throws {
        let controller = try LocalControllerProvider(logTag: "AttributeSubscriber").getController()
        device = MTRDevice(nodeID: deviceId, controller: controller)
        super.init()
        device.add(self, queue: DispatchQueue.global())
    }

    /// Registers interest in a single attribute and parses each report into `T`.
    ///
    /// Reports that fail to parse are silently ignored; `onUpdate` is only called for values
    /// that parse successfully. Reports arrive as soon as `MTRDevice` receives them, including
    /// the initial priming report right after the subscription is established.
    ///
    /// - Parameters:
    ///   - endpoint: The endpoint ID hosting the attribute.
    ///   - cluster: The cluster ID the attribute belongs to.
    ///   - attribute: The attribute ID to observe.
    ///   - onUpdate: Called on a background queue with each successfully parsed value.
    func subscribe<T: AttributeParser>(endpoint: NSNumber, cluster: NSNumber, attribute: NSNumber, onUpdate: @escaping (T) -> Void) {
        let registration = Registration(endpoint: endpoint, cluster: cluster, attribute: attribute, handle: { raw in
            if let value = try? T.parse(value: raw) {
                onUpdate(value)
            }
        })

        registrationsLock.lock()
        registrations.append(registration)
        registrationsLock.unlock()
    }

    func device(_ device: MTRDevice, receivedAttributeReport attributeReport: [[String: Any]]) {
        registrationsLock.lock()
        let currentRegistrations = registrations
        registrationsLock.unlock()

        for entry in attributeReport {
            guard let path = entry[MTRAttributePathKey] as? MTRAttributePath,
                  let raw = try? entry.readAny() else {
                continue
            }

            for registration in currentRegistrations where registration.endpoint == path.endpoint
                && registration.cluster == path.cluster
                && registration.attribute == path.attribute {
                registration.handle(raw)
            }
        }
    }

    func device(_ device: MTRDevice, stateChanged state: MTRDeviceState) {
        SharedLogger.debug("AttributeSubscriber device state changed: \(state.rawValue)")
    }

    func device(_ device: MTRDevice, receivedEventReport eventReport: [[String: Any]]) {
        SharedLogger.debug("AttributeSubscriber received event report: \(eventReport)")
    }
}
