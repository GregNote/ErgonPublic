package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.fragment_world_map.*
import pl.garedgame.game.Game
import pl.garedgame.game.Notification
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.adapters.EventAdapter
import pl.garedgame.game.adapters.GameUnitAdapter
import pl.garedgame.game.extensions.registerOnBackPressedCallback
import pl.garedgame.game.extensions.unregisterOnBackPressedCallback
import pl.garedgame.game.objects.Base
import pl.garedgame.game.objects.subOjects.BuildingCreation
import pl.garedgame.game.objects.subOjects.Production
import pl.garedgame.game.objects.subOjects.Research
import java.text.SimpleDateFormat
import java.util.*

class WorldMapFragment : Fragment(), Notification.Observer {

    var gameUnitsShowed = false
    var gameUnitAdapter = GameUnitAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_world_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameUnitAdapter.onClick = { _, gameUnit ->
            when (gameUnit) {
                is Base -> {
                    gameUnitsList.findNavController().navigate(R.id.worldMap_to_baseFragment, Bundle().apply { putLong("baseId", gameUnit.id) })
                }
            }
            updateGameUnitsShowed(false)
        }
        gameUnitAdapter.onLongClick = { _, gameUnit ->
            map.camera.lockTo(gameUnit)
        }
        gameUnitAdapter.updateItems(Game.instance.getVisibleUnits())
        gameUnitsList.adapter = gameUnitAdapter
        gameUnitsList.layoutManager = LinearLayoutManager(context)
        nearestEvents.adapter = EventAdapter()
        nearestEvents.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        clock.text = SimpleDateFormat.getDateTimeInstance().format(Date(Game.instance.save.timer.timestamp))
        map.updateCallback = {
            clock.apply {
                post {
                    text = SimpleDateFormat.getDateTimeInstance().format(Date(Game.instance.save.timer.timestamp))
                }
            }
        }
        showHideBases.setOnClickListener {
            SoundPlayer.instance.playSound(R.raw.sound_button_02)
            updateGameUnitsShowed(!gameUnitsShowed)
        }
        Game.instance.getUnits().find { it is Base }?.apply {
            map.camera.moveCameraTo(orientation.pos.x, orientation.pos.y)
        }
        updateGameUnitsShowed()
    }

    private fun updateGameUnitsShowed(pBasesShowed: Boolean = gameUnitsShowed) {
        gameUnitsShowed = pBasesShowed
        TransitionManager.beginDelayedTransition(mapLayout)
        val set = ConstraintSet()
        set.clone(mapLayout)
        set.clear(R.id.gameUnitsList, ConstraintSet.START)
        set.clear(R.id.gameUnitsList, ConstraintSet.END)
        if (gameUnitsShowed) {
            gameUnitAdapter.updateItems(Game.instance.getUnits())
            set.connect(R.id.gameUnitsList, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            set.setRotation(R.id.showHideBases, -90f)
        } else {
            set.connect(R.id.gameUnitsList, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.START)
            set.setRotation(R.id.showHideBases, 90f)
        }
        set.applyTo(mapLayout)
    }

    private fun updateNotification(show: Boolean) {
        TransitionManager.beginDelayedTransition(mapLayout)
        val set = ConstraintSet()
        set.clone(mapLayout)
        set.clear(R.id.notificationInfo, ConstraintSet.TOP)
        set.clear(R.id.notificationInfo, ConstraintSet.BOTTOM)
        if (show) {
            set.connect(R.id.notificationInfo, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        } else {
            set.connect(R.id.notificationInfo, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        }
        set.applyTo(mapLayout)
    }

    override fun onEvent(event: Notification.Event, vararg args: Any) {
        when (event) {
            Notification.Event.PersonSpotted -> showNotification("Person Spotted!")
            Notification.Event.SquadSpotted -> showNotification("Squad Spotted!")
            Notification.Event.VehicleSpotted -> showNotification("Vehicle Spotted!")
            Notification.Event.BaseSpotted -> showNotification("Base Spotted!")
            Notification.Event.MissionFinished -> showNotification("Mission Finished!")
            Notification.Event.JobFinished -> {
                if (args.isNotEmpty()) {
                    args[0].apply {
                        when (this) {
                            is Research -> {
                                showNotification("$name Finished!")
                            }
                            is Production -> {
                                showNotification("$item (${count}) Finished!")
                            }
                            is BuildingCreation -> {
                                showNotification("${building.name} Finished!")
                            }
                            else -> return
                        }
                    }

                }
            }
            else -> { }
        }
    }

    private fun showNotification(text: String, action: (() -> Unit)? = null) {
        notificationInfo.text = text
        gameTimerView.setPause()
        updateNotification(true)
        notificationInfo.setOnClickListener {
            action?.invoke()
            updateNotification(false)
            gameTimerView.setNormal()
        }
    }

    override fun onResume() {
        super.onResume()
        Game.instance.save.timer.state = 1f
        SoundPlayer.instance.playMusic(R.raw.main_theme)
        Notification.register(Notification.Event.JobFinished, this)
        Notification.register(Notification.Event.PersonSpotted, this)
        Notification.register(Notification.Event.SquadSpotted, this)
        Notification.register(Notification.Event.VehicleSpotted, this)
        Notification.register(Notification.Event.BaseSpotted, this)
        Notification.register(Notification.Event.MissionFinished, this)
        map.onResume()
        registerOnBackPressedCallback {
            context?.apply {
                Game.instance.saveGame()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Game.instance.save.timer.state = 0f
        map.onPause()
        unregisterOnBackPressedCallback()
        Notification.unregister(this)
    }
}
