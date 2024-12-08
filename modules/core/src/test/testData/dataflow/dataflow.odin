package dataflow

easy :: proc() {
    when ODIN_OS == .Windows || ODIN_ARCH == .i386 {
        is_windows := true
    }
    else when ODIN_OS == .Linux{
        is_linux := true
    } else {
        else_windows := true
    }

    when ODIN_OS != .Windows {
        not_windows := true
    }
}
