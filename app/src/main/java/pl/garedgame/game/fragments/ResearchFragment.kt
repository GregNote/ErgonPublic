package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_research.*
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.SoundPlayer
import pl.garedgame.game.adapters.ResearchAdapter
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.objects.Base
import pl.garedgame.game.objects.Organisation
import pl.garedgame.game.objects.subOjects.Research

class ResearchFragment : Fragment() {
    private var baseId = -1L
    private lateinit var base: Base
    private lateinit var organisation: Organisation
    private var archived = false
    private lateinit var researchAdapter: ResearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_research, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseId = arguments?.getLong("baseId", baseId) ?: baseId
        base = Game.instance.findGameUnitById(baseId) as Base
        organisation = Game.instance.save.organisations.find { it.id == base.organisationId } as Organisation
        researchAdapter = ResearchAdapter()
        projects.layoutManager = LinearLayoutManager(context)
        projects.adapter = researchAdapter

        researchAdapter.onClick = {
            SoundPlayer.instance.playSound(R.raw.sound_button_02)
            setResearchDetails(researchAdapter.getSelectedResearch())
        }
        researchAdapter.isCurrent = { base.jobs.contains(it) }

        projectsListSwitcher.setBounceOnClick {
            updateProjects(!archived)
        }
        startProject.setBounceOnClick {
            if (!archived) researchAdapter.getSelectedResearch()?.apply {
                if (!hasActiveResearch()) {
                    base.startJob(this)
                    updateProjects()
                    setResearchDetails(this)
                }
            }
        }
        updateProjects()
    }

    private fun hasActiveResearch() = base.jobs.any { job -> job is Research }

    private fun setResearchDetails(research: Research?) {
        if (research != null) {
            projectDescription.text = research.description
            if (base.jobs.contains(research)) {
                val currentResearch = base.jobs.find { it == research } as Research
                projectName.text = "${currentResearch.name} (${currentResearch.progressPercent()}%)"
                projectRequirements.visibility = View.VISIBLE
                projectRequirements.text = "Pozostało : ${currentResearch.formattedRemainTime()}"
            } else {
                projectName.text = research.name
                val requirements = research.cost.getString()
                projectRequirements.visibility = if (requirements.isNotEmpty()) {
                    projectRequirements.text = "Koszt:$requirements"
                    View.VISIBLE
                } else View.GONE
            }
            startProject.visibility = if (archived || hasActiveResearch()) View.GONE else View.VISIBLE
        } else {
            projectName.text = ""
            projectDescription.text = ""
            projectRequirements.text = ""

            startProject.visibility = View.GONE
        }
    }

    private fun updateProjects(_archived: Boolean = archived) {
        archived = _archived
        researchAdapter.update(if (archived) organisation.archivedResearches else organisation.getAvailableResearch())
        setResearchDetails(researchAdapter.getSelectedResearch())
    }

    override fun onResume() {
        super.onResume()
        SoundPlayer.instance.playMusic(R.raw.main_theme)
    }
}
