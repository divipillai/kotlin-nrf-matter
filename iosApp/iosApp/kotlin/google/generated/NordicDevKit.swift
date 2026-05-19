// This file contains machine-generated code.

public import Foundation
import GoogleHomeSDK
private import SwiftProtobuf

/*
 * This file was machine generated via the code generator
 * in `codegen.clusters.swift.CustomGenerator`
 *
 */

extension NordicSemiconductor {
/// :nodoc:
  public struct NordicDevKitTrait: MatterTrait {

    /// List of the event types that are supported by `NordicDevKitTrait`.
    public static let supportedEventTypes: [Event.Type] = [
      UserButtonChangedEvent.self,
    ]

    /// List of the commands that are supported by `NordicDevKitTrait`.
    public static let supportedCommandTypes: [Command.Type] = [
      SetLedCommand.self,
    ]

    public static let identifier = NordicSemiconductor.NordicDevKitTrait.makeTraitID(for: 4294048769)

    public let metadata: TraitMetadata

    /// List of attributes for the `NordicDevKitTrait`.
    public let attributes: NordicSemiconductor.NordicDevKitTrait.Attributes

    private let interactionProxy: InteractionProxy

    public init(decoder: TraitDecoder, interactionProxy: InteractionProxy?, metadata: TraitMetadata) throws {
      guard let interactionProxy = interactionProxy else {
        throw HomeError.invalidArgument("InteractionProxy parameter required.")
      }
      let unwrappedDecoder = try decoder.unwrapPayload(namespace: Self.identifier.namespace)
      self.interactionProxy = interactionProxy
      self.attributes = try Attributes(decoder: unwrappedDecoder)
      self.metadata = metadata
    }

    // Internal for testing.
    internal init(attributes: NordicSemiconductor.NordicDevKitTrait.Attributes = .init(), interactionProxy: InteractionProxy?, metadata: TraitMetadata = .init()) throws {
      guard let interactionProxy = interactionProxy else {
        throw HomeError.invalidArgument("InteractionProxy parameter required.")
      }
      self.interactionProxy = interactionProxy
      self.attributes = attributes
      self.metadata = metadata
    }

    public func encode(with encoder: TraitEncoder) throws {
      encoder.wrapPayload(namespace: Self.identifier.namespace)
      try self.attributes.encode(with: encoder)
    }

    public func update(_ block: @Sendable (MutableAttributes) -> Void) async throws -> Self {
      let mutable = MutableAttributes(attributes: self.attributes)
      block(mutable)
      if self.interactionProxy.strictOperationValidation {
        guard self.attributes.$devKitName.isSupported || !mutable.devKitNameIsSet else {
          throw HomeError.invalidArgument("devKitName is not supported.")
        }
      }
      let updatedTrait = try NordicSemiconductor.NordicDevKitTrait(attributes: self.attributes.apply(mutable), interactionProxy: self.interactionProxy, metadata: self.metadata)
      try await self.interactionProxy.update(trait: mutable, useTimedInteraction: false)
      return updatedTrait
    }
  }
}

// MARK: - ForceReadableTrait

extension NordicSemiconductor.NordicDevKitTrait: ForceReadableTrait {
  public func forceRead() async throws {
    try await self.interactionProxy.forceRead(traitID: Self.identifier)
  }
}

// MARK: - Attributes

extension NordicSemiconductor.NordicDevKitTrait {

  /// Attributes for the `NordicDevKitTrait`.
  public struct Attributes: Sendable {
    // Attributes required at runtime.
    /** A list of the attribute IDs of the attributes supported by the cluster instance. */
    /// Nullable: false.
    @TraitAttribute public var attributeList: [UInt32]?

