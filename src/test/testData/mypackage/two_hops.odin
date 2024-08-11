package mypackage

import "../otherpackage"

First :: struct {
    second: otherpackage.Second
}

main :: proc() {
    f := First {}
    test := f.second.third
}