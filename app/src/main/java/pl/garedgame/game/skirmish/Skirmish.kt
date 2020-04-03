package pl.garedgame.game.skirmish

import pl.garedgame.game.objects.GameUnit
import pl.garedgame.game.objects.Organisation

class Skirmish {

    var sceneMapIndex = -1
    var playerController = Controller()
    val controllers = hashMapOf<Int, Controller>()

    companion object {
        val instance = Skirmish()

        fun hasControllerWithName(name: String): Boolean {
            return instance.controllers.values.find { it.name == name } != null
        }

        fun getControllerById(id: Int): Controller? = instance.controllers[id]

        fun getControllerByOrganisationId(id: Long): Controller? {
            return instance.controllers.values.find { it.organisation.id == id }
        }

        fun addController(controller: Controller) {
            instance.controllers[instance.controllers.size] = controller
            setupController(controller)
        }

        fun addController(id: Int, controller: Controller) {
            instance.controllers[id] = controller
            setupController(controller)
        }

        private fun setupController(controller: Controller) {
            for (c in instance.controllers.values) {
                if (c != controller) {
                    c.organisation.setRelation(controller.organisation.id, Organisation.Relation.Enemy)
                    controller.organisation.setRelation(c.organisation.id, Organisation.Relation.Enemy)
                }
            }
        }

        fun removeController(id: Int) {
            instance.controllers.remove(id)
        }

        fun getAllUnits(): ArrayList<GameUnit> {
            val units = arrayListOf<GameUnit>()
            instance.controllers.values.forEach { controller -> units.addAll(controller.getAllUnits()) }
            return units
        }

        fun getControllers(): Collection<Controller> = instance.controllers.values

        fun clearControllers() {
            instance.controllers.clear()
        }
    }
}
