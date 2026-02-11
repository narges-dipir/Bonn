import SwiftUI
import shared

@MainActor
final class SharedHomeStore: ObservableObject {
    @Published var isLoading = false
    @Published var platform = ""
    @Published var versionMessage = ""
    @Published var videoName = ""
    @Published var termsPreview = ""
    @Published var error = ""

    private let viewModel = SharedGraph.shared.createHomeViewModel()

    func load() {
        isLoading = true
        platform = IOSPlatform().name

        Task {
            await viewModel.refresh(deviceId: "ios-app-device", versionCode: 1)
            let snapshot = viewModel.currentState()
            isLoading = false
            versionMessage = snapshot.version?.message ?? "No version response"
            videoName = snapshot.video?.name ?? "No video response"
            termsPreview = snapshot.termsPreview ?? "No terms response"
            error = snapshot.error ?? ""
        }
    }
}

struct ContentView: View {
    @StateObject private var store = SharedHomeStore()

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Bunn")
                .font(.largeTitle)
                .fontWeight(.bold)

            Text("iOS side powered by Kotlin Multiplatform shared repository + state layer.")
                .font(.footnote)
                .foregroundColor(.secondary)

            Text("Running on \(store.platform)")
                .font(.footnote)
                .foregroundColor(.secondary)

            Divider()

            if store.isLoading {
                Text("Loading shared data...")
            } else {
                Text("Version: \(store.versionMessage)")
                Text("Video: \(store.videoName)")
                Text("Terms: \(store.termsPreview)")
            }

            if !store.error.isEmpty {
                Text("Error: \(store.error)")
                    .foregroundColor(.red)
            }

            Spacer()
        }
        .padding()
        .onAppear {
            store.load()
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
