import UIKit
import SwiftUI
import shared
import SwiftUI

/// Bridges the shared Compose Multiplatform UI into SwiftUI.
///
/// Wraps the Kotlin-defined `MainViewController`, which needs nothing injected from Swift: it
/// installs the logger and starts Koin itself, and the iOS-specific Matter implementations are
/// registered there as Kotlin adapters over `ios-matter`.
struct ContentView: UIViewControllerRepresentable {

    /// Creates the Compose Multiplatform view controller hosting the shared UI.
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {

    }
}
