package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_skirmish_lobby.*
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.objects.Base
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.Squad
import pl.garedgame.game.objects.subOjects.Equipment
import pl.garedgame.game.ui.SquadsEditionView

class SquadsFragment : Fragment(), SquadsEditionView.SquadsEditionListener {

    private var baseId = -1L
    private lateinit var base: Base

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        baseId = arguments?.getLong("baseId", baseId) ?: baseId
        base = Game.instance.findGameUnitById(baseId) as Base
        return inflater.inflate(R.layout.fragment_squads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        squadsEditionView.setListener(this)
    }

    override fun onResume() {
        super.onResume()
        SoundPlayer.instance.playMusic(R.raw.main_theme)
    }

    override fun assign(unit: GameUnit): Boolean = base.assign(unit)
    override fun unAssign(unit: GameUnit): Boolean = base.unAssign(unit)
    override fun getItems(): ArrayList<Equipment>? = base.items
    override fun getPersons(): List<Person> = base.getAvailableUnits().filterIsInstance<Person>()
    override fun getSquads(): List<Squad> = base.units.filterIsInstance<Squad>()
    override fun getOrganisationId(): Long = base.organisationId
}
