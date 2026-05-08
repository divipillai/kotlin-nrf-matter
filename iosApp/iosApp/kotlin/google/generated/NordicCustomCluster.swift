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
  public struct NordicCustomClusterTrait: MatterTrait {

    /// No supported events for `NordicCustomClusterTrait`.
    public static let supportedEventTypes: [Event.Type] = []

    /// List of the commands that are supported by `NordicCustomClusterTrait`.
    public static let supportedCommandTypes: [Command.Type] = [
      SetLedCommand.self,
    ]

    public static let identifier = NordicSemiconductor.NordicCustomClusterTrait.makeTraitID(for: 4294048769)

    public let metadata: TraitMetadata

    /// List of attributes for the `NordicCustomClusterTrait`.
    public let attributes: NordicSemiconductor.NordicCustomClusterTrait.Attributes

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
    internal init(attributes: NordicSemiconductor.NordicCustomClusterTrait.Attributes = .init(), interactionProxy: InteractionProxy?, metadata: TraitMetadata = .init()) throws {
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
        guard self.attributes.$userLed.isSupported || !mutable.userLedIsSet else {
          throw HomeError.invalidArgument("userLed is not supported.")
        }
      }
      let updatedTrait = try NordicSemiconductor.NordicCustomClusterTrait(attributes: self.attributes.apply(mutable), interactionProxy: self.interactionProxy, metadata: self.metadata)
      try await self.interactionProxy.update(trait: mutable, useTimedInteraction: false)
      return updatedTrait
    }
  }
}

// MARK: - ForceReadableTrait

extension NordicSemiconductor.NordicCustomClusterTrait: ForceReadableTrait {
  public func forceRead() async throws {
    try await self.interactionProxy.forceRead(traitID: Self.identifier)
  }
}

// MARK: - Attributes

extension NordicSemiconductor.NordicCustomClusterTrait {

  /// Attributes for the `NordicCustomClusterTrait`.
  public struct Attributes: Sendable {
    // Attributes required at runtime.
    /** A list of the attribute IDs of the attributes supported by the cluster instance. */
    /// Nullable: false.
    @TraitAttribute public var attributeList: [UInt32]?

    /// Nullable: false.
    @TraitAttribute public var developmentKitName: string?
    /// Nullable: false.
    @TraitAttribute public var userLed: Bool?
    /// Nullable: false.
    @TraitAttribute public var userButton: Bool?

