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

testTypeInference4 :: proc() {
    dict := Dict(i32, List(Point)) { entries = { 1 = Point{ a, b } } }
    get_value(dict, 1)
}

testTypeInference5 :: proc() {
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

testBitsetsAndEnums :: proc() {
    b := DirectionSet { .South, .East }
    c := Direction.East
    d := DirectionSet.East
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

typeInference_procedureGroup :: proc() {
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

typeInference_polyProcedureGroup :: proc() {
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

    S :: struct {
        x, y: Direction,
        p: proc(d: Direction) -> Direction
    }

    s := S {
        x = .North
    }

    arr := [Direction]i32 { }
    arr[.North] = 1

    Directions :: bit_set[Direction; i32]
    bs := Directions { }
    b := .North in bs

    // attribute-values


    // case-clause (ok)
    {
        north : Direction = .NORTH
        switch north {
        case .North:
            fmt.println("Hello")
        }
    }

    {
        p1 :: proc(i: i64, d: Direction) -> i32 {
            return 1
        }
        p2 :: proc(i: i32, d: Direction) -> i32 {
            return 2
        }

        group :: proc {
        p1, p2
        }

        // implicit expressions in procedure overloads
        g := group(1, .East)
    }

    {
        Direction_Alias :: distinct Direction
        z := Direction_Alias(.North)
    }

    {
        r, err := o.optional_ok()
        cmp := .Italian == err
    }

    {
        bitset := Directions{ }
        bitset += { .North }
    }

    {
        field_proc_ret := (S{}.p)(.North)
    }

    {

    }
}

testPositionalInitialization :: proc() {
    E :: enum {
        A, B
    }
    S :: struct {
        e: E
    }

    s : S = { .A }
}
// TODO
// resolve references in attributes
// objective-c types with "->"
// procedure group: when parameter is the same in all procedure groups, we can infer the argument's type even though there is no clear winner
// support var_args in argument->parameter computation (test with log_ext in macros.odin)

testBitSetOperations :: proc() {
    Direction :: enum {
        N, S, O, E
    }
    DirectionSet :: distinct bit_set[Direction]

    Directions :: DirectionSet{ .N, .S }
    operation := Directions | { .E }

    x := operation
}

testEnumeratedArrays :: proc() {
    x := [Direction][]i32 {
        .North = {
            100,
            200
        },
    }

    y := [Direction][Direction]i32 {
        .North = { .East = 100 }
    }
}

testSwizzleFieldsAndArrays :: proc() {
    Vector3 :: distinct [3]f32
    a := Vector3{ 1, 2, 3 }
    b := Vector3{ 5, 6, 7 }
    c := (a * b) / 2 + 1
    d := c.x + c.y + c.z
}

testStringBuilder :: proc(allocator := context.allocator, loc := #caller_location) {
    x := o.builder_make(1, 1, allocator, loc)
}

testPrimitiveTypeCasting :: proc() {
    bg := [3]f32{ }
    a := f32(2) / 255.0
    rgb := [3]f32{ f32(1), f32(2), f32(3) }
    c := ((1.0 - a) * bg + a * rgb)

    x := c.x
}

testTypeInfoOf :: proc() {
    x := type_info_of(y)
}

mat4 :: matrix[4, 4]i32
testMatrixType :: proc() ->  (m: mat4) {
    x := m[3]
}

testNestedWhenStatements :: proc() {
    when 0 == 0 {
        when 1 == 1 {
            CONST :: 1
        }
    }

    p :: proc () {
        x := CONST
    }
}

testArrayOfStructs :: proc() {
    arr := []Point {
        { x = 1, y = 2 },
    }
}

testPointerToCompoundLiteral :: proc() {
    p :: proc(x: ^Point) {

    }
    points : ^Point = &{
        x = 1
    }

    nested_points := []^Point {
        &{ x = 2 }
    }

    S :: struct {
        p: ^Point
    }
    nested_struct := S {
        p = &{ x = 2 }
    }

    proc_call := p(&{ x = 3 })
}

testRecursiveStruct :: proc() {
    Node :: struct {
        prev: ^Node,
        next: ^Node
    }

    n := Node{ }
    x := n.next.prev
}

testOffsetOfSymbols :: proc() {
    offset := offset_of(Point, x)
}

testConstrainedType :: proc($C: typeid/Dict($K , $V)) -> T {
    c: C
    x := c.entries
}

Dict2 :: struct($Key: typeid, $Value: i32 = 3) {

}
testFieldsOfParaPoly :: proc(d: $D/Dict2($K)) where D.Key {

}

default_swap_proc :: proc($T: typeid) -> proc(q: []T, i, j: int) {
    return proc(q: []T, i, j: int) {
        q[i], q[j] = q[j], q[i]
    }
}

testSoaSlices :: proc() {
    xs := []i32{ 1, 2, 3 }
    ys := []f32{ 1, 2, 3 }
    zs := []i64{ 1, 2, 3 }

    s := soa_zip(x=xs, y=ys, z=zs)

    for v, i in s {
        d := v.x
        e := v.y
        f := v.z
    }

    xs2, ys2, zs2 := soa_unzip(s);
    a := xs2
    b := ys2
    c := zs2
}

testAnyType :: proc() {
    x: any
    y := x
}

testUnionConversion :: proc() {
    U :: union {
        i32, f32
    }
    p1 :: proc(u: U) -> i32 {
    }
    p2 :: proc(point: Point) -> f32 {
    }

    p :: proc {
    p1, p2
    }

    x := p(1.0)
}

testAnyTypeConversion :: proc() {
    U :: union {
        i32, f32
    }
    p1 :: proc(t: any, u: U) -> i32 {
    }
    p2 :: proc(t: any, point: Point) -> f32 {
    }

    p :: proc {
    p1, p2
    }

    x := p(1.0, 1.0)
}

testFloatConversion :: proc() {
    p1 :: proc(t: f64) -> f64 {
    }
    p2 :: proc(p: Point) -> Point {
    }
    p3 :: proc(t: i64) -> i64 {
    }

    p :: proc {
    p1, p2
    }
    q :: proc {
    p2, p3
    }

    x := p(1)
    y := q(1.0)
}

testSwizzleBuiltinProc :: proc() {
    arr := [3]i32 { }

    x := swizzle(arr, 3, 2, 1)
}