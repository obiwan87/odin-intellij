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

main :: proc () {
    e :: Enemy {
        0, 0, { 100, 200, 130 }
    }
    e.weapon.strength
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

get_at :: proc(list: List($T), index: i32) -> T {
    return list.items[index]
}

get_key :: proc(dict: Dict($K, $V), key: K) -> V {
    return dict.entries[key]
}

get_multi_dict_entry :: proc(dict: Dict($K, List($V)), key: K, index: i32) -> V {
    return dict.entries[key].items[index];
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

get_entry :: proc(dict: Dict($Key, $Value)) -> (Key, Value) {

}

testTypeInference6 :: proc() {
    dict := Dict(i32, Point) {}
    key, value := get_entry(dict)
    point := value
}
