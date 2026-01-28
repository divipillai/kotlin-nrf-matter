import UIKit
import SwiftUI
import ComposeApp
import SwiftUI

struct ContentView: View {
    var body: some View {
        NavigationStack {
            List {
                Text("Living Room Light")
                Text("Bedroom Outlet")
                Text("Kitchen Switch")
            }
            .navigationTitle("nRF Matter")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        print("Add Device tapped")
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
        }
        .padding(.horizontal)
    }
}

