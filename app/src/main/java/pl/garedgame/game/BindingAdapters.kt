package pl.garedgame.game

import android.widget.TextView
import androidx.databinding.BindingAdapter
import pl.garedgame.game.objects.Base
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.subOjects.Skill
import pl.garedgame.game.ui.AvatarView
import pl.garedgame.game.ui.BaseView
import pl.garedgame.game.ui.SkillView
import pl.garedgame.game.util.doOnMainThread

@BindingAdapter("updateActive")
fun SkillView.updateActive(active: Boolean) {
    this.active = active
}

@BindingAdapter("updateSkill")
fun SkillView.updateSkill(skill: Skill) {
    this.skill = skill
}

@BindingAdapter("updatePerson")
fun AvatarView.updatePerson(person: Person?) {
    updateColors(person)
}

@BindingAdapter("updateBase")
fun BaseView.updateBase(base: Base?) {
    getBuilding = base?.let { _base ->
        { _base.buildings[it] }
    }
}

@BindingAdapter("value")
fun TextView.setValue(value: Long) {
    text = value.toString()
}

@BindingAdapter("value")
fun TextView.setValue(value: Int) {
    text = value.toString()
}

@BindingAdapter("personHealth")
fun TextView.personHealth(person: Person) {
    { personHealth(person) }.apply { person.healthListener = this }
    doOnMainThread { text = "${person.health}/${person.maxHealth()}" }
}
