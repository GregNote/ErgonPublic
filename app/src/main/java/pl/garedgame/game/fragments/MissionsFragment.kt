package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_missions.*
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.objects.Base

class MissionsFragment : Fragment() {

    private var baseId = -1L
    private lateinit var base: Base

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_missions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseId = arguments?.getLong("baseId", baseId) ?: baseId
        base = Game.instance.findGameUnitById(baseId) as Base

        Game.instance.getUnits().find { it is Base }?.apply {
            mapView.camera.moveCameraTo(orientation.pos.x, orientation.pos.y)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        SoundPlayer.instance.playMusic(R.raw.main_theme)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
