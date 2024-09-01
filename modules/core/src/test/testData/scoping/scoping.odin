package scoping

import "core:fmt"
import p "../mypackage"
main :: proc() {
}

MyStruct :: struct {
    a, b, c: i32
}

assignment :: proc() {
    x := 1

    // test is not in scope in the right hand side of the assignment
    test := 1 + x
}

partial_scope :: proc() {
    x : i32 = 1
    test := 1 // y should not be in scope here
    y := 1
}

conditional_block :: proc() {
    if x, v := 1, 1; x > 0 {
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

    if x := 0; x > 0 do fmt.println(x)
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

    p :: proc() {
    }

    test_outer := S { }
    p()

    {
        test_inner := 1
        Kinner :: 1
    }

    S :: struct {
    }

}

for_block :: proc() {
    for i, j := 0, 0; i <= 10; i += 1 {
        test := 1
        fmt.println(test)
    }
}

for_in_block :: proc() {
    arr := [?]i32 { 1, 2, 3, 4 }
    for index, val in arr {
        test := 1
        fmt.println(test)
    }
}

switch_block :: proc() {
    u := 1
    switch s := 1; u {
    case 1:
        y := 0
        x := 0
        fmt.println(y)
        fmt.println(x)
        test_case_1 := 1
        fmt.println(test_case_1)
    case 2:
        y := 1
        fmt.println(y)
        test_case_2 := 1
        fmt.println(test_case_2)

    }
}

switch_in_block :: proc() {
    U :: union {
        i32, f32
    }
    x : U = 1
    switch t in x {
    case i32:
        test_i32 := 1
        fmt.println("it's an i32", test_i32)
    case f32:
        test_f32 := 1
        fmt.println("it's an f32", test_f32)
    }
}

structs_unions :: proc() {
    S :: struct($Key, $Value: i32) {
        x: Key,
        y: Value
    }

    U :: union($T1, $T2: typeid) {
        T1,
        T2
    }
}

recursive_local_defs :: proc() {
    p :: proc() {
        test := 1
        fmt.println(test)
    }

    s :: struct {
        next: ^s
    }
}

using_statement :: proc() {
    Point :: struct {
        x, y: i32
    }

    Line :: struct {
        p1, p2: Point
    }

    Triangle :: struct {
        using line: Line,
        p3: Point
    }

    point := Point { x = 1, y = 1 }
    {
        using point
        test := 1
        fmt.println(test)
    }

    {
        using l := Line { p1 = point, p2 = point }
        test_line := 1
        fmt.println(test_line)
    }

    {
        using t : Triangle
        test_triangle := 1
        fmt.println(test_triangle)
        fmt.println(p1, p2, p3)
    }

    {
        p :: proc(using p: Point) {
            test_proc := 1
        }
    }

    {
        Channels :: enum {
            R, G, B
        }

        using Channels

        test_enum := 1
    }

    {
        P, Q :: struct {
        }, struct{
        }
    }
}

nested_procs :: proc() {
    invisible := 1;
    S :: struct {
    }
    p :: proc() {
        s := S { }
        r :: proc() {
            test := S { }
            nested_procs()
        }
    }
}

labels :: proc() {
    label1: for {
        test_1 := 1
        label2: for{
            test_2 := 2
            label3: if 1 == 1 {
                test_3 := 3

                label4: switch test_3 {
                case 3:
                    test_4 := 1
                }
            }
        }
    }
}

using_import :: proc() {
    using p
    test := 1
}

literal_blocks :: proc() {
    x : MyStruct
    x = { }
}

using_fields :: proc() {
    Point :: struct {
        x, y: i32
    }

    Line :: struct {
        p1, p2: Point
    }

    Triangle :: struct {
        using line: ^Line,
        p3: Point
    }

    triangle := Triangle { }
    test := triangle.p1
}

override_parent_symbols :: proc() {
    Point :: struct {
        x, y: i32
    }
    xyz := Point { }
    test := xyz
}

xyz : i32 : 1

return_type_defined_proc :: proc() {

    Error :: enum {
        None,
        Something_Bad,
        Something_Worse,
        The_Worst,
        Your_Mum,
    }

    caller1 :: proc(e: Error) -> Error {
        test1 := e

        return e
    }

    caller2 :: proc() -> Error {
        return .None
    }

    test2 := caller2()
}