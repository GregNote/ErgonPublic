package pl.garedgame.game

import com.google.gson.annotations.Expose

class SpaceOrientation2D(
        @Expose var scaleX: Float = 1f,
        @Expose var scaleY: Float = 1f,
        @Expose var rotation: Float = 0f,
        @Expose val pos: Vector2 = Vector2.ZERO
)
