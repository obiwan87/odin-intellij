package main

Point :: struct {

}

parapoly_slice :: proc(x: []$T) -> T {

}

parapoly_matrix :: proc(x: matrix[1, 1]$T) -> T {

}

parapoly_slice_constrained :: proc(x: $T/[]$U) -> U {

}

poly_proc :: proc(x: proc(p: $K) -> $E, y: K) -> E {
    return x(y)
}

poly_proc_explicit :: proc(x: proc($K: typeid, index: i32) -> K) -> K {
    return x(K, 2)
}

testParapoly_slice :: proc() {
    x := parapoly_slice([]i32 { })
}

testParapoly_matrix :: proc() {
    x := parapoly_matrix(matrix[1 , 1]i32 { 0 })
}
testParapoly_slice_constrained :: proc() {
    x := parapoly_slice_constrained([]i32 { })
}

testParapoly_proc :: proc() {
    x := poly_proc(proc(x: i32) -> i32 { return 1}, 1)
}

Dict :: struct($Key, $Value: typeid) {

}
testParapoly_instantiatedStruct :: proc() {
    PointDict :: Dict(int, Point)

    x := PointDict {}
}