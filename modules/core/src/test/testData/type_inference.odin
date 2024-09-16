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

    my_make :: proc {
    my_make_slice, my_make_dyn_array,
    }

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

test_polyOverloadWithMake :: proc() {
    Allocator_Error :: struct {
    }
    make_slice :: proc($T: typeid/[]$E, #any_int len: int, allocator := context.allocator, loc := #caller_location) -> (T, Allocator_Error) #optional_allocator_error {
        return make_aligned(T, len, align_of(E), allocator, loc)
    }

    make_dynamic_array :: proc($T: typeid/[dynamic]$E, allocator := context.allocator, loc := #caller_location) -> (T, Allocator_Error) #optional_allocator_error {
        return make_dynamic_array_len_cap(T, 0, 0, allocator, loc)
    }

    make_dynamic_array_len :: proc($T: typeid/[dynamic]$E, #any_int len: int, allocator := context.allocator, loc := #caller_location) -> (T, Allocator_Error) #optional_allocator_error {
        return make_dynamic_array_len_cap(T, len, len, allocator, loc)
    }

    make_dynamic_array_len_cap :: proc($T: typeid/[dynamic]$E, #any_int len: int, #any_int cap: int, allocator := context.allocator, loc := #caller_location) -> (array: T, err: Allocator_Error) #optional_allocator_error {
        err = _make_dynamic_array_len_cap((^Raw_Dynamic_Array)(&array), size_of(E), align_of(E), len, cap, allocator, loc)
        return
    }

    make_map :: proc($T: typeid/map[$K]$E, #any_int capacity: int = 1 << MAP_MIN_LOG2_CAPACITY, allocator := context.allocator, loc := #caller_location) -> (m: T, err: Allocator_Error) #optional_allocator_error {
        make_map_expr_error_loc(loc, capacity)
        context.allocator = allocator

        err = reserve_map(&m, capacity, loc)
        return
    }

    make_multi_pointer :: proc($T: typeid/[^]$E, #any_int len: int, allocator := context.allocator, loc := #caller_location) -> (mp: T, err: Allocator_Error) #optional_allocator_error {
        make_slice_error_loc(loc, len)
        data := mem_alloc_bytes(size_of(E) * len, align_of(E), allocator, loc) or_return
        if data == nil && size_of(E) != 0 {
            return
        }
        mp = cast(T)raw_data(data)
        return
    }

    make_soa_slice :: proc($T: typeid/#soa[]$E, length: int, allocator := context.allocator, loc := #caller_location) -> (array: T, err: Allocator_Error) #optional_allocator_error {
        return make_soa_aligned(T, length, align_of(E), allocator, loc)
    }

    make_soa_dynamic_array :: proc($T: typeid/#soa[dynamic]$E, allocator := context.allocator, loc := #caller_location) -> (array: T, err: Allocator_Error) #optional_allocator_error {
        context.allocator = allocator
        reserve_soa(&array, 0, loc) or_return
        return array, nil
    }

    make_soa_dynamic_array_len :: proc($T: typeid/#soa[dynamic]$E, #any_int length: int, allocator := context.allocator, loc := #caller_location) -> (array: T, err: Allocator_Error) #optional_allocator_error {
        context.allocator = allocator
        resize_soa(&array, length, loc) or_return
        return array, nil
    }

    make_soa_dynamic_array_len_cap :: proc($T: typeid/#soa[dynamic]$E, #any_int length, capacity: int, allocator := context.allocator, loc := #caller_location) -> (array: T, err: Allocator_Error) #optional_allocator_error {
        context.allocator = allocator
        reserve_soa(&array, capacity, loc) or_return
        resize_soa(&array, length, loc) or_return
        return array, nil
    }

    make :: proc{
    make_slice,
    make_dynamic_array,
    make_dynamic_array_len,
    make_dynamic_array_len_cap,
    make_map,
    make_multi_pointer,

    make_soa_slice,
    make_soa_dynamic_array,
    make_soa_dynamic_array_len,
    make_soa_dynamic_array_len_cap,
    }

    x := make([]Point, 1)
    Points :: []Point
    y := make(Points, 1)
    PointsDistinct :: distinct []Point
    z := make(PointsDistinct, 1)
// TODO test all the other variants
}

test_astNew :: proc() {
    ConcreteAstNode :: struct {
        base: i32
    }
    Pos :: struct {
    }
    Node :: struct {
        a: i32
    }
    ConcreteNode :: struct {
        using n: Node,
        b: i32
    }
    ConcreteNode2 :: struct {
        using cn: ConcreteNode,
        c: i32
    }
    new_from_positions :: proc($T: typeid, pos, end: Pos) -> ^T {

    }

    new_from_pos_and_end_node :: proc($T: typeid, pos: Pos, end: ^Node) -> ^T {

    }

    new_from_pos_and_end_node_2 :: proc($T: typeid, pos: Pos, end: ^o.Node) -> ^T {

    }

    new :: proc {
    new_from_pos_and_end_node_2,
    new_from_positions,
    new_from_pos_and_end_node,

    }

    x := new(ConcreteAstNode, Pos{ }, Pos{ })
    y := new(ConcreteNode, Pos{ }, &ConcreteNode{ })
    z := new(ConcreteNode2, Pos{ }, &ConcreteNode2{ })
    a := new(o.Stmt, Pos{ }, &o.Stmt{ })

}

test_dynamicArrayAllocatorSymbol :: proc() {
    x : [dynamic]i32

    y := x.allocator
}

test_procedureContext :: proc() {
    y := context.allocator.data
}

test_structField :: proc() {

    x : Point = {
        x = 1,
        y = 2
    }

    MyStruct :: struct {
        p1, p2: Point,
        a: struct {
            alpha, beta, gamma: f64
        }
    }

    l : MyStruct = {
        p1 = { x = 1, y = 1 },
        p2 = { x = 2, y = 2 },
        a = {
            alpha = 12,
            beta = 14,
            gamma = 15
        }
    }
}

testNamelessStruct :: proc() {
    MyStruct :: struct {
        a: struct {
            x, y: i32
        }
    }

    s := MyStruct{ }
    x := s.a.x
    y := s.a
}

testImplicitEnumExpression :: proc() {

    p :: proc(t: Direction) {

    }

    x := p(.North) // expecting direction from .North
}

testBitSetOperations :: proc() {
    Direction :: enum {
        N, S, O, E
    }
    DirectionSet :: distinct bit_set[Direction]

    Directions :: DirectionSet{ .N, .S }
    operation := Directions | { .E }

    x := operation
}

testEnumAliases :: proc() {
    DirectionAlias :: Direction

    x = DirectionAlias.

}