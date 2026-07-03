import XCTest

final class iosAppUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testScreenshots() throws {
        let app = XCUIApplication()
        setupSnapshot(app)
        app.launch()

        snapshot("Dashboard")

        app.buttons["Bindings"].tap()
        snapshot("Bindings")
        
        app.buttons["Logs panel"].tap()
        snapshot("Logs panel")
    }
}
