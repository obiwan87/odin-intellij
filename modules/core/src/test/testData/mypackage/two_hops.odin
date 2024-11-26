package mypackage

import "../otherpackage"
import "../thirdpackage"

First :: struct {
    second: otherpackage.Second
}

main :: proc() {
    f := First {}
    test := f.second.third
    second := f.second
    third := thirdpackage.Third{ }
}