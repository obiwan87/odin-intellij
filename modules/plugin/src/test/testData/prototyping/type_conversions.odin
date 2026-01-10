package prototyping

import "core:fmt"

Dict :: struct($K: typeid, $V: typeid) {
    entries: map[K]V
}

Point :: struct {
    x, y: i32
}

Line :: struct {
    points: []Point
}

Shape :: union {
    Point,
    Line
}

get_shape :: proc() -> Shape {
    return nil
}

get_multiple :: proc() -> (int, int, bool) {
    return 1, 1, false
}

Error :: enum {
    SyntaxError,
    ParseError,
    IOError
}

poly_map_type :: proc(x: map[int]string, k: int) -> string

poly_map_type_2 :: distinct poly_map_type

Point2D :: distinct Point
Point2Dv2 :: distinct Point2D

test_lambda :: proc(lambda: poly_map_type) {

}



poly_map :: proc(x: map[$K]$V, k: K) -> V {
    return x[k]
}

poly_matrix :: proc(x: matrix[2, 3]$T, r: i32, c: i32) -> T {
    return x[r, c]
}

poly_bit_set :: proc(x: bit_set[$E], val: E) -> bool {
    return val in x
}

poly_proc :: proc(x: proc(p: $K) -> $E, y: K) -> E {
    return x(y)
}

print_types_of_literals :: proc() {
    {
        x := 1i
        b := typeid_of(type_of(x))
        fmt.println("1i", b)
    }
    {
        x := 1.0i
        b := typeid_of(type_of(x))
        fmt.println("1.0i", b)
    }
    {
        x := 1j
        b := typeid_of(type_of(x))
        fmt.println("1j", b)
    }

    {
        x := 0o1
        b := typeid_of(type_of(x))
        fmt.println("0o1", b)
    }

    {
        x := imag(1i)
        b := typeid_of(type_of(x))
        fmt.println("1i", b)
    }

    {
        x := 1.0
        b := typeid_of(type_of(x))
        fmt.println("1.0", b)
    }

    Direction :: enum{
        North, East, South, West
    }
    Direction_Set :: bit_set[Direction; i32]
    Char_Set :: bit_set['A' ..= 'Z']
    Number_Set :: bit_set[0 ..< 10]

    {
        x : Direction_Set = { .East, .South }
        b := typeid_of(type_of(x))
        fmt.println(b)
        for elem in x {
            fmt.println(typeid_of(type_of(elem)))
        }
    }

    {
        x : Char_Set = { 'A', 'Y' }
        b := typeid_of(type_of(x))
        fmt.println(b)
        for elem in x {
            fmt.println(typeid_of(type_of(elem)))
        }
    }

    {
        x : Number_Set = { 0, 9 }
        b := typeid_of(type_of(x))
        fmt.println(b)
        for elem in x {
            fmt.println(typeid_of(type_of(elem)))
        }
    }

    {
        a: [^]int
        fmt.println(a) // <nil>
        b := [?]int { 10, 20, 30 }
        a = raw_data(b[:])
        fmt.println(typeid_of(type_of(a[1])))
    }

    using Direction

    x := East

}

Vector4 :: distinct quaternion256
V4 :: Vector4
binary_ops_combinations :: proc () {

    {
        t_byte : byte = 1
        t_int : int = 1
        t_i8 : i8 = 1
        t_i16 : i16 = 1
        t_i32 : i32 = 1
        t_i64 : i64 = 1
        t_i128 : i128 = 1
        t_uint : uint = 1
        t_u8 : u8 = 1
        t_u16 : u16 = 1
        t_u32 : u32 = 1
        t_u64 : u64 = 1
        t_u128 : u128 = 1
        t_uintptr : uintptr = 1
        t_i16le : i16le = 1
        t_i32le : i32le = 1
        t_i64le : i64le = 1
        t_i128le : i128le = 1
        t_u16le : u16le = 1
        t_u32le : u32le = 1
        t_u64le : u64le = 1
        t_u128le : u128le = 1
        t_i16be : i16be = 1
        t_i32be : i32be = 1
        t_i64be : i64be = 1
        t_i128be : i128be = 1
        t_u16be : u16be = 1
        t_u32be : u32be = 1
        t_u64be : u64be = 1
        t_u128be : u128be = 1
        t_f16 : f16 = 1
        t_f32 : f32 = 1
        t_f64 : f64 = 1
        t_f16le : f16le = 1
        t_f32le : f32le = 1
        t_f64le : f64le = 1
        t_f16be : f16be = 1
        t_f32be : f32be = 1
        t_f64be : f64be = 1
        t_complex32 : complex32 = 1
        t_complex64 : complex64 = 1
        t_complex128 : complex128 = 1
        t_quaternion64 : quaternion64 = 1i
        t_quaternion128 : quaternion128 = 1i
        t_quaternion256 : quaternion256 = 1i

        v1 : V4 = 1j
        v2 : Vector4 = 1k

        fmt.println(v1 + v2)
    }
}



