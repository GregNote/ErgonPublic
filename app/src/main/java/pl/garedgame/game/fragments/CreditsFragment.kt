package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer

class CreditsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_credits, container, false)
    }

    override fun onResume() {
        super.onResume()
        SoundPlayer.instance.playMusic(R.raw.menu_theme)
    }
}
