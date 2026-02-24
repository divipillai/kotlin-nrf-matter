import UIKit
import SwiftUI
import ComposeApp
import SwiftUI

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(swiftCodeProvider: SwiftCodeProviderImpl())
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {

    }
}
