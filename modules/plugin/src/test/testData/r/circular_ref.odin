package r

import c "../c"

Color :: [4]c.float

circular_ref :: proc() {
    x := Color {0.0, 0.0, 0.0, 0.0}
    r := x.r
}