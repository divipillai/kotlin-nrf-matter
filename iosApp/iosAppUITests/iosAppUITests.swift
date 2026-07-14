import XCTest

final class iosAppUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    /// Launches the app and captures fastlane snapshot screenshots of the Dashboard,
    /// Bindings, and Logs Panel tabs by navigating between them in turn.
    @MainActor
    func testScreenshots() throws {
        let app = XCUIApplication()
        setupSnapshot(app)
        app.launch()

        snapshot("Dashboard")

        tapTab(app, "Bindings")
        snapshot("Bindings")

        tapTab(app, "Logs Panel")
        snapshot("Logs Panel")
    }

    /// Taps the tab identified by `identifier`.
    ///
    /// Compose Multiplatform tabs aren't native UIKit buttons, so their UIAccessibility
    /// trait isn't guaranteed to be `.button`. `Modifier.testTag(...)` on the Kotlin side
    /// maps to `accessibilityIdentifier` on iOS, so match on identifier across any element
    /// type instead of assuming `app.buttons[...]`.
    @MainActor
    private func tapTab(_ app: XCUIApplication, _ identifier: String) {
        let tab = app.descendants(matching: .any).matching(identifier: identifier).firstMatch
        XCTAssertTrue(tab.waitForExistence(timeout: 5), "Tab '\(identifier)' not found")
        tab.tap()
    }
}
