//
//  ApiExternalScreen.swift
//  kotlinIOS
//
//  Created by hyun kim on 08/08/20.
//  Copyright © 2020 hyun kim. All rights reserved.
//

import Foundation

import SwiftUI
import sample_base

struct ApiExternalScreen: SampleScreen {
   
    var model = ApiExternalViewModel()

    func content(navigator: Navigator) -> some View {
        Column {
            List(model.repoList.value as? [String] ?? [String](), id: \.self) { item in
                Text(item)
            }
            Row {
                SampleTextField("Keyword", model.input)
                Button("Call") {
                    model.onClickCall()
                }
            }
        }
        .navigationTitle("Api External Call".localized())
    }

}
