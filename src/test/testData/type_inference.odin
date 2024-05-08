package main

import "core:fmt"

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
    T1,
    T2
}

get_at :: proc(list: List($T), index: i32) -> T {
    return list.items[index]
}

get_key :: proc(dict: Dict($K, $V), key: K) -> V {
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

testTypeInference2 :: proc() {
    dict := Dict(i32, Point) { entries = { 1 = Point{ a, b } } }
    get_key(dict, 1)
}

testTypeInference3 :: proc() {
    dict := Dict(i32, List(Point)) { entries = { 1 = Point{ a, b } } }
    get_multi_dict_entry(dict, 0, 0)
}

testTypeInference4:: proc() {
    dict := Dict(i32, List(Point)) { entries = { 1 = Point{ a, b } } }
    get_key(dict, 1)
}

testTypeInference5:: proc() {
    dict := Dict(i32, List(Point)) { entries = { 1 = Point{ a, b } } }
    get_key(dict, 1).items[0]
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
    point := Point {}
    point_ptr := &point
}