package main


main :: proc() {
    {
        walker_definition := Enemy_Definition {
            collider_size     = {},
            move_speed        = 1,
        }
        walker_definition.animations["walk"] = walker_anim
        gs.enemy_definitions["Walker"] = walker_definition
    }
}