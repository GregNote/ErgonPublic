package pl.garedgame.game.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.gson.annotations.Expose
import pl.garedgame.game.Game
import pl.garedgame.game.Notification
import pl.garedgame.game.adapters.GameUnitViewHolder
import pl.garedgame.game.databinding.ListItemBaseBinding
import pl.garedgame.game.objects.subOjects.*
import pl.garedgame.game.util.Utils

class Base(
        firstBonus: Boolean = false,
        @Expose var name: String = Game.content.baseNames[Utils.rand.nextInt(Game.content.baseNames.size)]
) : Assignation() {

    @Expose
    var prefabricats = 0L
    @Expose
    var platinum = 0L
    @Expose
    var titanium = 0L
    @Expose
    var ergon = 0L
    @Expose
    var buildings = Array<Building?>(19) { null }
    @Expose
    var personnel = Array<ArrayList<Long>>(19) { arrayListOf() }
    @Expose
    var jobs = arrayListOf<Job>()
    @Expose
    var items = arrayListOf<Equipment>()

    init {
        putBuilding(0, Game.content.getBuilding("depot"))
        if (firstBonus) {
            putBuilding(2, Game.content.getBuilding("lab"))
            putBuilding(4, Game.content.getBuilding("workshop"))
            putBuilding(6, Game.content.getBuilding("solar_power"))
        }
        texture = "base.png"
    }

    fun getBuildingsListWithPersonnel(): ArrayList<Building> {
        val list = arrayListOf<Building>()
        for (building in buildings) building?.apply {
            if (personnelCapacity > 0) list.add(this)
        }
        return list
    }

    fun getAvailableUnits(): ArrayList<GameUnit> {
        val result = ArrayList(units)
        for (gameUnit in units) {
            if (gameUnit !is Person) result.remove(gameUnit)
            for (building in personnel) building.apply {
                if (building.contains(gameUnit.id)) result.remove(gameUnit)
            }
        }
        return result
    }

    override fun getRangeOfView() = 1500f
    override fun getFieldOfView() = 360f

    fun putBuilding(index: Int, element: Building?) {
        buildings[index] = element
    }

    private fun getResearchMultiplier(): Float {
        var researchMultiplier = 0f
        val researchBuildings = buildings.filter {
            it != null && it.type == Building.BuildingType.Research
        }
        for (i in researchBuildings.indices) {
            for (unitId in personnel[i]) {
                val person = units.find { it.id == unitId } as Person
                researchMultiplier += person.skills.scientistSkill.getLevel() / 20f
            }
        }
        return researchMultiplier
    }

    private fun getProductionMultiplier(): Float {
        var productionMultiplier = 0f
        val productionBuildings = buildings.filter {
            it != null && it.type == Building.BuildingType.Production
        }
        for (i in productionBuildings.indices) {
            for (unitId in personnel[i]) {
                val person = units.find { it.id == unitId } as Person
                productionMultiplier += person.skills.engineerSkill.getLevel() / 20f
            }
        }
        return productionMultiplier
    }

    override fun update(sinceMillis: Long) {
        super.update(sinceMillis)
        val researchMultiplier = getResearchMultiplier()
        val productionMultiplier = getProductionMultiplier()
        val jobsToFinish = arrayListOf<Job>()

        for (job in jobs) {
            when (job) {
                is Research -> job.update((sinceMillis * researchMultiplier).toLong())
                is Production -> job.update((sinceMillis * productionMultiplier).toLong())
                else -> job.update(sinceMillis)
            }
            if (job.isDone()) jobsToFinish.add(job)
        }
        finishJobs(jobsToFinish)
    }

    fun canStartJob(job: Job): Boolean {
        var start = true
        val cost = job.cost()

        if (prefabricats < (cost.prefabricats ?: 0L)) start = false
        if (platinum < (cost.platinum ?: 0L)) start = false
        if (titanium < (cost.titanium ?: 0L)) start = false
        if (ergon < (cost.ergon ?: 0L)) start = false
        for (itemCost in cost.items.orEmpty()) {
            if (itemCost.second > items.filter { it.key == itemCost.first }.size) start = false
        }
        return start
    }

    fun startJob(job: Job): Boolean {
        if (canStartJob(job)) {
            val cost = job.cost()
            cost.prefabricats?.apply { prefabricats -= this }
            cost.platinum?.apply { prefabricats -= this }
            cost.titanium?.apply { prefabricats -= this }
            cost.ergon?.apply { prefabricats -= this }
            cost.items?.apply {
                for (itemCost in this) {
                    val itemsByKey = items.filter { it.key == itemCost.first }
                    for (i in 0 until itemCost.second) {
                        items.remove(itemsByKey[i])
                    }
                }
            }
            jobs.add(job)
            return true
        }
        return false
    }

    private fun finishJobs(jobsToFinish: ArrayList<Job>) {
        for (job in jobsToFinish) {
            when (job) {
                is Research -> {
                    Game.instance.save.organisations.find { it.id == organisationId }?.apply {
                        archivedResearches.add(job)
                    }
                }
                is Production -> {
                    for (i in 0 until job.count) items.add(Game.content.newEquipment(job.item))
                }
                is BuildingCreation -> {
                    putBuilding(job.index, job.building)
                }
            }
            jobs.remove(job)
            Notification.notify(Notification.Event.JobFinished, job)
        }
    }

    fun getCurrentEnergy(): Int {
        var energy = 0
        for (building in buildings) {
            building?.apply {
                if (energyBalance < 0) energy += Math.abs(energyBalance)
            }
        }
        return energy
    }

    fun getMaxEnergy(): Int {
        var energy = 0
        for (building in buildings) {
            building?.apply {
                if (energyBalance > 0) energy += Math.abs(energyBalance)
            }
        }
        return energy
    }

    fun getPersonelCount(): Int {
        return getCountOf(Person::class.java)
    }

    fun getEnergyState(): String {
        return "${getCurrentEnergy()}/${getMaxEnergy()}"
    }

    class BaseViewHolder(
            parent: ViewGroup,
            private val binding: ListItemBaseBinding = ListItemBaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ) : GameUnitViewHolder(binding.root) {

        override fun bind(item: GameUnit?) {
            if (item is Base) {
                binding.base = item
                binding.isSmall = isSmall
            } else clear()
        }

        override fun clear() {
            binding.base = null
        }
    }
}
