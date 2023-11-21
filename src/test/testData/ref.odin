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