//
//  ViewModelEx.swift
//  kotlinIOS
//
//  Created by hyun kim on 09/09/20.
//  Copyright © 2020 hyun kim. All rights reserved.
//

import Foundation
import SwiftUI
import base

prefix func + (_ flow: ViewModelFlow<NSString>) -> String {
    return flow.asValue() as String? ?? ""
}

prefix func + (_ flow: ViewModelFlow<KotlinInt>) -> Int? {
    guard let value = flow.asValue() else {
        return nil
    }
    return Int(truncating: value)
}

prefix func + <VALUE> (_ flow: ViewModelFlow<VALUE>) -> VALUE? {
    return flow.asValue()
}

prefix func ++ (_ flow: ViewModelFlow<NSString>) -> Binding<String> {
    return Binding<String>(get: { () -> String in flow.valueOrNull as String? ?? "" }, set: { (v: String) in
        if (flow.valueOrNull as String? ?? "" != v) {
            flow.tryEmit(value: NSString(utf8String: v))
        }
    })
}
