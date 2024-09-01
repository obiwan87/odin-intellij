package otherpackage

import p "../mypackage"
import "../thirdpackage"

Second :: thirdpackage.Third

main :: proc() {
    test := p.a_mypublic_proc()
}