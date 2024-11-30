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

