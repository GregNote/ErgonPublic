package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_skirmish_lobby.*
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.adapters.RecyclerAdapter
import pl.garedgame.game.adapters.RecyclerViewHolder
import pl.garedgame.game.dialogs.SetNameDialog
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.Squad
import pl.garedgame.game.objects.subOjects.Equipment
import pl.garedgame.game.skirmish.Controller
import pl.garedgame.game.skirmish.SceneMaps
import pl.garedgame.game.skirmish.Skirmish
import pl.garedgame.game.ui.SquadsEditionView
import pl.garedgame.game.util.SharedPrefs
import pl.garedgame.game.util.px
import kotlin.math.min

class SkirmishLobbyFragment : Fragment() {

    companion object {
        fun loadLastName(): String = SharedPrefs.getString("PLAYER_KEY", "last_name").let {
            if (it.isNotEmpty()) it else "Player"
        }

        fun saveLastName(name: String) {
            SharedPrefs.putString("PLAYER_KEY", "last_name", name)
        }
    }

    private var skirmishType: String = ""
    private var controllerAdapter = ControllerAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Skirmish.clearControllers()
        skirmishType = arguments?.getString("skirmishType", skirmishType) ?: skirmishType
        return inflater.inflate(R.layout.fragment_skirmish_lobby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controllerSlots.layoutManager = LinearLayoutManager(view.context)
        controllerSlots.adapter = controllerAdapter

        when (skirmishType) {
            "public" -> {}
            "private" -> {
                val lastName = loadLastName()
                Skirmish.instance.playerController.name = lastName
                playerName.text = lastName
                playerName.setBounceOnClick { v ->
                    SetNameDialog(v.context, object : SetNameDialog.SetNameDialogListener {
                        override fun getCurrentName(): String = lastName
                        override fun onSetName(name: String) {
                            Skirmish.instance.playerController.name = name
                            playerName.text = name
                            updateControllers()
                            saveLastName(name)
                        }
                    }).show()
                }
                Skirmish.instance.playerController.prepareForSkirmish()
                Skirmish.addController(Skirmish.instance.playerController)
                startGame.setBounceOnClick { v ->
                    val bundle = Bundle().apply { putString("skirmishType", skirmishType) }
                    v.findNavController().navigate(R.id.skirmishLobby_to_skirmishFragment, bundle)
                }
                addController.setBounceOnClick {
                    val addController = if (Skirmish.instance.sceneMapIndex in SceneMaps.instance.maps.indices) {
                        val sceneMap = SceneMaps.instance.maps[Skirmish.instance.sceneMapIndex]
                        val spawnCount = sceneMap.objects.count { it.tag == "spawn" }
                        Skirmish.instance.controllers.size < spawnCount
                    } else true
                    if (addController) {
                        Skirmish.addController(Controller().apply {
                            name = "Controller_${Skirmish.getControllers().size}"
                        })
                        updateControllers()
                    }
                }
                mapSpinner.apply {
                    val indexes = arrayListOf(-1)
                    val indexesStr = arrayListOf("Editor")

                    for (i in SceneMaps.instance.maps.indices) {
                        indexes.add(i)
                        indexesStr.add("Map_${i + 1}")
                    }
                    val dataAdapter = ArrayAdapter<String>(context, R.layout.spinner_item_objective,
                            indexesStr)
                    dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_objective)
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            Skirmish.instance.sceneMapIndex = -1
                        }
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            Skirmish.instance.sceneMapIndex = indexes[position]
                            if (Skirmish.instance.sceneMapIndex in SceneMaps.instance.maps.indices) {
                                val sceneMap = SceneMaps.instance.maps[Skirmish.instance.sceneMapIndex]
                                val spawnCount = sceneMap.objects.count { it.tag == "spawn" }
                                if (Skirmish.instance.controllers.size > spawnCount) {
                                    Skirmish.clearControllers()
                                    Skirmish.addController(Skirmish.instance.playerController)
                                    updateControllers()
                                }
                            }
                        }
                    }
                    adapter = dataAdapter
                    setSelection(min(1, indexes.size - 1))
                }
            }
            else -> if (skirmishType.startsWith("ip:")) {}
        }

        squadsEditionView.setListener(object : SquadsEditionView.SquadsEditionListener {
            override fun assign(unit: GameUnit): Boolean {
                return Skirmish.instance.playerController.slots.add(unit)
            }
            override fun unAssign(unit: GameUnit): Boolean {
                return Skirmish.instance.playerController.slots.remove(unit)
            }
            override fun getItems(): ArrayList<Equipment>? = Skirmish.instance.playerController.items
            override fun getPersons(): List<Person> = Skirmish.instance.playerController.slots.filterIsInstance<Person>()
            override fun getSquads(): List<Squad> = Skirmish.instance.playerController.slots.filterIsInstance<Squad>()
            override fun getOrganisationId(): Long  = Skirmish.instance.playerController.organisation.id
        })

        updateControllers()
    }

    private fun updateControllers() {
        controllerAdapter.updateItems(Skirmish.instance.controllers.values)
    }

    override fun onResume() {
        super.onResume()
        SoundPlayer.instance.playMusic(R.raw.menu_theme)
    }

    class ControllerViewHolder(private val textView: TextView) : RecyclerViewHolder<Controller>(textView) {

        override fun bind(item: Controller?) {
            textView.text = item?.name ?: "NULL"
            textView.post {
                textView.layoutParams?.also {
                    it.width = ViewGroup.LayoutParams.MATCH_PARENT
                    textView.layoutParams = it
                }
            }
        }
        override fun clear() { }
    }

    class ControllerAdapter : RecyclerAdapter<Controller, ControllerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControllerViewHolder {
            val textView = TextView(ContextThemeWrapper(parent.context, R.style.NormalButton))
            textView.setPadding(1.px, 6.px, 1.px, 6.px)
            return ControllerViewHolder(textView)
        }
    }
}
