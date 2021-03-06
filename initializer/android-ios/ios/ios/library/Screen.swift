//
// Created by hyun kim on 08/07/20.
// Copyright (c) 2020 hyun kim. All rights reserved.
//

import Foundation
import SwiftUI
import base

// able to customize load and error view
struct Screen<Content> : View where Content : View {
    
    let initLoading: () -> AnyView
    let initError: (_ error: ResourceError, _ retry: @escaping () -> Void) -> AnyView
    let loading: () -> AnyView
    let error: (_ error: ResourceError, _ retry: @escaping () -> Void) -> AnyView
    let model: BaseViewModel
    
    let children: () -> Content
    
    
    @State
    var changeCount = 0
    
    init(
        _ model: BaseViewModel,
        initLoading: @escaping () -> AnyView = { AnyView(ProgressView("Init Loading…")) },
        initError: @escaping (_ error: ResourceError, _ retry: @escaping () -> Void) -> AnyView = { error, retry in
            AnyView(Snackbar(message: "Init Error \(error.message ?? "nil")", buttonText: "Retry") {
                retry()
            })
        },
        error: @escaping (_ error: ResourceError, _ retry: @escaping () -> Void) -> AnyView = { error, retry in
            AnyView(Snackbar(message: "Error \(error.message ?? "nil")", buttonText: "Retry") {
                retry()
            })
        },
        loading: @escaping () -> AnyView = { AnyView(ProgressView("Loading…")) },
        children: @escaping () -> Content) {
        self.model = model
        self.children = children
        self.initLoading = initLoading
        self.initError = initError
        self.loading = loading
        self.error = error
    }
    
    
    @State
    var scope: ViewModelScope? = nil
    
    var body: some View {
        Box {
            if (changeCount < 0) {//used for refresh view
                EmptyView()
            }
            
            if (model.initStatus.asValue()?.isLoading() ?? false) {
                initLoading()
            } else if (model.initStatus.asValue()?.isError() ?? false) {
                initError(model.initStatus.value!.error()) {
                    model.initStatus.value!.retryOnError()
                }
            } else {
                children()
                
                if (model.status.asValue()?.isLoading() ?? false) {
                    loading()
                } else if (model.status.asValue()?.isError() ?? false) {
                    error(model.status.value!.error()) {
                        model.status.value!.retryOnError()
                    }
                }
            }
        }
        .onAppear {
            model.onCompose()
            scope = model.watchChanges { (data) in
                self.changeCount += 1
            }
        }
        .onDisappear {
            scope?.close()
        }
    }
}
