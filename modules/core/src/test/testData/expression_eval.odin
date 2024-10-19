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
}