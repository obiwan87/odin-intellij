package evaluation

import c "./conditionalImporting"

S :: c._S

easy :: proc() {
    when ODIN_OS == .Windows {
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

advanced :: proc() {
    when ODIN_OS == .Windows && ODIN_ARCH == .amd64{
        win_amd64 := S { }
        win_amd_var := win_amd64.windows_amd
    } else {
        win_i386_else := S { }
        win_i386_else_var := win_i386_else.windows
    }

    when ODIN_OS == .Windows && ODIN_ARCH == .i386{
        win_i386 := S { }
        win_i386 := win_i386.windows_i386
    }

    when ODIN_OS == .Windows && ODIN_ARCH != .i386{
        win_not_i386 := S { }
        win_not_i386_var := win_not_i386.windows_amd
    }

    when ODIN_OS == .Linux{
        linux := S { }
        linux_var := linux.linux
    }
}

