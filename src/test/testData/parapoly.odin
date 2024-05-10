package main

Point :: struct {

}

parapoly_slice :: proc(x: []$T) -> T {

}

parapoly_matrix :: proc(x: matrix[1, 1]$T) -> T {

}

parapoly_slice_constrained :: proc(x: $T/[]$U) -> U {

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
