import UIKit
import SwiftUI
import ComposeApp
import SwiftUI

/// Bridges the shared Compose Multiplatform UI into SwiftUI.
///
/// Wraps the Kotlin-defined `MainViewController`, injecting the native
/// `SwiftCodeProviderImpl` so the shared code can call back into the iOS-specific
/// Matter implementations.
struct ContentView: UIViewControllerRepresentable {
    
    /// Creates the Compose Multiplatform view controller, wired up with the native
    /// Swift implementation of `SwiftCodeProvider`.
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {

    }
}
