package prototyping

import "core:fmt"

main :: proc() {
    {
        val := poly_test_2(Point)
        fmt.println(val)
    }
    {
        val := poly_test_3(List(Point) { })
        fmt.println(val)
    }
    {
        val := poly_test_4(List(Point) { }, Point)
        fmt.println(val)
    }
    {
        val := poly_test_5(List(Point) { }, List(Point))
        fmt.println(val)
    }
    {
        val := poly_test_6(List(Point) { items = []Point { Point{ } } }, 1)

        fmt.println(val)
    }

    {
        val := poly_proc_explicit_2(Point)
        fmt.println(val)
    }

    {
        point_proc :: proc($K: typeid) {
        }
        val := poly_proc_explicit(point_proc)
    }

    main :: proc() {
        a : Value = 2

        as_int := a.(int) // <--- HERE
        as_int^ = 3

        fmt.printfln("a: %v", a)
    }
}