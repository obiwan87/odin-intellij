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
        0, 0, { 100, 200, 130}
    }
    e.weapon.strength
}

Point :: struct {
    x, y: i32
}
List :: struct($Item: typeid) {
    items: []Item
}
get_at :: proc(list: List($T), index: i32) -> T {
    return list.items[index]
}

testTypeInference :: proc() {
    points := List(Point) {}
    get_at(points, 1)

}
