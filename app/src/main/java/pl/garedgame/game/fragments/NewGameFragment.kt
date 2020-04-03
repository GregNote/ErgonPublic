package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_new_game.*
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.adapters.GameUnitAdapter
import pl.garedgame.game.dialogs.EditPersonDialog
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.subOjects.Equipment

class NewGameFragment : Fragment() {

    companion object {
        const val needToPickCount = 9
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        needToPickLabel.text = getString(R.string.needToPickNPersons, needToPickCount)

        val startPersonnel = arrayListOf<GameUnit>()
        for (i in 0 until needToPickCount * 4) startPersonnel.add(Person())

        val chosenAdapter = GameUnitAdapter()
        val toChooseAdapter = GameUnitAdapter(startPersonnel)

        val update = {
            startNewGame.visibility = if (chosenAdapter.items.size == needToPickCount) View.VISIBLE else View.INVISIBLE
            needToPickLabel.visibility = if (chosenAdapter.items.size == needToPickCount) View.INVISIBLE else View.VISIBLE
            needToPickLabel.text = getString(R.string.needToPickNPersonsCurrent, needToPickCount, chosenAdapter.items.size)
            sortView.sort(arrayListOf(chosenAdapter, toChooseAdapter))
        }

        chosenAdapter.onClick = { _, gameUnit ->
            if (chosenAdapter.removeItem(gameUnit)) {
                toChooseAdapter.addItem(gameUnit)
            }
            update.invoke()
        }
        chosenAdapter.onLongClick = { _, gameUnit ->
            EditPersonDialog(context!!, object: EditPersonDialog.EditPersonDialogListener {
                override fun getPerson(): Person = gameUnit as Person
                override fun getItems(): ArrayList<Equipment>? = null
                override fun onDismiss() = update.invoke()
            }).show()
        }
        toChooseAdapter.onClick = { _, gameUnit ->
            if (toChooseAdapter.removeItem(gameUnit)) {
                chosenAdapter.addItem(gameUnit)
            }
            update.invoke()
        }
        toChooseAdapter.onLongClick = { _, gameUnit ->
            EditPersonDialog(context!!, object: EditPersonDialog.EditPersonDialogListener {
                override fun getPerson(): Person = gameUnit as Person
                override fun getItems(): ArrayList<Equipment>? = null
                override fun onDismiss() = update.invoke()
            }).show()
        }

        sortView.getAdapters = { arrayListOf(chosenAdapter, toChooseAdapter) }

        chosen.layoutManager = LinearLayoutManager(context)
        chosen.adapter = chosenAdapter
        toChoose.layoutManager = LinearLayoutManager(context)
        toChoose.adapter = toChooseAdapter

        startNewGame.setBounceOnClick { v ->
            val orgName = if (organisationNameEdit.text.isNullOrEmpty()) {
                "OBFS"
            } else organisationNameEdit.text.toString()
            Game.instance.newGame(orgName, chosenAdapter.items)
            v.findNavController().navigate(R.id.newGame_to_worldMapFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        SoundPlayer.instance.playMusic(R.raw.menu_theme)
    }
}
