package pl.garedgame.game.skirmish

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_skirmish.*
import pl.garedgame.game.Game
import pl.garedgame.game.Notification
import pl.garedgame.game.Scenario
import pl.garedgame.game.Vector2
import pl.garedgame.game.dialogs.ChooseDialog
import pl.garedgame.game.fragments.SkirmishFragment
import pl.garedgame.game.multiplayer.Command
import pl.garedgame.game.multiplayer.GameServer
import pl.garedgame.game.objects.Assignation
import pl.garedgame.game.objects.GameUnit
import java.lang.ref.WeakReference
import kotlin.math.min

abstract class SkirmishBehavior(fragment: SkirmishFragment) {
    private val fragmentWR = WeakReference(fragment)
    protected fun getFragment(): SkirmishFragment? = fragmentWR.get()
    open fun initWithArgs(args: Bundle?) {
        getFragment()?.let { fragment ->
            val toAdapter = arrayListOf<GameUnit>()
            Skirmish.instance.playerController.also { controller ->
                controller.slots.forEach { slot ->
                    toAdapter.add(slot)
                    if (slot is Assignation) {
                        slot.units.forEach { unit ->
                            toAdapter.add(unit)
                        }
                    }
                }
            }
            fragment.gameUnitAdapter.addItems(toAdapter)
            fragment.gameUnitAdapter.onLongClick = { _, gameUnit ->
                fragment.skirmishView.camera.lockTo(gameUnit)
            }
        }
    }
    abstract fun onSceneClick(position: Vector2)
    open fun loadScene() {
        getFragment()?.let { fragment ->
            if (Skirmish.instance.sceneMapIndex in SceneMaps.instance.maps.indices) {
                fragment.skirmishView.loadScene(Skirmish.instance.sceneMapIndex) {
                    onSceneLoaded()
                }
            } else {
                fragment.skirmishView.loadEditorScene {
                    onSceneLoaded()
                }
            }
        }
    }
    protected open fun onSceneLoaded() {
        getFragment()?.let { fragment ->
            fragment.skirmishView.scene.apply {
                val spawns = findTaggedObjects("spawn")
                if (spawns.isNotEmpty())
                    for ((i, controller) in Skirmish.getControllers().withIndex()) {
                        val spawnPos = spawns[i % spawns.size].orientation.pos
                        controller.getAllUnits().forEach {
                            it.orientation.pos.set(Vector2.getRandom(spawnPos, min(it.orientation.scaleX, it.orientation.scaleY)))
                        }
                    }
                Skirmish.getAllUnits().forEach { unit ->
                    add(unit)
                }
            }
        }
    }
}

class CampaignSkirmishBehavior(fragment: SkirmishFragment) : SkirmishBehavior(fragment) {
    private var squadIds: LongArray? = null
    override fun initWithArgs(args: Bundle?) {
        super.initWithArgs(args)
        squadIds = args?.getLongArray("squadIds")
    }
    override fun onSceneClick(position: Vector2) {
    }
}

class PrivateSkirmishBehavior(fragment: SkirmishFragment) : SkirmishBehavior(fragment), Notification.Observer {

    val scenario = Scenario()

    init {
        Notification.register(Notification.Event.ScenarioWin, this)
        Notification.register(Notification.Event.ScenarioLoss, this)
    }

    override fun onSceneClick(position: Vector2) {
        getFragment()?.also { fragment ->
            fragment.gameUnitAdapter.getSelected()?.also {
                if (it.playerOrder(position, fragment.abilitiesAdapter.getSelected())) {
                    fragment.abilitiesAdapter.clearSelected()
                }
            }
       }
    }

    override fun onEvent(event: Notification.Event, vararg args: Any) {
        when (event) {
            Notification.Event.ScenarioWin -> {
                getFragment()?.also {
                    it.skirmishView.scene.drawShadows = false
                    ChooseDialog(it.requireContext(), object : ChooseDialog.ChooseDialogListener {
                        override fun getDescription(): String = "You Win!"
                        override fun getOption1(): String = "Finish"
                        override fun getOption2(): String = "Continue?"
                        override fun onOption1() { it.fragmentManager?.popBackStack() }
                        override fun onOption2() { }
                    }).show()
                }
            }
            Notification.Event.ScenarioLoss -> {
                getFragment()?.also {
                    it.skirmishView.scene.drawShadows = false
                    ChooseDialog(it.requireContext(), object : ChooseDialog.ChooseDialogListener {
                        override fun getDescription(): String = "You Loss!"
                        override fun getOption1(): String = "Finish"
                        override fun getOption2(): String = "Continue?"
                        override fun onOption1() { it.fragmentManager?.popBackStack() }
                        override fun onOption2() { }
                    }).show()
                }
            }
            else -> { }
        }
    }
}

class PublicSkirmishBehavior(
        fragment: SkirmishFragment
) : SkirmishBehavior(fragment), GameServer.Listener {
    override fun initWithArgs(args: Bundle?) {
        super.initWithArgs(args)
        Game.instance.save.timer.onUpdate = null
        getFragment()?.let { fragment -> fragment.gameTimerView.visibility = View.GONE }
    }
    override fun onSceneClick(position: Vector2) { }
    override fun onNoWifi() { }
    override fun onHostStart(wifi: String) { }
    override fun onClientStart() { }
    override fun onError(error: String) { }
    override fun onData(data: Command.Data) { }
}