    /** A list of server-generated commands (server to client) which are supported by this
    cluster server instance. */
    /// Nullable: false.
    @TraitAttribute public var generatedCommandList: [UInt32]?
    /** A list of client-generated commands which are supported by this cluster server instance.
    */
    /// Nullable: false.
    @TraitAttribute public var acceptedCommandList: [UInt32]?
    /**  Whether the server supports zero or more optional cluster features. A cluster feature
    is a set of cluster elements that are mandatory or optional for a defined feature of the
    cluster. If a cluster feature is supported by the cluster instance, then the corresponding
    bit is set to 1, otherwise the bit is set to 0 (zero). */
    /// Nullable: false.
    @TraitAttribute public var featureMap: UInt32?
    /** The revision of the server cluster specification supported by the cluster instance. */
    /// Nullable: false.
    @TraitAttribute public var clusterRevision: UInt16?
    /// Nullable: false.
    @TraitAttribute public var devKitName: String?
    /// Nullable: false.
    @TraitAttribute public var userLed: Bool?
    /// Nullable: false.
    @TraitAttribute public var userButton: Bool?

    internal init(
      generatedCommandList: [UInt32]? = nil,
      acceptedCommandList: [UInt32]? = nil,
      attributeList: [UInt32]? = nil,
      featureMap: UInt32? = nil,
      clusterRevision: UInt16? = nil,
      devKitName: String? = nil,
      userLed: Bool? = nil,
      userButton: Bool? = nil
    ) {
      self._generatedCommandList = .init(
        wrappedValue: generatedCommandList,
        isSupported: attributeList?.contains(0x0FFF8) ?? false,
        isNullable: false
      )
      self._acceptedCommandList = .init(
        wrappedValue: acceptedCommandList,
        isSupported: attributeList?.contains(0x0FFF9) ?? false,
        isNullable: false
      )
      self._attributeList = .init(
        wrappedValue: attributeList,
        isSupported: attributeList?.contains(0x0FFFB) ?? false,
        isNullable: false
      )
      self._featureMap = .init(
        wrappedValue: featureMap,
        isSupported: attributeList?.contains(0x0FFFC) ?? false,
        isNullable: false
      )
      self._clusterRevision = .init(
        wrappedValue: clusterRevision,
        isSupported: attributeList?.contains(0x0FFFD) ?? false,
        isNullable: false
      )
      self._devKitName = .init(
        wrappedValue: devKitName,
        isSupported: attributeList?.contains(0x0FFF10000) ?? false,
        isNullable: false
      )
      self._userLed = .init(
        wrappedValue: userLed,
        isSupported: attributeList?.contains(0x0FFF10001) ?? false,
        isNullable: false
      )
      self._userButton = .init(
        wrappedValue: userButton,
        isSupported: attributeList?.contains(0x0FFF10002) ?? false,
        isNullable: false
      )
    }

