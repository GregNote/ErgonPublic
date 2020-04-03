package pl.garedgame.game.objects.subOjects

import android.graphics.Color
import com.google.gson.annotations.Expose
import pl.garedgame.game.Game
import pl.garedgame.game.objects.Person

class PersonAppearance {
    @Expose
    var skinTone = Color.parseColor(Person.getRandOf(Game.content.skinTones))
    @Expose
    var hairColor = Color.parseColor(Person.getRandOf(Game.content.hairColors))
    @Expose
    var irisColor = Color.parseColor(Person.getRandOf(Game.content.irisColors))
    @Expose
    var shirtColor = Color.parseColor(Person.getRandOf(Game.content.shirtColors))
}