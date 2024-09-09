package otherpackage

import "../thirdpackage"

Second :: thirdpackage.Third

main :: proc() {
    test := thirdpackage.a_mypublic_proc()
}