    fileprivate init(decoder: TraitDecoder) throws {
      let decodedAttributeList: [UInt32] = try decoder.decodeOptionalArray(tag: 0x0FFFB) ?? []
      var generatedAttributeList = [UInt32]()
      generatedAttributeList.append(0x0FFFB)

      let generatedCommandListValue: [UInt32]? = try decoder.decodeOptionalArray(tag: 0x0FFF8)
      let generatedCommandListIsSupported = generatedCommandListValue != nil
      if generatedCommandListIsSupported {
        generatedAttributeList.append(0x0FFF8)
      }
      self._generatedCommandList = .init(
        wrappedValue: generatedCommandListIsSupported ? generatedCommandListValue : nil,
        isSupported: generatedCommandListIsSupported,
        isNullable: false
      )

      let acceptedCommandListValue: [UInt32]? = try decoder.decodeOptionalArray(tag: 0x0FFF9)
      let acceptedCommandListIsSupported = acceptedCommandListValue != nil
      if acceptedCommandListIsSupported {
        generatedAttributeList.append(0x0FFF9)
      }
      self._acceptedCommandList = .init(
        wrappedValue: acceptedCommandListIsSupported ? acceptedCommandListValue : nil,
        isSupported: acceptedCommandListIsSupported,
        isNullable: false
      )

      let featureMapValue: UInt32? = try decoder.decodeOptional(tag: 0x0FFFC)
      let featureMapIsSupported = featureMapValue != nil
      if featureMapIsSupported {
        generatedAttributeList.append(0x0FFFC)
      }
      self._featureMap = .init(
        wrappedValue: featureMapIsSupported ? featureMapValue : nil,
        isSupported: featureMapIsSupported,
        isNullable: false
      )

      let clusterRevisionValue: UInt16? = try decoder.decodeOptional(tag: 0x0FFFD)
      let clusterRevisionIsSupported = clusterRevisionValue != nil
      if clusterRevisionIsSupported {
        generatedAttributeList.append(0x0FFFD)
      }
      self._clusterRevision = .init(
        wrappedValue: clusterRevisionIsSupported ? clusterRevisionValue : nil,
        isSupported: clusterRevisionIsSupported,
        isNullable: false
      )

      let devKitNameValue: String? = try decoder.decodeOptional(tag: 0x0FFF10000)
      let devKitNameIsSupported = devKitNameValue != nil
      if devKitNameIsSupported {
        generatedAttributeList.append(0x0FFF10000)
      }
      self._devKitName = .init(
        wrappedValue: devKitNameIsSupported ? devKitNameValue : nil,
        isSupported: devKitNameIsSupported,
        isNullable: false
      )

      let userLedValue: Bool? = try decoder.decodeOptional(tag: 0x0FFF10001)
      let userLedIsSupported = userLedValue != nil
      if userLedIsSupported {
        generatedAttributeList.append(0x0FFF10001)
      }
      self._userLed = .init(
        wrappedValue: userLedIsSupported ? userLedValue : nil,
        isSupported: userLedIsSupported,
        isNullable: false
      )

      let userButtonValue: Bool? = try decoder.decodeOptional(tag: 0x0FFF10002)
      let userButtonIsSupported = userButtonValue != nil
      if userButtonIsSupported {
        generatedAttributeList.append(0x0FFF10002)
      }
      self._userButton = .init(
        wrappedValue: userButtonIsSupported ? userButtonValue : nil,
        isSupported: userButtonIsSupported,
        isNullable: false
      )

      self._attributeList = .init(
        wrappedValue: generatedAttributeList,
        isSupported: true,
        isNullable: false
      )
    }

    fileprivate func apply(_ update: NordicSemiconductor.NordicDevKitTrait.MutableAttributes) -> Self {
      let generatedCommandListValue = self.generatedCommandList
      let acceptedCommandListValue = self.acceptedCommandList
      let attributeListValue = self.attributeList
      let featureMapValue = self.featureMap
      let clusterRevisionValue = self.clusterRevision
      let devKitNameValue = update.devKitNameIsSet ? update.devKitName : self.devKitName
      let userLedValue = self.userLed
      let userButtonValue = self.userButton
      return NordicSemiconductor.NordicDevKitTrait.Attributes(
        generatedCommandList: generatedCommandListValue,
        acceptedCommandList: acceptedCommandListValue,
        attributeList: attributeListValue,
        featureMap: featureMapValue,
        clusterRevision: clusterRevisionValue,
        devKitName: devKitNameValue,
        userLed: userLedValue,
        userButton: userButtonValue
      )
    }

  }
}

extension NordicSemiconductor.NordicDevKitTrait.Attributes: TraitEncodable {
  public static var identifier: String { NordicSemiconductor.NordicDevKitTrait.identifier }

  public func encode(with encoder: TraitEncoder) throws {
    try encoder.encode(tag: 0x0FFF8, value: self.generatedCommandList)
    try encoder.encode(tag: 0x0FFF9, value: self.acceptedCommandList)
    try encoder.encode(tag: 0x0FFFB, value: self.attributeList)
    try encoder.encode(tag: 0x0FFFC, value: self.featureMap)
    try encoder.encode(tag: 0x0FFFD, value: self.clusterRevision)
    try encoder.encode(tag: 0x0FFF10000, value: self.devKitName)
    try encoder.encode(tag: 0x0FFF10001, value: self.userLed)
    try encoder.encode(tag: 0x0FFF10002, value: self.userButton)
  }
}

// MARK: - Hashable & Equatable

extension NordicSemiconductor.NordicDevKitTrait: Hashable {
  public static func ==(lhs: NordicSemiconductor.NordicDevKitTrait, rhs: NordicSemiconductor.NordicDevKitTrait) -> Bool {
    return lhs.identifier == rhs.identifier
      && lhs.attributes == rhs.attributes
      && lhs.metadata == rhs.metadata
  }

