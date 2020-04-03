package pl.garedgame.game.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_squad_edition.view.*
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.adapters.GameUnitAdapter
import pl.garedgame.game.dialogs.EditPersonDialog
import pl.garedgame.game.dialogs.SetNameDialog
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.Squad
import pl.garedgame.game.objects.subOjects.Equipment

class SquadsEditionView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr) {

    private val squadsAdapter = GameUnitAdapter().apply { selectable = true }
    private val availablePersonnelAdapter = GameUnitAdapter()
    private val currentSquadMembersAdapter = GameUnitAdapter()

    init {
        LinearLayout.inflate(context, R.layout.view_squad_edition, this)

        squads.adapter = squadsAdapter
        squads.layoutManager = LinearLayoutManager(context)
        availablePersonnel.adapter = availablePersonnelAdapter
        availablePersonnel.layoutManager = LinearLayoutManager(context)
        currentSquadMembers.adapter = currentSquadMembersAdapter

        sortView.getAdapters = { arrayListOf(currentSquadMembersAdapter, availablePersonnelAdapter) }
    }

    private val selectedSquad: Squad?
        get() = squadsAdapter.getSelected() as Squad?

    fun setListener(listener: SquadsEditionListener) {
        squadsAdapter.onSelected = { _, _ -> updateAdapters(listener) }

        addNew.setBounceOnClick {
            SetNameDialog(it.context, object: SetNameDialog.SetNameDialogListener {
                override fun onSetName(name: String) {
                    val newSquad = Squad(name).apply { organisationId = listener.getOrganisationId() }
                    if (listener.assign(newSquad)) updateAdapters(listener)
                }
            }).show()
        }
        remove.setBounceOnClick {
            selectedSquad?.apply {
                if (listener.unAssign(this)) {
                    val unitsCopy = ArrayList(units)
                    for (gameUnit in unitsCopy) listener.assign(gameUnit)
                    updateAdapters(listener)
                }
            }
        }

        currentSquadMembersAdapter.onClick = { _, gameUnit ->
            selectedSquad?.apply {
                SoundPlayer.instance.playSound(R.raw.sound_button_02)
                if (unAssign(gameUnit)) {
                    listener.assign(gameUnit)
                    updateAdapters(listener)
                }
            }
        }
        currentSquadMembersAdapter.onLongClick = { _, gameUnit ->
            showEditPersonDialog(gameUnit as Person, listener)
        }

        availablePersonnelAdapter.onClick = { _, gameUnit ->
            selectedSquad?.apply {
                SoundPlayer.instance.playSound(R.raw.sound_button_02)
                if (listener.unAssign(gameUnit)) {
                    assign(gameUnit)
                    updateAdapters(listener)
                }
            }
        }
        availablePersonnelAdapter.onLongClick = { _, gameUnit ->
            showEditPersonDialog(gameUnit as Person, listener)
        }

        post { updateAdapters(listener) }
    }

    private fun showEditPersonDialog(person: Person, listener: SquadsEditionListener) {
        SoundPlayer.instance.playSound(R.raw.sound_button_01)
        EditPersonDialog(context!!, object: EditPersonDialog.EditPersonDialogListener {
            override fun getPerson(): Person = person
            override fun getItems(): ArrayList<Equipment>? = listener.getItems()
            override fun onDismiss() = updateAdapters(listener)
            override fun getNavController(): NavController? = findNavController()
        }).show()
    }

    private fun updateAdapters(listener: SquadsEditionListener) {
        updateCurrentSquadDetails()
        val squads = listener.getSquads()
        val availablePersonnel = listener.getPersons()
        squadsAdapter.updateItems(squads)
        availablePersonnelAdapter.updateItems(availablePersonnel)
        sortView.sort(arrayListOf(availablePersonnelAdapter))
    }

    private fun updateCurrentSquadDetails(squad: Squad? = selectedSquad) {
        if (squad != null) {
            remove.visibility = View.VISIBLE
            squadName.visibility = View.VISIBLE
            squadName.text = squad.name
            squadName.setOnClickListener {
                SetNameDialog(it.context, object: SetNameDialog.SetNameDialogListener {
                    override fun getCurrentName(): String = squad.name
                    override fun onSetName(name: String) {
                        squad.name = name
                        squadName.text = squad.name
                    }
                }).show()
            }
            currentSquadMembersAdapter.updateItems(ArrayList(squad.units))
            squadsAdapter.notifyDataSetChanged()
        } else {
            remove.visibility = View.INVISIBLE
            squadName.visibility = View.GONE
            squadName.text = ""
            currentSquadMembersAdapter.updateItems(ArrayList())
        }
    }

    interface SquadsEditionListener {
        fun assign(unit: GameUnit): Boolean
        fun unAssign(unit: GameUnit): Boolean
        fun getItems(): ArrayList<Equipment>?
        fun getPersons(): List<Person>
        fun getSquads(): List<Squad>
        fun getOrganisationId(): Long
    }
}
