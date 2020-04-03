package pl.garedgame.game.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.adapters.GameUnitAdapter
import pl.garedgame.game.objects.Person

class PersonSortView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr) {

    private var sortCriterion = 0
    private var criteria: ArrayList<ImageView>

    var getAdapters: (() -> ArrayList<GameUnitAdapter>)? = null

    init {
        LinearLayout.inflate(context, R.layout.view_person_sort, this)
        setBackgroundResource(R.drawable.primary_framed)

        criteria = arrayListOf(findViewById(R.id.sortSoldier), findViewById(R.id.sortEngineer), findViewById(R.id.sortMechanic), findViewById(R.id.sortScientist))
        for ((index, criterion) in criteria.withIndex()) {
            criterion.setOnClickListener {
                val i = index + 1
                SoundPlayer.instance.playSound(R.raw.sound_button_02)
                sortCriterion = if (sortCriterion != i) i else 0
                getAdapters?.apply { sort(invoke()) }
            }
        }
    }

    fun sort(gameUnitAdapters: ArrayList<GameUnitAdapter>) {
        for ((index, item) in criteria.withIndex()) {
            if (sortCriterion == (index + 1))
                item.setBackgroundResource(R.drawable.primary_dark_framed)
            else item.setBackgroundColor(Color.TRANSPARENT)
        }
        for (gameUnitAdapter in gameUnitAdapters) {
            gameUnitAdapter.sortItems(Comparator { o1, o2 ->
                if (o1 is Person && o2 is Person) {
                    when (sortCriterion) {
                        1 -> (o2.skills.soldierSkill.XP - o1.skills.soldierSkill.XP).toInt()
                        2 -> (o2.skills.engineerSkill.XP - o1.skills.engineerSkill.XP).toInt()
                        3 -> (o2.skills.mechanicSkill.XP - o1.skills.mechanicSkill.XP).toInt()
                        4 -> (o2.skills.scientistSkill.XP - o1.skills.scientistSkill.XP).toInt()
                        else -> (o1.id - o2.id).toInt()
                    }
                } else {
                    (o1.id - o2.id).toInt()
                }
            })
        }
    }
}