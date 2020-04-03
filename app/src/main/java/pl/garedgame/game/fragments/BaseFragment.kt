package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_base.*
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.dialogs.ChooseBuildingDialog
import pl.garedgame.game.dialogs.SetNameDialog
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.objects.Base
import pl.garedgame.game.objects.subOjects.Building
import pl.garedgame.game.objects.subOjects.BuildingCreation

class BaseFragment : Fragment() {
    private var baseId = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_base, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseId = arguments?.getLong("baseId", baseId) ?: baseId
        val base = Game.instance.findGameUnitById(baseId) as Base
        baseView.getBuilding = { base.buildings[it] }
        baseView.onClick = {
            base.buildings[it]?.apply {
                when (type) {
                    Building.BuildingType.Research -> {
                        baseView.findNavController().navigate(R.id.base_to_researchFragment, Bundle().apply { putLong("baseId", baseId) })
                    }
                    Building.BuildingType.Production -> {
                        baseView.findNavController().navigate(R.id.base_to_productionFragment, Bundle().apply { putLong("baseId", baseId) })
                    }
                    Building.BuildingType.Other -> { }
                }
            }
        }
        baseView.onLongClick = {
            ChooseBuildingDialog(baseView.context, object: ChooseBuildingDialog.ChooseBuildingDialogListener {
                override fun onChooseBuilding(key: String) {
                    base.startJob(BuildingCreation(it, Game.content.getBuilding(key)))
                    baseView.invalidate()
                }
            }).show()
        }

        fBaseName.text = base.name
        fBaseName.setOnClickListener {
            SetNameDialog(it.context, object: SetNameDialog.SetNameDialogListener {
                override fun getCurrentName(): String = base.name
                override fun onSetName(name: String) {
                    base.name = name
                    fBaseName.text = base.name
                }
            }).show()
        }

        fSquads.setBounceOnClick { v ->
            v.findNavController().navigate(R.id.base_to_squadsFragment, Bundle().apply { putLong("baseId", baseId) })
        }
        fPersonnel.setBounceOnClick { v ->
            v.findNavController().navigate(R.id.base_to_personnelFragment, Bundle().apply { putLong("baseId", baseId) })
        }
        fVehicles.visibility = View.GONE
        fVehicles.setBounceOnClick { v ->
            v.findNavController().navigate(R.id.base_to_vehiclesFragment, Bundle().apply { putLong("baseId", baseId) })
        }
        fResearch.setBounceOnClick { v ->
            v.findNavController().navigate(R.id.base_to_researchFragment, Bundle().apply { putLong("baseId", baseId) })
        }
        fProduction.setBounceOnClick { v ->
            v.findNavController().navigate(R.id.base_to_productionFragment, Bundle().apply { putLong("baseId", baseId) })
        }
        fMissions.setBounceOnClick { v ->
            v.findNavController().navigate(R.id.base_to_missionsFragment, Bundle().apply { putLong("baseId", baseId) })
        }
    }
}
