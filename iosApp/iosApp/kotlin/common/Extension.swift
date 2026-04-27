//
//  Extension.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 23/04/2026.
//

extension [String: Any] {
    func readAny() throws -> Any {
        guard let data = self["data"] as? [String: Any],
              let value = data["value"] else {
            throw OperationError.missingAttribute
        }
        return value
    }
}
