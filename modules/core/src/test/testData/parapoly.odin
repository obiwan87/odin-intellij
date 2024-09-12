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

testParapoly_typeIdConstrained :: proc() {
    my_poly :: proc($T: typeid/[]$E) -> T {

    }

    x := my_poly([]Point)
}

testParapoly_proc :: proc() {
    x := poly_proc(proc(x: i32) -> i32 {
        return 1
    }, 1)
}

Dict :: struct($Key, $Value: typeid) {
    entries: map[Key]Value
}

testParapoly_specializedStruct :: proc() {
    PointDict :: Dict(int, Point)
    x := PointDict {}
}

testParapoly_typeAliasAsParam :: proc() {
    PointAlias :: Point
    PointDict :: Dict(int, PointAlias)

    d := PointDict{}
    point := d.entries[1]
}

testParapoly_distinctAlias :: proc() {
    PointsAlias    :: []Point
    PointsDistinct :: distinct []Point
    get_first :: proc(arr: []$T) -> T {
        return arr[0]
    }

    x := get_first(PointsAlias{});
    // should fail
    y := get_first(PointsDistinct{});
}

testTypeInference_withRecursivePolyPara :: proc() {
    Parent :: struct($T: typeid) {
        children: []Child(T),
        data: T
    }

    Child :: struct($T: typeid) {
        parent: Parent(T),
        change_parent: proc(parent: Parent(T)),
        data: T
    }

    t1 := Parent(Point) { }
    test := t1.data
}

testTypeInference_withDoubleInstantiation :: proc() {
    Whole :: struct($T: typeid) {
        part: Part(Other(T))
    }

    Part :: struct($T: typeid) {
        data: T
    }

    Other :: struct($T: typeid) {
        other: T
    }

    w := Whole(Point){ }
    test := w.part.data.other
}