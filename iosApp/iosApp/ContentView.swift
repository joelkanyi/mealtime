//
//  ContentView.swift
//  iosApp
//
//  Created by Joel Kanyi on 09/07/2023.
//

import SwiftUI
import shared

struct ContentView: View {
    var greeting = Greeting().greet()
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundColor(.accentColor)
            Text(greeting)
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
