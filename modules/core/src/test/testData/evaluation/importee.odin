package evaluation

import c "./conditionalImporting"
import "./shared"

S :: c._S

test_reference :: proc() {
    s := S { }
    x := s.windows_amd64

    when ODIN_OS == .Linux {
        l := S { }
        y := l.linux
    }
}

advanced :: proc() {
    when ODIN_OS == .Windows && ODIN_ARCH == .amd64{
        win_amd64 := S { }
        win_amd_var := win_amd64.windows_amd64
    }

    when ODIN_OS == .Windows && ODIN_ARCH == .i386{
        win_i386 := S { }
        win_i386_var := win_i386.windows_i386
    }

    when ODIN_OS == .Windows && ODIN_ARCH == .arm64{
        win_arm64 := S { }
        win_arm64_var := win_arm64.windows_arm64
    }

    when ODIN_OS == .Linux{
        linux := S { }
        linux_var := linux.linux
    }

    when ODIN_OS == .Darwin {
        shared_struct := shared.Shared { }
        shared_var := shared_struct.shared
    }
}

