package r

import "base:runtime"

import "core:log"
import "core:math/linalg"

import clay "../clay"

@(private="file")
g_clay_font_renderer: ^Font_Renderer
@(private="file")
g_clay_sprites: ^[dynamic]Sprite_Data
@(private="file")
g_clay_ctx: runtime.Context

clay_init :: proc(r: ^Font_Renderer, sprites: ^[dynamic]Sprite_Data) {
    g_clay_ctx = context
    g_clay_font_renderer = r
    g_clay_sprites       = sprites
}

clay_measure_text :: proc "c" (text: ^clay.String, config: ^clay.TextElementConfig) -> (size: clay.Dimensions) {
// TODO: multiline.
    context = g_clay_ctx

    fs_apply(
    g_clay_font_renderer,
    size    = f32(config.fontSize),
    spacing = f32(config.letterSpacing),
    font    = Font(config.fontId),
    )

    size.width  = fs_width(g_clay_font_renderer, string(text.chars[:text.length]))
    size.height = fs_lh(g_clay_font_renderer)
    return
}

clay_render :: proc(render_commands: ^clay.ClayArray(clay.RenderCommand)) {
    for i in 0..<i32(render_commands.length) {
        render_command := clay.RenderCommandArray_Get(render_commands, i)
        bounding_box   := render_command.boundingBox

        #partial switch render_command.commandType {
        case .Text:
            config := render_command.config.textElementConfig
            text   := string(render_command.text.chars[:render_command.text.length])

            fs_draw_text(
            g_clay_font_renderer,
            text    = text,
            pos     = {bounding_box.x, bounding_box.y},
            size    = f32(config.fontSize),
            color   = linalg.array_cast(config.textColor, u8),
            spacing = f32(config.letterSpacing),
            font    = Font(config.fontId),
            align_v = .Top,
            )

        case .Rectangle:
            config := render_command.config.rectangleElementConfig

            if config.cornerRadius != {} {
                log.warnf("TODO: rounded rectangles: %v", config.cornerRadius)
            }

            if config.color.a != 0 {
                _, err := append(g_clay_sprites, Sprite_Data{
                    location = {4*17, 2*17},
                    size     = {16, 16},
                    anchor   = {0, 0},
                    position = {bounding_box.x, bounding_box.y},
                    scale    = {bounding_box.width/16, bounding_box.height/16},
                    rotation = 0,
                    color    = transmute(u32)linalg.array_cast(config.color, u8),
                })
                assert(err == nil)
            }

        case .ScissorStart, .ScissorEnd:

        case .None: fallthrough
        case:
            log.panicf("unhandled clay render command: %v", render_command.commandType)
        }
    }
}