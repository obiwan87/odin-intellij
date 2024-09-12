package main

import "core:fmt"
import "base:runtime"
import o "otherpackage"

Weapon :: struct {
    strength: f32,
    durability: f32,
    critical: f32
}

Enemy :: struct {
    x: f32,
    y: f32,
    weapon: Weapon,
}

Point :: struct {
    x, y: i32
}

List :: struct($Item: typeid) {
    items: []Item
}

Dict :: struct($Key: typeid, $Value: typeid) {
    entries: map[Key]Value
}

Line :: struct {
    points: []Point
}

Shape :: union {
    Point,
    Line
}

PolyShape :: union($T1: typeid, $T2: typeid ) {
    Parent,
    Child
}

get_at :: proc(list: List($T), index: i32) -> T {
    return list.items[index]
}

get_value :: proc(dict: Dict($K, $V), key: K) -> V {
    return dict.entries[key]
}

get_multi_dict_entry :: proc(dict: Dict($K, List($V)), key: K, index: i32) -> V {
    return dict.entries[key].items[index];
}

get_entry :: proc(dict: Dict($Key, $Value)) -> (Key, Value) {

}

get_shape :: proc() -> Shape {
    return Line { }
}

get_as_first_polyshape :: proc(poly_shape: PolyShape($T1, $T2)) -> T1 {
    return poly_shape.(T1)
}

main :: proc () {
    e :: Enemy {
        0, 0, { 100, 200, 130 }
    }
    e.weapon.strength
}


testTypeInference :: proc() {
    points := List(Point) { }
    get_at(points, 1)

}

Dict :: struct($Key: typeid, $Value: typeid) {
    entries: map[Key]Value
}

testTypeInference2 :: proc() {
    dict := Dict(i32, Point) { entries = { 1 = Point{ a, b } } }
    get_value(dict, 1)
}

testTypeInference3 :: proc() {
    dict := Dict(i32, List(Point)) { entries = { 1 = Point{ a, b } } }
    get_multi_dict_entry(dict, 0, 0)
}

testTypeInference4:: proc() {
    dict := Dict(i32, List(Point)) { entries = { 1 = Point{ a, b } } }
    get_value(dict, 1)
}

testTypeInference5:: proc() {
    dict := Dict(i32, List(Point)) { entries = { 1 = Point{ a, b } } }
    get_value(dict, 1).items[0]
}

testTypeInference6 :: proc() {
    dict := Dict(i32, Point) { }
    key, value := get_entry(dict)
    point := value
}

testTypeInference7 :: proc() {
    shape := get_shape()
}

testTypeInference8 :: proc() {
    poly_shape : PolyShape(Line, Point) = Line { }
    first_shape := get_as_first_polyshape(poly_shape)
}

testTypeInference9 :: proc() {
    OptionalPoint :: union {
        Point
    }

    o : OptionalPoint = Point { 0, 0 }
    k := o.?
}

testTypeInference10 :: proc() {
    OptionalPoint :: union {
        Point
    }

    o : OptionalPoint = Point { 0, 0 }
    point := o.? or_else Point { 0, 0 } // expect k to be Point
}

testTypeInference11 :: proc() {
    OptionalPoint :: union {
        Point
    }

    point := o.(Point) // expect k to be Point
}

testTypeInference12 :: proc() {
    OptionalPoint :: union {
        Point
    }

    point, ok := o.(Point) // expect k to be Point
    x := point
    y := ok
}

testTypeInference13 :: proc() {
    p := Point { }
    point_ptr := &p
}

testTypeInference14 :: proc() {
    points := []Point { Point { }, Point { } }
    point_slice := points[0:2]
}

testTypeInference15 :: proc() {
    point_1 := Point{ 0, 0 } if true else Point{ 1, 1 }
    point_2 := true? Point{ 0, 0 } : Point{ 1, 1 }
    point_3 := Point{ 0, 0 } when true else Point{ 1, 1 }
}


testTypeInference16 :: proc() {
    complex_number1 := 1i
    complex_number2 := 1.0i
    quaternion1 := 1j
    quaternion2 := 1k
    quaternion3 := 1.0k

    r := '\n'
    s := "string"
}

Direction :: enum i32 {
    North, East, South, West
}

DirectionSet :: bit_set[Direction; u8]

testTypeInference17 :: proc() {
    b := DirectionSet { .South, .East }
    c := Direction.East
}

