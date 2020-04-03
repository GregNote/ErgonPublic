package pl.garedgame.game.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_main_menu.*
import pl.garedgame.game.BuildConfig
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.dialogs.ChooseDialog
import pl.garedgame.game.dialogs.GetStringDialog
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.extensions.setOnClick
import pl.garedgame.game.multiplayer.NetUtils

class MainMenuFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.DEBUG) {
            title.setOnClick { v ->
                v.findNavController().navigate(R.id.to_editorFragment)
            }
            multiPlayerButton.setBounceOnClick { v -> showCreateOrJoinDialog(v.context) }
        } else {
            multiPlayerButton.alpha = 0.6f
        }
        continueButton.visibility = if (Game.instance.hasSavedGame()) View.VISIBLE else View.GONE
        continueButton.setBounceOnClick { v ->
            Game.instance.loadGame()
            v.findNavController().navigate(R.id.mainMenu_to_worldMapFragment)
        }
        newGameButton.setBounceOnClick { v -> v.findNavController().navigate(R.id.mainMenu_to_newGameFragment) }
        skirmishButton.setBounceOnClick { navigateToSkirmishLobby("private") }
        creditsButton.setBounceOnClick { v -> v.findNavController().navigate(R.id.mainMenu_to_creditsFragment) }
        optionsButton.setBounceOnClick { v -> v.findNavController().navigate(R.id.mainMenu_to_optionsFragment) }
    }

    private fun showCreateOrJoinDialog(context: Context) {
        ChooseDialog(context, object : ChooseDialog.ChooseDialogListener {
            override fun getDescription(): String = context.getString(R.string.createOrJoinDescription)
            override fun getOption1(): String = context.getString(R.string.createOrJoin_create)
            override fun getOption2(): String = context.getString(R.string.createOrJoin_join)
            override fun onOption1() = navigateToSkirmishLobby("public")
            override fun onOption2() = showHostIpDialog(context)
        }).show()
    }

    private fun showHostIpDialog(context: Context) {
        GetStringDialog(context, object : GetStringDialog.GetStringDialogListener {
            override fun getDescription(): String = context.getString(R.string.hostIpDescription)
            override fun getCurrentString(): String = NetUtils.loadLastIp()
            override fun onString(string: String) = navigateToSkirmishLobby("ip:$string")
        }).show()
    }

    private fun navigateToSkirmishLobby(skirmishType: String) {
        view?.findNavController()?.navigate(R.id.mainMenu_to_versusSkirmishFragment,
                Bundle().apply { putString("skirmishType", skirmishType) })
    }

    override fun onResume() {
        super.onResume()
        SoundPlayer.instance.playMusic(R.raw.menu_theme)
    }
}
