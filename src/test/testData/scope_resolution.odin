package main

Point :: struct {
    x: i32,
    y: i32
}

proc_to_find :: proc(i, k: i32, j: i32) {

}

proc_to_search_from :: proc() {
    proc_to_find()
}

proc_proto :: proc() {
    point := Point {
        x = 1,
        y = 2
    }
    point.x
}