package pl.garedgame.game

import pl.garedgame.game.util.doOnMainThread

class Notification {

    companion object {

        private val observers = mutableMapOf<Event, ArrayList<Observer>>()

        fun register(event: Event, observer: Observer) {
            val array = observers[event]
            if (array != null) array.add(observer)
            else observers[event] = arrayListOf(observer)
        }

        fun unregister(observer: Observer) {
            for (array in observers.values) array.remove(observer)
        }

        fun notify(event: Event, vararg args: Any) {
            observers[event]?.apply {
                doOnMainThread {
                    for (observer in this) {
                        observer.onEvent(event, *args)
                    }
                }
            }
        }
    }

    enum class Event {
        JobFinished,
        PersonSpotted,
        SquadSpotted,
        VehicleSpotted,
        BaseSpotted,
        MissionFinished,
        DamageTaken,
        ScenarioWin,
        ScenarioLoss,
    }

    interface Observer {
        fun onEvent(event: Event, vararg args: Any)
    }
}
