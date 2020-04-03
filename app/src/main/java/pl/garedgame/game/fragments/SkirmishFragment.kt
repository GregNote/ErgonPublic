package pl.garedgame.game.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_skirmish.*
import pl.garedgame.game.*
import pl.garedgame.game.adapters.GameUnitAdapter
import pl.garedgame.game.extensions.setOnClick
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.Squad
import pl.garedgame.game.objects.subOjects.Ability
import pl.garedgame.game.objects.subOjects.AbilityAdapter
import pl.garedgame.game.render.OpenGLRenderer
import pl.garedgame.game.skirmish.CampaignSkirmishBehavior
import pl.garedgame.game.skirmish.PrivateSkirmishBehavior
import pl.garedgame.game.skirmish.PublicSkirmishBehavior
import pl.garedgame.game.skirmish.SkirmishBehavior

class SkirmishFragment : Fragment(), Notification.Observer {

    private lateinit var skirmishBehavior: SkirmishBehavior
    var gameUnitAdapter = GameUnitAdapter()
    var abilitiesAdapter = AbilityAdapter()

    private fun updateFps(fps: Int) {
        val color = fps / 60f
        fpsLabel?.setTextColor( when {
            color <= 0.5f -> Color.rgb(
                    255,
                    (color * 2f * 255).toInt(),
                    0
            )
            color <= 1f -> Color.rgb(
                    255 - (color * 2f * 255).toInt(),
                    255,
                    0
            )
            else -> Color.rgb(0, 255, 0)
        } )
        fpsLabel?.text = "$fps"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_skirmish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerUnits.adapter = gameUnitAdapter.apply {
            isSmall = true
            selectable = true
            onSelected = { _, gameUnit ->
                gameUnitAdapter.items.filter { it.selected }.forEach { it.selected = false }
                updateAbilities(gameUnit)
                gameUnit?.selected = true
            }
        }
        unitAbilities.adapter = abilitiesAdapter.apply {
            selectable = true
            onSelected = { _, ability ->
                    if (ability?.type?.require == Ability.Require.None) {
                        gameUnitAdapter.getSelected()?.also {
                            it.playerOrder(it.orientation.pos, ability)
                        }
                        abilitiesAdapter.clearSelected()
                }
            }
        }

        when (arguments?.getString("skirmishType")) {
            "campaign" -> skirmishBehavior = CampaignSkirmishBehavior(this)
            "private" -> skirmishBehavior = PrivateSkirmishBehavior(this)
            "public" -> skirmishBehavior = PublicSkirmishBehavior(this)
        }
        skirmishBehavior.initWithArgs(arguments)
        skirmishView.camera.onClickWithPosition = skirmishBehavior::onSceneClick
        updateAbilities()
    }

    override fun onResume() {
        super.onResume()
        skirmishView.onResume()
        Game.instance.save.timer.state = 0f
        SoundPlayer.instance.playMusic(R.raw.main_theme)
        skirmishBehavior.loadScene()
        if (BuildConfig.DEBUG) OpenGLRenderer.setFpsListener(this::updateFps)
    }

    override fun onPause() {
        super.onPause()
        Game.instance.save.timer.state = 0f
        Notification.unregister(this)
        skirmishView.onPause()
        if (BuildConfig.DEBUG) OpenGLRenderer.setFpsListener(null)
    }

    override fun onEvent(event: Notification.Event, vararg args: Any) {
    }

    private fun updateAbilities(unit: GameUnit? = null) {
        val abilities = when (unit) {
            is Person -> {
                weapon1.visibility = unit.primaryWeapon?.let { View.VISIBLE } ?: View.GONE
                weapon1.setItem(unit.primaryWeapon, weapon1.resources.getString(R.string.weapon1))
                weapon2.visibility = unit.secondaryWeapon?.let { View.VISIBLE } ?: View.GONE
                weapon2.setItem(unit.secondaryWeapon, weapon1.resources.getString(R.string.weapon2))
                weapon2.setOnClick {
                    unit.swapWeapons()
                    updateAbilities(unit)
                }
                unit.getAbilities()
            }
            is Squad -> {
                weapon1.visibility = View.GONE
                weapon2.visibility = View.GONE
                unit.getAbilities()
            }
            else -> {
                weapon1.visibility = View.GONE
                weapon2.visibility = View.GONE
                arrayListOf()
            }
        }
        abilitiesAdapter.updateItems(abilities)
        abilitiesAdapter.clearSelected()
    }
}