  public func hash(into hasher: inout Hasher) {
    hasher.combine(identifier)
    hasher.combine(attributes)
    hasher.combine(metadata)
  }
}

extension NordicSemiconductor.NordicDevKitTrait.Attributes: Hashable {
  public static func ==(lhs: NordicSemiconductor.NordicDevKitTrait.Attributes, rhs: NordicSemiconductor.NordicDevKitTrait.Attributes) -> Bool {
    var result = true
    result = lhs.generatedCommandList == rhs.generatedCommandList && result
    result = lhs.acceptedCommandList == rhs.acceptedCommandList && result
    result = lhs.attributeList == rhs.attributeList && result
    result = lhs.featureMap == rhs.featureMap && result
    result = lhs.clusterRevision == rhs.clusterRevision && result
    result = lhs.devKitName == rhs.devKitName && result
    result = lhs.userLed == rhs.userLed && result
    result = lhs.userButton == rhs.userButton && result
    return result
  }

  public func hash(into hasher: inout Hasher) {
    hasher.combine(self.generatedCommandList)
    hasher.combine(self.acceptedCommandList)
    hasher.combine(self.attributeList)
    hasher.combine(self.featureMap)
    hasher.combine(self.clusterRevision)
    hasher.combine(self.devKitName)
    hasher.combine(self.userLed)
    hasher.combine(self.userButton)
  }
}

// MARK: - MutableAttributes

extension NordicSemiconductor.NordicDevKitTrait {

  public final class MutableAttributes: TraitEncodable {
    public static let identifier: String = NordicSemiconductor.NordicDevKitTrait.identifier
    private let baseAttributes: Attributes

    fileprivate var devKitName: String?
    private(set) public var devKitNameIsSet = false
    public func setDevKitName(_ value: String) {
      self.devKitName = value
      self.devKitNameIsSet = true
    }
    public func clearDevKitName() {
      self.devKitName = nil
      self.devKitNameIsSet = false
    }

    internal init(attributes: NordicSemiconductor.NordicDevKitTrait.Attributes) {
      self.baseAttributes = attributes
    }

    public func encode(with encoder: TraitEncoder) throws {
      // MutableAttributes is encoded individually, e.g. through update(...),
      // therefore uddm wrapping needs to be applied.
      encoder.wrapPayload(namespace: Self.identifier.namespace)
      if self.devKitNameIsSet {
        try encoder.encode(tag: 0x0FFF10000, value: self.devKitName)
      }
    }
  }
}

// MARK: - Events

extension NordicSemiconductor.NordicDevKitTrait {

  // MARK: `NordicDevKitTrait.UserButtonChangedEvent`

  public struct UserButtonChangedEvent: Event, Hashable {

    /// Payload for the `UserButtonChangedEvent`.
    public struct Payload: CustomDebugStringConvertible, Hashable, Sendable {

      public init(
      ) {
      }

      public init(decoder: TraitDecoder) throws {
        self.init(
        )
      }

      public func encode(with encoder: TraitEncoder) throws {
      }

      public var debugDescription: String {
        return """
          NordicSemiconductor.NordicDevKitTrait.UserButtonChangedEvent.Payload(\
          )
          """
      }
    }

    public static let identifier = ScopedEventID(clusterID: NordicSemiconductor.NordicDevKitTrait.identifier, event: 4293984256)
    public static let name: StaticString = "UserButtonChanged"

    public let payload: Payload
    public let timestamp: TimeInterval
    public let importance: EventImportance
    public let number: UInt
    public let partID: String?

    public init(
      payload: Payload,
      timestamp: TimeInterval,
      importance: EventImportance,
      number: UInt,
      partID: String?
    ) {
      self.payload = payload
      self.timestamp = timestamp
      self.importance = importance
      self.number = number
      self.partID = partID
    }

    public init(
      decoder: TraitDecoder,
      timestamp: TimeInterval,
      importance: EventImportance,
      number: UInt,
      partID: String?
    ) throws {
      let unwrappedDecoder = try decoder.unwrapPayload(namespace: Self.identifier.namespace)
      self.payload = try Payload(decoder: unwrappedDecoder)
      self.timestamp = timestamp
      self.importance = importance
      self.number = number
      self.partID = partID
    }

