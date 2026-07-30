import shared
import SwiftUI

/// The app's entry point, hosting the shared Compose Multiplatform UI.
@main
struct iOSApp: App {

    /// Ensures the keychain-backed Matter keypair is correctly initialized before the UI loads.
    init() {
        KeychainKt.prepareKeychain()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
        }
    }
}