    internal init(
      developmentKitName: string? = nil,
      userLed: Bool? = nil,
      userButton: Bool? = nil
    ) {
      self._developmentKitName = .init(
        wrappedValue: developmentKitName,
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

      let developmentKitNameValue: string? = try decoder.decodeOptional(tag: 0x0FFF10000)
      let developmentKitNameIsSupported = developmentKitNameValue != nil
      if developmentKitNameIsSupported {
        generatedAttributeList.append(0x0FFF10000)
      }
      self._developmentKitName = .init(
        wrappedValue: developmentKitNameIsSupported ? developmentKitNameValue : nil,
        isSupported: developmentKitNameIsSupported,
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

    fileprivate func apply(_ update: NordicSemiconductor.NordicCustomClusterTrait.MutableAttributes) -> Self {
      let developmentKitNameValue = self.developmentKitName
      let userLedValue = update.userLedIsSet ? update.userLed : self.userLed
      let userButtonValue = self.userButton
      return NordicSemiconductor.NordicCustomClusterTrait.Attributes(
        developmentKitName: developmentKitNameValue,
        userLed: userLedValue,
        userButton: userButtonValue
      )
    }

  }
}

extension NordicSemiconductor.NordicCustomClusterTrait.Attributes: TraitEncodable {
  public static var identifier: String { NordicSemiconductor.NordicCustomClusterTrait.identifier }

  public func encode(with encoder: TraitEncoder) throws {
    try encoder.encode(tag: 0x0FFF10000, value: self.developmentKitName)
    try encoder.encode(tag: 0x0FFF10001, value: self.userLed)
    try encoder.encode(tag: 0x0FFF10002, value: self.userButton)
  }
}

// MARK: - Hashable & Equatable

extension NordicSemiconductor.NordicCustomClusterTrait: Hashable {
  public static func ==(lhs: NordicSemiconductor.NordicCustomClusterTrait, rhs: NordicSemiconductor.NordicCustomClusterTrait) -> Bool {
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

extension NordicSemiconductor.NordicCustomClusterTrait.Attributes: Hashable {
  public static func ==(lhs: NordicSemiconductor.NordicCustomClusterTrait.Attributes, rhs: NordicSemiconductor.NordicCustomClusterTrait.Attributes) -> Bool {
    var result = true
    result = lhs.developmentKitName == rhs.developmentKitName && result
    result = lhs.userLed == rhs.userLed && result
    result = lhs.userButton == rhs.userButton && result
    return result
  }

  public func hash(into hasher: inout Hasher) {
    hasher.combine(self.developmentKitName)
    hasher.combine(self.userLed)
    hasher.combine(self.userButton)
  }
}

// MARK: - MutableAttributes

extension NordicSemiconductor.NordicCustomClusterTrait {

  public final class MutableAttributes: TraitEncodable {
    public static let identifier: String = NordicSemiconductor.NordicCustomClusterTrait.identifier
    private let baseAttributes: Attributes

    fileprivate var userLed: Bool?
    private(set) public var userLedIsSet = false
    public func setUserLed(_ value: Bool) {
      self.userLed = value
      self.userLedIsSet = true
    }
    public func clearUserLed() {
      self.userLed = nil
      self.userLedIsSet = false
    }

    internal init(attributes: NordicSemiconductor.NordicCustomClusterTrait.Attributes) {
      self.baseAttributes = attributes
    }

    public func encode(with encoder: TraitEncoder) throws {
      // MutableAttributes is encoded individually, e.g. through update(...),
      // therefore uddm wrapping needs to be applied.
      encoder.wrapPayload(namespace: Self.identifier.namespace)
      if self.userLedIsSet {
        try encoder.encode(tag: 0x0FFF10001, value: self.userLed)
      }
    }
  }
}


// MARK: - Commands

// MARK: SetLedCommand

extension NordicSemiconductor.NordicCustomClusterTrait {

  /// Whether the device supports the `setLed` command for this trait.
  public var supportsSetLedCommand: Bool {
    return self.attributes.acceptedCommandList?.contains(0) ?? false
  }

  public func setLed(
    state: UInt8
  ) async throws {
    guard !self.interactionProxy.strictOperationValidation
      || self.supportsSetLedCommand
    else {
      throw HomeError.invalidArgument("setLed command is not supported.")
    }

    let request = SetLedCommand.Request(
      state: state
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
    state: UInt8
  ) throws -> BatchableCommand<Void> {
    guard !self.interactionProxy.strictOperationValidation
      || self.supportsSetLedCommand
    else {
      throw HomeError.invalidArgument("setLed command is not supported.")
    }

    let request = SetLedCommand.Request(
      state: state
    )
    return self.interactionProxy.buildBatchableCommand(
      commandID: SetLedCommand.identifier,
      request: request,
      useTimedInteraction: false
    )
  }

  public struct SetLedCommand: Command {
    public static let identifier =
      ScopedCommandID(clusterID: NordicSemiconductor.NordicCustomClusterTrait.identifier, command: 0)

    internal struct Request: CommandRequest {
      internal let state: UInt8

      func encode(with encoder: TraitEncoder) throws {
        encoder.wrapPayload(namespace: SetLedCommand.identifier.namespace)
        try encoder.encode(tag: 0x00, value: self.state)
      }
    }
  }
}

// MARK: - Attributes definitions

extension NordicSemiconductor.NordicCustomClusterTrait {
  public enum Attribute: UInt32, Field, CaseIterable {
    case developmentKitName = 4293984256
    case userLed = 4293984257
    case userButton = 4293984258

    public var id: UInt32 {
      self.rawValue
    }

    public var type: GoogleHomeSDK.FieldType {
      switch self {
        case .developmentKitName:
          return .struct(String.self)
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

extension TypedReference where T == NordicSemiconductor.NordicCustomClusterTrait {
  public var developmentKitName: TypedExpression<NordicSemiconductor.NordicCustomClusterTrait.string> {
    fieldSelect(from: self, selectedField: T.Attribute.developmentKitName)
  }
  public var userLed: TypedExpression<Bool> {
    fieldSelect(from: self, selectedField: T.Attribute.userLed)
  }
  public var userButton: TypedExpression<Bool> {
    fieldSelect(from: self, selectedField: T.Attribute.userButton)
  }
}

extension Updater where T == NordicSemiconductor.NordicCustomClusterTrait {
  public func setUserLed(_ value: Bool) {
    self.set(Parameter(field: T.Attribute.userLed, value: value))
  }
}

// MARK: - Command Request Fields definitions

extension NordicSemiconductor.NordicCustomClusterTrait.SetLedCommand {
  public enum CommandRequestFields: UInt32, Field, CaseIterable {
    case state = 0

    public var id: UInt32 {
      self.rawValue
    }

    public var type: GoogleHomeSDK.FieldType {
      switch self {
        case .state:
          return .uint8
      }
    }
  }

  public static func requestField(id: UInt32) -> (any Field)? {
    return CommandRequestFields(rawValue: id)
  }
}

// MARK: - Automation Commands

extension NordicSemiconductor.NordicCustomClusterTrait {

  public static func setLed(
    state: UInt8
  ) -> AutomationCommand {
    var parameters: [Parameter] = []
    parameters.append(
        Parameter(
            field: Self.SetLedCommand.CommandRequestFields.state,
            value: state))

    return AutomationCommand(
      trait: Self.self,
      command: Self.SetLedCommand.self,
      commandID: Self.SetLedCommand.identifier.commandID,
      parameters: parameters)
  }
}

// MARK: - Struct Fields definitions
