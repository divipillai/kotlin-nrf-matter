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

        snapshot("01Launch")

        // Add more `snapshot("name")` calls here as you navigate to other
        // screens you want captured, e.g.:
        // app.buttons["Devices"].tap()
        // snapshot("02Devices")
    }
}
