package pl.garedgame.game

import pl.garedgame.game.skirmish.Skirmish

class Scenario : Notification.Observer {
    private var finished = false

    private var winCondition: Condition? = EliminateAllEnemies()
    private var lossCondition: Condition? = DontLossYourSquad()

    private fun updateNotifications() {
        Notification.unregister(this)
        winCondition?.eventsRelated?.forEach { event ->
            Notification.register(event, this)
        }
        lossCondition?.eventsRelated?.forEach { event ->
            Notification.register(event, this)
        }
    }

    override fun onEvent(event: Notification.Event, vararg args: Any) {
        if (!finished) {
            if (lossCondition?.eventsRelated?.contains(event) == true &&
                    lossCondition?.check() == true) {
                finished = true
                Notification.notify(Notification.Event.ScenarioLoss)
            } else if (winCondition?.eventsRelated?.contains(event) == true &&
                    winCondition?.check() == true) {
                finished = true
                Notification.notify(Notification.Event.ScenarioWin)
            }
        }
    }

    class EliminateAllEnemies : Condition() {
        override val eventsRelated = arrayOf(Notification.Event.DamageTaken)
        override fun check(): Boolean {
            return Skirmish.instance.controllers.values.filter {
                it != Skirmish.instance.playerController
            }.all { !it.isAnyAlive() }
        }
    }

    class DontLossYourSquad : Condition() {
        override val eventsRelated = arrayOf(Notification.Event.DamageTaken)
        override fun check(): Boolean {
            return !Skirmish.instance.playerController.isAnyAlive()
        }
    }

    abstract class Condition {
        abstract val eventsRelated: Array<Notification.Event>
        abstract fun check(): Boolean
    }

    abstract class GroupCondition(vararg conditions: Condition) : Condition() {
        override val eventsRelated = conditions.let { asd ->
            val events = arrayListOf<Notification.Event>()
            asd.forEach { events.addAll(it.eventsRelated) }
            Array(events.size) { events[it] }
        }
        protected val subConditions = arrayOf(*conditions)
    }

    class OrCondition(vararg conditions: Condition) : GroupCondition(*conditions) {
        override fun check() = subConditions.any { it.check() }
    }

    class AndCondition(vararg conditions: Condition) : GroupCondition(*conditions) {
        override fun check() = subConditions.all { it.check() }
    }
}
