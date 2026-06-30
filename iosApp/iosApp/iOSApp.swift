import SharedCode
import SwiftUI

@main
struct iOSApp: App {
    
    init() {
        KeypairInitializer.initKeychain()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
        }
    }
}
