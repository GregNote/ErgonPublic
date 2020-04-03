package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_personnel.*
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.adapters.GameUnitAdapter
import pl.garedgame.game.dialogs.EditPersonDialog
import pl.garedgame.game.objects.Base
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.subOjects.Building
import pl.garedgame.game.objects.subOjects.Equipment
import pl.garedgame.game.ui.BuildingView

class PersonnelFragment : Fragment() {
    private var baseId = -1L
    private lateinit var base: Base
    private lateinit var personnelAdapter: PersonnelAdapter
    private lateinit var gameUnitAdapter: GameUnitAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_personnel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseId = arguments?.getLong("baseId", baseId) ?: baseId
        base = Game.instance.findGameUnitById(baseId) as Base
        personnelAdapter = PersonnelAdapter(base)
        gameUnitAdapter = GameUnitAdapter(base.getAvailableUnits())

        buildings.layoutManager = LinearLayoutManager(context)
        buildings.adapter = personnelAdapter
        personnelAdapter.onClick = {
            SoundPlayer.instance.playSound(R.raw.sound_button_02)
            updateAvailableUnits()
        }

        availablePersonnel.layoutManager = LinearLayoutManager(context)
        availablePersonnel.adapter = gameUnitAdapter
        gameUnitAdapter.onClick = { _, gameUnit ->
            personnelAdapter.getSelectedBuilding()?.also {
                SoundPlayer.instance.playSound(R.raw.sound_button_02)
                val personnel = base.personnel[base.buildings.indexOf(it)]
                if (it.personnelCapacity > personnel.size && !personnel.contains(gameUnit.id)) {
                    personnel.add(gameUnit.id)
                    personnelAdapter.notifySelected()
                    updateAvailableUnits()
                }
            }
        }
        gameUnitAdapter.onLongClick = { _, gameUnit ->
            if (gameUnit is Person) {
                SoundPlayer.instance.playSound(R.raw.sound_button_01)
                EditPersonDialog(context!!, object: EditPersonDialog.EditPersonDialogListener {
                    override fun getPerson(): Person = gameUnit
                    override fun getItems(): ArrayList<Equipment>? = base.items
                    override fun onDismiss() = updateAvailableUnits()
                }).show()
            }
        }
        sortView.getAdapters = { arrayListOf(gameUnitAdapter) }
    }

    private fun updateAvailableUnits() {
        gameUnitAdapter.updateItems(base.getAvailableUnits())
        sortView.sort(arrayListOf(gameUnitAdapter))
    }

    override fun onResume() {
        super.onResume()
        SoundPlayer.instance.playMusic(R.raw.main_theme)
    }

    class PersonnelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val buildingImage = view.findViewById(R.id.buildingImage) as BuildingView
        val buildingCapacity = view.findViewById(R.id.buildingCapacity) as TextView
        val gameUnitAdapter = GameUnitAdapter()
        init {
            view.findViewById<RecyclerView>(R.id.personnelList).apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = gameUnitAdapter
            }
        }
    }

    class PersonnelAdapter(private val base: Base) : RecyclerView.Adapter<PersonnelViewHolder>() {
        private var selected = -1
        var onClick: (() -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonnelViewHolder {
            return PersonnelViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_personnel, parent, false))
        }

        fun getSelectedBuilding(): Building? =
                if (selected >= 0 && selected < base.getBuildingsListWithPersonnel().size) {
            base.getBuildingsListWithPersonnel()[selected]
        } else null

        fun notifySelected() {
            if (selected >= 0 && selected < base.getBuildingsListWithPersonnel().size)
                notifyItemChanged(selected)
        }

        override fun getItemCount() = base.getBuildingsListWithPersonnel().size

        override fun onBindViewHolder(holder: PersonnelViewHolder, position: Int) {
            if (selected >= 0) selected = Math.min(selected, itemCount - 1)
            val item = base.getBuildingsListWithPersonnel()[position]
            holder.buildingImage.getBuilding = { item }
            holder.buildingImage.setSelection(position == selected)
            holder.buildingImage.setOnClickListener {
                SoundPlayer.instance.playSound(R.raw.sound_button_02)
                val prevSelected = selected
                selected = if (selected == position) -1 else position
                if (selected >= 0) notifyItemChanged(selected)
                if (prevSelected >= 0) notifyItemChanged(prevSelected)
            }
            val personnel = base.personnel[base.buildings.indexOf(item)]
            holder.buildingCapacity.text = "${personnel.size}/${item.personnelCapacity}"
            holder.gameUnitAdapter.updateItems(ArrayList(base.units.filter { personnel.contains(it.id) }))
            holder.gameUnitAdapter.onClick = { _, gameUnit ->
                personnel.remove(gameUnit.id)
                notifyItemChanged(position)
                onClick?.invoke()
            }
            holder.gameUnitAdapter.onLongClick = { _, gameUnit ->
                SoundPlayer.instance.playSound(R.raw.sound_button_01)
                EditPersonDialog(holder.itemView.context, object: EditPersonDialog.EditPersonDialogListener {
                    override fun getPerson(): Person = gameUnit as Person
                    override fun getItems(): ArrayList<Equipment>? = base.items
                    override fun onDismiss() = notifyItemChanged(position)
                }).show()
            }
        }
    }
}
