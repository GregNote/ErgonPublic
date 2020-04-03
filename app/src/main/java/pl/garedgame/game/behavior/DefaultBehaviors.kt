package pl.garedgame.game.behavior

class DefaultBehaviors {
    companion object {
        fun playerPersonDefaultBehaviors() = BTFilter(
                BTBoolean(WSF.IsOperative, true),
                BTFilter(
                        BTBoolean(WSF.HasOrder, false),
                        BTSelector(
                                BTFilter(
                                        BTBoolean(WSF.HasWeapon, true),
                                        BTFilter(
                                                BTBoolean(WSF.EnemyInSight, true),
                                                BTAttackNearestEnemy()
                                        )
                                ),
                                BTFilter(
                                        BTBoolean(WSF.HasHealingItem, true),
                                        BTSelector(
                                                BTFilter(
                                                        BTBoolean(WSF.IsWounded, true),
                                                        BTHealSelf()
                                                ),
                                                BTFilter(
                                                        BTBoolean(WSF.IsWoundedSquadMate, true),
                                                        BTSequence(
                                                                BTMoveNearToUnit(WSF.MostWoundedSquadMateId),
                                                                BTHealMostWoundedSquadMate()
                                                        )
                                                )
                                        )
                                ),
                                BTFilter(
                                        BTBoolean(WSF.IsSelected, false),
                                        BTRecon()
                                )
                        )
                )
        )
        fun controllerPersonDefaultBehaviors() = BTFilter(
                BTBoolean(WSF.IsOperative, true),
                BTSelector(
                        BTFilter(
                                BTBoolean(WSF.EnemyInSight, true),
                                BTFilter(
                                        BTBoolean(WSF.HasWeapon, true),
                                        BTAttackNearestEnemy()
                                )
                        ),
                        BTFilter(
                                BTBoolean(WSF.EnemyInSight, false),
                                BTRecon()
                        )
                )
        )
    }
}