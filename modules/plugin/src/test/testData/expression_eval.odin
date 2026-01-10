package testData

testIntegerValue :: proc() {
    X :: 1
    Y :: X

    x := [Y]i32{ 1 }
}

testAddingIntegers :: proc() {
    X :: 1
    Y :: X + 3

    x := [Y]i32{ 1, 2, 3, 4 }
}

testOdinOs :: proc() {
    IS_WINDOWS :: ODIN_OS == .Windows
    IS_BUILD_MODE_DYNAMIC :: ODIN_BUILD_MODE == .Dynamic
}

testWhenStatementConditions :: proc() {
    when !ODIN_TEST && !ODIN_NO_ENTRY_POINT {
    }
    when ODIN_BUILD_MODE == .Dynamic {
    } else when !ODIN_TEST && !ODIN_NO_ENTRY_POINT {
        when ODIN_NO_CRT {
            when ODIN_ARCH == .amd64 {
            } else when ODIN_ARCH == .i386 {
            } else when ODIN_OS == .Darwin && ODIN_ARCH == .arm64 {
            } else when ODIN_ARCH == .riscv64 {
            }
        } else {
        }
    }
}


testWhenStatementMultipleDefinitions_withAlias :: proc() {

    when ODIN_OS == .Linux {
        _S :: struct {
            f: i32
        }
    } else {
        _S :: struct {
            f: i64
        }
    }

    // This symbol will be reached with different knowledge
    // We cannot use the cached value as-is
    // Instead, we need to either, use cached values that are dependent
    // on the context, or turn off cache retrieval for subsequent computation
    // What is the condition, under which cache computation is allowed/disallowed?
    // Thoughts:
    // When reaching out to a target whose lattice is not a subset of the source lattice
    //
    S :: _S

    when ODIN_OS == .Linux {
        linux := S { }
        linux_val := linux.f
    } else {
        other := S { }
        other_val := other.f
    }
}

testWhenStatementMultipleDefinitions :: proc() {

    when ODIN_OS != .Windows {
        when ODIN_OS == .Linux {
            S :: struct {
                f: i32
            }
        } else when ODIN_OS == .Darwin {
            S :: struct {
                f: f64
            }
        } else {
            S :: struct {
                f: f32
            }
        }
    }

    when ODIN_OS == .Linux {
        linux := S { }
        linux_val := linux.f
    } else when ODIN_OS == .Darwin {
        darwin := S { }
        darwin_val := darwin.f
    } else when ODIN_OS != .Windows {
        other := S { }
        other_val := other.f
    }
}

testWhenStatement :: proc() {
    when ODIN_OS != .Windows {
        X :: 1
    } else when ODIN_OS == .Windows {
        X :: 3
    } else when ODIN_OS == .Linux {
        X :: 5
    }

    Y :: X
}


testParapolyExpressionEval :: proc() {
    Vector :: struct($D: int) {
        elements: [D]int
    }

    vec3 := Vector(3) {

    }

    elem3 := vec3.elements

    vec4 := Vector(4) {

    }

    elem4 := vec4.elements
}

