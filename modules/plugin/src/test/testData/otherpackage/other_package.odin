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

Builder :: struct {
    buf: [dynamic]byte,
}
Allocator_Error :: struct {}

builder_make_none :: proc(allocator := context.allocator, loc := #caller_location) -> (res: Builder, err: Allocator_Error) #optional_allocator_error {
    return Builder{ buf=make([dynamic]byte, allocator, loc) or_return }, nil
}

builder_make_len :: proc(len: int, allocator := context.allocator, loc := #caller_location) -> (res: Builder, err: Allocator_Error) #optional_allocator_error {
    return Builder{ buf=make([dynamic]byte, len, allocator, loc) or_return }, nil
}

builder_make_len_cap :: proc(len, cap: int, allocator := context.allocator, loc := #caller_location) -> (res: Builder, err: Allocator_Error) #optional_allocator_error {
    return Builder{ buf=make([dynamic]byte, len, cap, allocator, loc) or_return }, nil
}

builder_make :: proc{
builder_make_none,
builder_make_len,
builder_make_len_cap,
}
