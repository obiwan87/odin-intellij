package otherpackage

import "../thirdpackage"

optional_ok :: proc() -> (r: i32, err: thirdpackage.Languages) {
    return 1, .Italian
}