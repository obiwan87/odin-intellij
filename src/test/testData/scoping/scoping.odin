package scoping

import "core:fmt"

main :: proc() {}

MyStruct :: struct {

}

assignment :: proc() {
    x := 1

    // test is not in scope in the right hand side of the assignment
    test := 1 + x
}

partial_scope :: proc() {
    x := 1
    test := 1 // y should not be in scope here
    y := 1
}

conditional_block :: proc() {
    if x := 1; x > 0 {
    // x should be in scope here
        y := x
        test_if := 1
    } else if z := 1; z > 0 {
    // y should be in scope here
        test_else_if := 1
        fmt.println(test_else_if)
    } else {
    // x and y should be in scope here
        w := x + z
        test_else := 1
    }
}

shadowing :: proc() {
    x := 1
    // This should point the x above
    {
        x := x
        // This should point the x above
        y := x
    }
}

file_scope :: proc() {
    test := 1 // All global symbols should be visible here, including the name "global_scope"
}


params :: proc(x, y, z: i32, u, v, w: f64, my_struct: MyStruct, k:=1) -> (r1: i32, r2: f32, r3, r4: f64) {
    test := x
    return 1, 1.0, 1.0, 1.0
}

Table :: struct($Key:typeid, $Val:typeid) {
    entries: map[Key]Val
}

poly_params :: proc($T: typeid, t: Table($Key, $Val/Key), k: Key, v: Val) -> (r1: T, r2: Val, r3: Key) {
    test := 1
}

constants :: proc() {
    K :: 1

    p :: proc() { }

    test_outer := S {}
    p()

    {
        test_inner := 1
        Kinner :: 1
    }

    S :: struct { }

}

defers :: proc() {

}
