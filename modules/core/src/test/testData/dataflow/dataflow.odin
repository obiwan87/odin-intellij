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

types :: proc() {
    when ODIN_OS == .Windows {
        S :: struct {
            field: i32
        }
    }

    when ODIN_OS == .Linux {
        S :: struct {
            field: f64
        }
    }

    when ODIN_OS == .Linux {
        s := S { }
        l := s.field
    }

    when ODIN_OS == .Windows {
        s := S { }
        w := s.field
    }
}