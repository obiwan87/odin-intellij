package prototyping

//
// Does not compile
//poly_proc_explicit :: proc(x: proc($T: typeid) -> T, y: T) -> T {
//    return x(y)
//}

List :: struct($Item: typeid) {
    items: []Item
}

MyPolyStruct :: struct($Item: typeid, $K: typeid/List(Item) ) {
    firstItem: Item
}

// This works, meaning that th
poly_test :: proc(list: List($Item)) -> Item {
    A :: struct {
        a: Item
    }
    a := A { }
    return a.a
}

poly_test_2 :: proc($K: typeid) -> K {
    A :: struct($K2: typeid/K) {
        item: K2
    }
    a := A(K) { }
    return a.item
}

poly_test_3 :: proc(list: List($Item)) -> Item {
    A :: struct($K: typeid/Item) {
        item: K
    }

    a := A(Item) { }
    return a.item
}

poly_test_4 :: proc(list: List($Item), $K: typeid) -> Item {
    A :: struct($K2: typeid/Item) {
        item: K2
    }
    a := A(K) { }
    return a.item
}

poly_test_5 :: proc(list: $T/List($Item), $K: typeid/T) -> Item {
    A :: struct($K2: typeid/Item) {
        item: K2
    }
    a := A(Item) { }
    return a.item
}

poly_test_6 :: proc(list: List($T), $K: i32) -> Point {
    return list.items[0]
}

poly_proc_explicit :: proc(x: proc($K: typeid) -> K) {

}

poly_proc_explicit_2 :: proc($K: typeid) -> K {
    return K { }
}