Point2D :: Point
PointDict :: Dict(i32, Point)
testTypeInference_withTypeAliases :: proc() {
    x := PointDict { }
    point := x.entries[0]
}

Points :: []Point
testTypeInference_withTypeAliases_2 :: proc() {
    x : Points = []Point2D { }
    point := x[0]
}

testForInVars :: proc() {
    p := map[Point]Line { }
    c := "string"
    s := []Point { }
    a := [1]Point { Point { } }
    for point, line in p {
        test1 := point
        test2 := line
    }

    for val, idx in c {
        test3 := val
        test4 := idx
    }

    for val, idx in s {
        test5 := val
        test6 := idx
    }

    for val, idx in a {
        test7 := val
        test8 := idx
    }
}



testTypeSwitch :: proc() {
    x : Shape = Point { }

    switch t in x {
    case Point:
        test1 := t
    case Line:
        test2 := t
    }
}

binary_operators_on_arrays :: proc() {
    a := [3]f32{ 1, 2, 3 }
    b := [3]f32{ 5, 6, 7 }
    c := (a * b) / 2 + 1
    test := c
}

ClayArray :: struct($type: typeid){
    internalArray: [^]type,
}

RenderCommand :: struct {
    config: int
}

RenderCommandArray_Get :: proc(array: ^ClayArray(RenderCommand), index: int) -> ^RenderCommand ---

circular_reference_test :: proc(render_commands: ^ClaryArray(RenderCommand)) {
    command := RenderCommandArray_Get(render_commands, 1)
    x := command.config
}




typeInference_procedureOverload :: proc() {
    PointDistinctAlias :: distinct Point
    PointAlias :: Point

    add_one_string :: proc(s: string) -> string {
        return "1"
    }

    add_one_integer :: proc(i: i32) -> i32 {
        return i + 1
    }

    add_one_struct :: proc(p: Point) -> Point {
        return p
    }

    add_one_distinct_alias :: proc(p: PointDistinctAlias) -> PointDistinctAlias {
        return 1
    }
    add_one_alias :: proc(p: PointAlias, k: i32) -> i32 {
        return 1
    }

    add_one_alias_2 :: proc(i: i32, p: PointAlias) -> f64 {
        return 1.0
    }

    add_one_arr :: proc(arr: [2]i32) -> Point {
        return Point { arr[0], arr[1] }
    }

    add_one_arr_2 :: proc(arr: [2]i64) -> Point {
        return Point { arr[0], arr[1] }
    }


    add_one :: proc {
    add_one_string,
    add_one_integer,
    add_one_struct,
    add_one_distinct_alias,
    add_one_alias,
    add_one_alias_2,
    add_one_arr,
    add_one_arr_2,
    }

    r := add_one([2]i32{ 1, 2 })
    s := add_one(12, PointAlias{ }) // ret f64
    t := add_one(k=12, p=PointAlias{ }) // ret i32
    u := add_one(PointAlias { })
    v := add_one(Point { }, 1)
    w := add_one(PointDistinctAlias { })
    x := add_one(1)
    y := add_one("1")
    z := add_one(Point { })
}

typeInference_polyProcedureOverload :: proc() {
    my_make_slice :: proc($K: typeid/[]$E) -> i32 {
        return 1
    }

    my_make_dyn_array :: proc($K: typeid/[dynamic]$E) -> i64 {
        return 1
    }

    my_make :: proc { my_make_slice, my_make_dyn_array, }

    x := my_make([]Point)
    y := my_make([dynamic]Point)

}
testTypeInference_anyType :: proc(x : any) {
    y := x
}

testTwoHopsInferenceWithPointer :: proc() {
    U :: union {
        o.S1,
        o.S2
    }
    u : U
    switch v in u {
    case o.S1:
        y := v.s2.i
    }
}

testTypeInference_withParaPolyAlias :: proc() {
    PointListDistinct :: distinct []Point
    PointListDistinct2 :: distinct PointListDistinct
    PointList :: []Point
    get_first_element :: proc(arr: []$T) -> T {
        return arr[0]
    }

    get_first_element_2 :: proc(arr: $S/[]$T) -> T {
        return arr[0]
    }

    first := get_first_element_2(PointList { Point { } } )
    first2 := get_first_element  (PointList { Point { } } )

    first_dist := get_first_element_2(PointListDistinct2 { Point { } } )
}