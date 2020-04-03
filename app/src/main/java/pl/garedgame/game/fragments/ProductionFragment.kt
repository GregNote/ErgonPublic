package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_production.*
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.adapters.ProductionAdapter
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.objects.Base
import pl.garedgame.game.objects.Organisation
import pl.garedgame.game.objects.subOjects.*

class ProductionFragment : Fragment() {
    private var baseId = -1L
    private lateinit var base: Base
    private lateinit var organisation: Organisation
    private lateinit var productionAdapter: ProductionAdapter
    private var countToProduce = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_production, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseId = arguments?.getLong("baseId", baseId) ?: baseId
        base = Game.instance.findGameUnitById(baseId) as Base
        organisation = Game.instance.save.organisations.find { it.id == base.organisationId } as Organisation
        productionAdapter = ProductionAdapter()
        productions.layoutManager = LinearLayoutManager(context)
        productions.adapter = productionAdapter

        inc.setBounceOnClick {
            ++countToProduce
            productionCount.text = countToProduce.toString()
            setProductionDetails(productionAdapter.getSelectedProduction())
        }
        dec.setBounceOnClick {
            if (countToProduce > 1) {
                --countToProduce
                productionCount.text = countToProduce.toString()
                setProductionDetails(productionAdapter.getSelectedProduction())
            }
        }

        productionAdapter.onClick = {
            setProductionDetails(productionAdapter.getSelectedProduction())
        }
        productionAdapter.isCurrent = { base.jobs.contains(it) }

        startProduction.setOnClickListener {
            SoundPlayer.instance.playSound(R.raw.sound_button_02)
            it.setBounceOnClick {
                productionAdapter.getSelectedProduction()?.apply {
                    if (!hasActiveProduction()) {
                        val production = Production(item, countToProduce)
                        base.startJob(production)
                        updateProjects()
                        setProductionDetails(production)
                    }
                }
            }
        }
        productionCount.text = countToProduce.toString()
        updateProjects()
    }

    private fun hasActiveProduction() = base.jobs.any { job -> job is Production }

    private fun setProductionDetails(production: Production?) {
        if (production != null) {
            val template = Game.content.templateEquipment(production.item)
            var desc = template.description

            when (template) {
                is WeaponMelee -> {
                    desc += "\ndmg=${template.minDamage}-${template.maxDamage}"
                    desc += "\nrange=${template.range}"
                }
                is WeaponRange -> {
                    desc += "\ndmg=${template.bulletDamage}*${template.bulletCount}"
                    desc += "\nspread angle=${template.spreadAngle}"
                }
                is Armor -> {
                    desc += "\narmor=${template.armor}"
                    desc += "\nspeed=${template.speed}"
                }
                is Explosive -> {
                    desc += "\ndmg=${template.damage}"
                    desc += "\nrange=${template.range}"
                }
                is Usable -> {
                    desc += "\nloads=${template.loads}"
                    template.health?.let { health ->  desc += "\nheal=${health}" }
                }
            }
            productionDescription.text = desc

            if (base.jobs.contains(production)) {
                val currentProduction = base.jobs.find { it == production } as Production
                productionName.text = "${template.name} (${currentProduction.progressPercent()}%)"
                productionRequirements.visibility = View.VISIBLE
                productionRequirements.text = "Pozostało : ${currentProduction.formattedRemainTime()}"
            } else {
                productionName.text = template.name
                val requirements = template.cost.getString(Math.max(production.count, countToProduce))
                productionRequirements.visibility = if (requirements.isNotEmpty()) {
                    productionRequirements.text = "Koszt:$requirements"
                    View.VISIBLE
                } else View.GONE
            }
            startProduction.visibility = if (hasActiveProduction()) View.GONE else View.VISIBLE
        } else {
            productionName.text = ""
            productionDescription.text = ""
            productionRequirements.text = ""

            startProduction.visibility = View.GONE
        }
    }

    private fun updateProjects() {
        val availableProduction = arrayListOf<Production>()
        for (job in base.jobs.filter { job -> job is Production }) {
            availableProduction.add(job as Production)
        }
        for (equipment in organisation.getAvailableEquipment()) {
            availableProduction.add(Production(equipment, countToProduce))
        }
        productionAdapter.update(availableProduction)
        setProductionDetails(productionAdapter.getSelectedProduction())
    }

    override fun onResume() {
        super.onResume()
        SoundPlayer.instance.playMusic(R.raw.main_theme)
    }
}
