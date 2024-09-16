package otherpackage

import "../thirdpackage"

Second :: thirdpackage.Third

main :: proc() {
    test := thirdpackage.a_mypublic_proc()
}

Node :: struct {
    type: i32
}

Expr :: struct {
    using n: Node
}

Stmt :: struct {
    using e: Expr
}