    public func encode(with encoder: TraitEncoder) throws {
      encoder.wrapPayload(namespace: Self.identifier.namespace)
      try self.payload.encode(with: encoder)
    }

    public var debugDescription: String {
      return """
        NordicSemiconductor.NordicDevKitTrait.UserButtonChangedEvent(\
          payload: \(self.payload),
          timestamp: \(self.timestamp),
          importance: \(self.importance),
          number: \(self.number),
          partID: \(self.partID ?? "nil"))
        """
    }
  }
}

// MARK: - Commands

// MARK: SetLedCommand

extension NordicSemiconductor.NordicDevKitTrait {

  /// Whether the device supports the `setLed` command for this trait.
  public var supportsSetLedCommand: Bool {
    return self.attributes.acceptedCommandList?.contains(4293984256) ?? false
  }

  public func setLed(
    action: LedActionEnum
  ) async throws {
    guard !self.interactionProxy.strictOperationValidation
      || self.supportsSetLedCommand
    else {
      throw HomeError.invalidArgument("setLed command is not supported.")
    }

    let request = SetLedCommand.Request(
      action: action
    )
    try await self.interactionProxy.sendCommand(
      commandID: SetLedCommand.identifier,
      request: request,
      useTimedInteraction: false
    )
  }

  /// The batchable version of setLed command above.
  /// - SeeAlso:
  /// setLed
  public func setLedBatchable(
    action: LedActionEnum
  ) throws -> BatchableCommand<Void> {
    guard !self.interactionProxy.strictOperationValidation
      || self.supportsSetLedCommand
    else {
      throw HomeError.invalidArgument("setLed command is not supported.")
    }

    let request = SetLedCommand.Request(
      action: action
    )
    return self.interactionProxy.buildBatchableCommand(
      commandID: SetLedCommand.identifier,
      request: request,
      useTimedInteraction: false
    )
  }

  public struct SetLedCommand: Command {
    public static let identifier =
      ScopedCommandID(clusterID: NordicSemiconductor.NordicDevKitTrait.identifier, command: 4293984256)

    internal struct Request: CommandRequest {
      internal let action: LedActionEnum

      func encode(with encoder: TraitEncoder) throws {
        encoder.wrapPayload(namespace: SetLedCommand.identifier.namespace)
        try encoder.encode(tag: 0x00, value: self.action)
      }
    }
  }
}

// MARK: - Enums

extension NordicSemiconductor.NordicDevKitTrait {

  public enum LedActionEnum: UInt64, Enum8, Sendable {
    case off = 0
    case on = 1
    case toggle = 2
    // Added by codegen to be forward compatible.
    case unrecognized_ = 0xffff_ffff_ffff_ffff

    static public func unrecognizedCase() -> Self { self.unrecognized_ }
  }
}

// MARK: - Attributes definitions

extension NordicSemiconductor.NordicDevKitTrait {
  public enum Attribute: UInt32, Field, CaseIterable {
    case generatedCommandList = 65528
    case acceptedCommandList = 65529
    case attributeList = 65531
    case featureMap = 65532
    case clusterRevision = 65533
    case devKitName = 4293984256
    case userLed = 4293984257
    case userButton = 4293984258

    public var id: UInt32 {
      self.rawValue
    }

    public var type: GoogleHomeSDK.FieldType {
      switch self {
        case .generatedCommandList:
          return .uint32
        case .acceptedCommandList:
          return .uint32
        case .attributeList:
          return .uint32
        case .featureMap:
          return .uint32
        case .clusterRevision:
          return .uint16
        case .devKitName:
          return .string
        case .userLed:
          return .bool
        case .userButton:
          return .bool
      }
    }
  }

  public static func attribute(id: UInt32) -> (any Field)? {
    return Attribute(rawValue: id)
  }
}

// MARK: - Attribute fieldSelect definitions

extension TypedReference where T == NordicSemiconductor.NordicDevKitTrait {
  public var generatedCommandList: TypedExpression<[UInt32]> {
    fieldSelect(from: self, selectedField: T.Attribute.generatedCommandList)
  }
  public var acceptedCommandList: TypedExpression<[UInt32]> {
    fieldSelect(from: self, selectedField: T.Attribute.acceptedCommandList)
  }
  public var attributeList: TypedExpression<[UInt32]> {
    fieldSelect(from: self, selectedField: T.Attribute.attributeList)
  }
  public var featureMap: TypedExpression<UInt32> {
    fieldSelect(from: self, selectedField: T.Attribute.featureMap)
  }
  public var clusterRevision: TypedExpression<UInt16> {
    fieldSelect(from: self, selectedField: T.Attribute.clusterRevision)
  }
  public var devKitName: TypedExpression<String> {
    fieldSelect(from: self, selectedField: T.Attribute.devKitName)
  }
  public var userLed: TypedExpression<Bool> {
    fieldSelect(from: self, selectedField: T.Attribute.userLed)
  }
  public var userButton: TypedExpression<Bool> {
    fieldSelect(from: self, selectedField: T.Attribute.userButton)
  }
}

extension TypedExpression where V == TypedTrait<NordicSemiconductor.NordicDevKitTrait> {
  public var generatedCommandList: TypedExpression<[UInt32]> {
    fieldSelect(from: self, selectedField: NordicSemiconductor.NordicDevKitTrait.Attribute.generatedCommandList)
  }
  public var acceptedCommandList: TypedExpression<[UInt32]> {
    fieldSelect(from: self, selectedField: NordicSemiconductor.NordicDevKitTrait.Attribute.acceptedCommandList)
  }
  public var attributeList: TypedExpression<[UInt32]> {
    fieldSelect(from: self, selectedField: NordicSemiconductor.NordicDevKitTrait.Attribute.attributeList)
  }
  public var featureMap: TypedExpression<UInt32> {
    fieldSelect(from: self, selectedField: NordicSemiconductor.NordicDevKitTrait.Attribute.featureMap)
  }
  public var clusterRevision: TypedExpression<UInt16> {
    fieldSelect(from: self, selectedField: NordicSemiconductor.NordicDevKitTrait.Attribute.clusterRevision)
  }
  public var devKitName: TypedExpression<String> {
    fieldSelect(from: self, selectedField: NordicSemiconductor.NordicDevKitTrait.Attribute.devKitName)
  }
  public var userLed: TypedExpression<Bool> {
    fieldSelect(from: self, selectedField: NordicSemiconductor.NordicDevKitTrait.Attribute.userLed)
  }
  public var userButton: TypedExpression<Bool> {
    fieldSelect(from: self, selectedField: NordicSemiconductor.NordicDevKitTrait.Attribute.userButton)
  }
}

extension Updater where T == NordicSemiconductor.NordicDevKitTrait {
  public func setDevKitName(_ value: String) {
    self.set(Parameter(field: T.Attribute.devKitName, value: value))
  }
}

// MARK: - Event Fields definitions

// MARK: - Command Request Fields definitions

extension NordicSemiconductor.NordicDevKitTrait.SetLedCommand {
  public enum CommandRequestFields: UInt32, Field, CaseIterable {
    case action = 0

    public var id: UInt32 {
      self.rawValue
    }

    public var type: GoogleHomeSDK.FieldType {
      switch self {
        case .action:
          return .enum(NordicSemiconductor.NordicDevKitTrait.LedActionEnum.self)
      }
    }
  }

  public static func requestField(id: UInt32) -> (any Field)? {
    return CommandRequestFields(rawValue: id)
  }
}

// MARK: - Automation Commands

extension NordicSemiconductor.NordicDevKitTrait {

  public static func setLed(
    action: LedActionEnum
  ) -> AutomationCommand {
    var parameters: [Parameter] = []
    parameters.append(
        Parameter(
            field: Self.SetLedCommand.CommandRequestFields.action,
            value: action))

    return AutomationCommand(
      trait: Self.self,
      command: Self.SetLedCommand.self,
      commandID: Self.SetLedCommand.identifier.commandID,
      parameters: parameters)
  }
}

// MARK: - Struct Fields definitions
